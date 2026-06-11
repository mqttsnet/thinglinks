package com.mqttsnet.thinglinks.video.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.service.device.DeviceKeepaliveTimeoutService;
import com.mqttsnet.thinglinks.video.service.device.VideoSipConfigService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaHeartbeatService;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanScheduleService;
import com.mqttsnet.thinglinks.video.service.ssrc.SsrcPoolReconcileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Video 调度任务 Facade 实现（boot-impl 本地调用版本）
 * <p>
 * 由 iot-executor 进程直接加载本类，调用 video-biz 中的 Service，
 * 不走 Feign 远程调用。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoJobHandlerFacadeImpl implements VideoJobHandlerFacade {

    private final VideoMediaHeartbeatService videoMediaHeartbeatService;
    private final VideoRecordPlanScheduleService videoRecordPlanScheduleService;
    private final VideoRecordFileService videoRecordFileService;
    private final VideoSipConfigService videoSipConfigService;
    private final DeviceKeepaliveTimeoutService deviceKeepaliveTimeoutService;
    private final SsrcPoolReconcileService ssrcPoolReconcileService;

    @Override
    public R<?> zlmMediaServerHeartbeat(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("ZLM 心跳检测开始 tenantId={}", tenantId);
        videoMediaHeartbeatService.zlmHeartbeat(tenantId);
        return R.success();
    }

    @Override
    public R<?> ablMediaServerHeartbeat(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("ABL 心跳检测开始 tenantId={}", tenantId);
        videoMediaHeartbeatService.ablHeartbeat(tenantId);
        return R.success();
    }

    @Override
    public R<?> executeRecordPlanSchedule(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("录像计划调度执行 tenantId={}", tenantId);
        videoRecordPlanScheduleService.executeSchedule(tenantId);
        return R.success();
    }

    @Override
    public R<?> cleanExpiredRecordFiles(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("过期录像文件清理 tenantId={}", tenantId);
        int count = videoRecordFileService.cleanExpiredFiles(tenantId);
        log.info("过期录像文件清理完成 tenantId={}, deletedCount={}", tenantId, count);
        return R.success(count);
    }

    @Override
    public R<?> refreshMediaServerCache(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("流媒体服务器缓存刷新 tenantId={}", tenantId);
        videoMediaHeartbeatService.refreshAll(tenantId);
        return R.success();
    }

    @Override
    public R<?> refreshSipTenantConfigCache(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("租户 SIP 配置缓存刷新 tenantId={}", tenantId);
        videoSipConfigService.refreshTenantCache(tenantId);
        return R.success();
    }

    @Override
    public R<?> deviceKeepaliveTimeoutCheck(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("设备心跳超时检测 tenantId={}", tenantId);
        deviceKeepaliveTimeoutService.checkTimeoutForTenant(tenantId);
        return R.success();
    }

    @Override
    public R<?> ssrcPoolReconcile(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId cannot be null");
        log.info("SSRC 池孤儿对账 tenantId={}", tenantId);
        ssrcPoolReconcileService.reconcileForTenant(tenantId);
        return R.success();
    }
}
