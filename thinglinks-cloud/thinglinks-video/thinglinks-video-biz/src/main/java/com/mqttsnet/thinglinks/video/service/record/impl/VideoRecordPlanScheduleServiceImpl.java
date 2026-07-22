package com.mqttsnet.thinglinks.video.service.record.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.common.lock.video.VideoLockKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanScheduleService;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 录像计划调度服务实现。
 * <p>
 * 核心逻辑：
 * <ol>
 *   <li>查询 planStatus=1 的活跃计划</li>
 *   <li>解析 scheduleRule JSON 判断当前时间是否在录制窗口</li>
 *   <li>对每个计划加分布式锁，防止集群重复触发</li>
 *   <li>云端录像(planType=1)：调用 ZLM startRecord/stopRecord</li>
 * </ol>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DS(DsConstant.BASE_TENANT)
public class VideoRecordPlanScheduleServiceImpl implements VideoRecordPlanScheduleService {

    private final VideoRecordPlanService videoRecordPlanService;
    private final VideoMediaServerService videoMediaServerService;
    private final ZlmMediaServerOpenAnyTenantService zlmMediaServerOpenAnyTenantService;
    private final DistributedLock distributedLock;

    @Override
    public void executeSchedule(Long tenantId) {
        ContextUtil.setTenantId(tenantId);
        List<VideoRecordPlan> activePlans = videoRecordPlanService.listActivePlans();
        if (CollUtil.isEmpty(activePlans)) {
            log.debug("租户 {} 无启用的录像计划", tenantId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("录像计划调度开始: tenantId={}, activePlans={}", tenantId, activePlans.size());

        for (VideoRecordPlan plan : activePlans) {
            try {
                executePlanWithLock(plan, now);
            } catch (Exception e) {
                log.error("录像计划执行异常: planId={}, planName={}", plan.getId(), plan.getPlanName(), e);
            }
        }
    }

    private void executePlanWithLock(VideoRecordPlan plan, LocalDateTime now) {
        CacheKey lockKey = VideoLockKeyBuilder.forRecordPlanExecute(plan.getId());
        var lockResult = distributedLock.tryLockAndRun(
                lockKey.getKey(), lockKey.getExpire().getSeconds(), TimeUnit.SECONDS,
                () -> {
                    doExecutePlan(plan, now);
                    return null;
                });
        if (!lockResult.isLocked()) {
            log.debug("录像计划正在被其他实例执行: planId={}", plan.getId());
        }
    }

    private void doExecutePlan(VideoRecordPlan plan, LocalDateTime now) {
        boolean inWindow = isInRecordWindow(plan.getScheduleRule(), now);

        if (StrUtil.isBlank(plan.getMediaIdentification())) {
            log.warn("录像计划缺少流媒体标识: planId={}", plan.getId());
            return;
        }

        VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(plan.getMediaIdentification());
        if (mediaServer == null) {
            log.warn("未找到流媒体服务器: planId={}, mediaIdentification={}", plan.getId(), plan.getMediaIdentification());
            return;
        }

        if (plan.getPlanType() != null && plan.getPlanType() == 1) {
            // 云端录像
            handleCloudRecord(plan, mediaServer, inWindow);
        } else {
            // 设备录像 — 预留，后续通过 SIP 命令控制
            log.debug("设备录像计划暂不支持调度: planId={}", plan.getId());
        }
    }

    private void handleCloudRecord(VideoRecordPlan plan, VideoMediaServerResultDTO mediaServer, boolean inWindow) {
        // 检查录制格式
        String recordFormat = StrUtil.blankToDefault(plan.getRecordFormat(), "mp4");
        int segmentDuration = plan.getSegmentDuration() != null ? plan.getSegmentDuration() : 3600;

        if (inWindow) {
            // 在录制窗口内，检查是否正在录制
            boolean recording = zlmMediaServerOpenAnyTenantService.isRecording(mediaServer, "record", plan.getMediaIdentification());
            if (!recording) {
                log.info("启动云端录像: planId={}, mediaIdentification={}, format={}", plan.getId(), plan.getMediaIdentification(), recordFormat);
                boolean started = zlmMediaServerOpenAnyTenantService.startRecord(
                        mediaServer, "record", plan.getMediaIdentification(), recordFormat, segmentDuration);
                if (started) {
                    log.info("云端录像启动成功: planId={}", plan.getId());
                } else {
                    log.warn("云端录像启动失败: planId={}", plan.getId());
                }
            }
        } else {
            // 不在录制窗口，停止录制
            boolean recording = zlmMediaServerOpenAnyTenantService.isRecording(mediaServer, "record", plan.getMediaIdentification());
            if (recording) {
                log.info("停止云端录像: planId={}, mediaIdentification={}", plan.getId(), plan.getMediaIdentification());
                boolean stopped = zlmMediaServerOpenAnyTenantService.stopRecord(mediaServer, "record", plan.getMediaIdentification());
                if (stopped) {
                    log.info("云端录像停止成功: planId={}", plan.getId());
                } else {
                    log.warn("云端录像停止失败: planId={}", plan.getId());
                }
            }
        }
    }

    /**
     * 解析 scheduleRule JSON 判断当前时间是否在录制窗口内。
     * <p>
     * 支持三种调度类型：
     * <ul>
     *   <li>weekly: {"type":"weekly","days":[1,2,3,4,5],"startTime":"08:00:00","endTime":"18:00:00"}</li>
     *   <li>cron: {"type":"cron","cron":"0 0 8 * * ?","duration":36000}</li>
     *   <li>always: {"type":"always"} — 7x24全天录制</li>
     * </ul>
     */
    private boolean isInRecordWindow(String scheduleRule, LocalDateTime now) {
        if (StrUtil.isBlank(scheduleRule)) {
            // 无调度规则，默认全天录制
            return true;
        }

        try {
            JSONObject rule = JSON.parseObject(scheduleRule);
            String type = rule.getString("type");

            if ("always".equals(type)) {
                return true;
            }

            if ("weekly".equals(type)) {
                return isInWeeklyWindow(rule, now);
            }

            // 其他类型暂按全天处理
            log.debug("未识别的调度类型: {}, 默认全天录制", type);
            return true;
        } catch (Exception e) {
            log.warn("解析 scheduleRule 失败: {}, 默认全天录制", scheduleRule, e);
            return true;
        }
    }

    private boolean isInWeeklyWindow(JSONObject rule, LocalDateTime now) {
        JSONArray days = rule.getJSONArray("days");
        if (CollUtil.isEmpty(days)) {
            return false;
        }

        int todayValue = now.getDayOfWeek().getValue(); // 1=Monday ... 7=Sunday
        boolean dayMatch = false;
        for (int i = 0; i < days.size(); i++) {
            if (days.getIntValue(i) == todayValue) {
                dayMatch = true;
                break;
            }
        }
        if (!dayMatch) {
            return false;
        }

        String startTimeStr = rule.getString("startTime");
        String endTimeStr = rule.getString("endTime");
        if (StrUtil.isBlank(startTimeStr) || StrUtil.isBlank(endTimeStr)) {
            return true;
        }

        // "HH:mm:ss" 就是 ISO_LOCAL_TIME 默认格式，LocalTime.parse 不用额外 formatter
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);
        LocalTime nowTime = now.toLocalTime();

        if (endTime.isAfter(startTime)) {
            return !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
        } else {
            // 跨天: e.g. 22:00 -> 06:00
            return !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime);
        }
    }
}
