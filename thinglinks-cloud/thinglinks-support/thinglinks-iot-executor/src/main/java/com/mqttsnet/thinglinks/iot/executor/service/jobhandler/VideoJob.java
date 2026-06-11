package com.mqttsnet.thinglinks.iot.executor.service.jobhandler;

import java.util.function.Consumer;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.iot.executor.service.AbstractTenantJob;
import com.mqttsnet.thinglinks.video.facade.VideoJobHandlerFacade;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Video 模块调度任务处理器
 * <p>
 * 通过 XXL-Job 调度，调用 VideoJobHandlerFacade（boot-impl 本地执行）。
 * 涵盖流媒体心跳检测、录像计划调度、过期录像清理、缓存刷新等。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
@Component
@Slf4j
public class VideoJob extends AbstractTenantJob {

    @Autowired
    @Lazy
    private VideoJobHandlerFacade videoJobHandlerApi;

    // ==================== ZLM 心跳检测 ====================

    /**
     * ZLM 流媒体服务器心跳检测 & 指标采集
     * <p>建议调度频率：60s</p>
     */
    @XxlJob("zlmMediaServerHeartbeatJobHandler")
    public void zlmMediaServerHeartbeatJobHandler() {
        XxlJobHelper.log("[ZLM心跳检测]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "ZLM心跳检测", videoJobHandlerApi::zlmMediaServerHeartbeat);
        });
    }

    // ==================== ABL 心跳检测 ====================

    /**
     * ABL 流媒体服务器心跳检测 & 指标采集
     * <p>建议调度频率：60s</p>
     */
    @XxlJob("ablMediaServerHeartbeatJobHandler")
    public void ablMediaServerHeartbeatJobHandler() {
        XxlJobHelper.log("[ABL心跳检测]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "ABL心跳检测", videoJobHandlerApi::ablMediaServerHeartbeat);
        });
    }

    // ==================== 录像计划调度 ====================

    /**
     * 执行录像计划调度
     * <p>扫描启用的计划，按 scheduleRule 触发录像开始/停止。建议调度频率：30s</p>
     */
    @XxlJob("executeRecordPlanScheduleJobHandler")
    public void executeRecordPlanScheduleJobHandler() {
        XxlJobHelper.log("[录像计划调度]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "录像计划调度", videoJobHandlerApi::executeRecordPlanSchedule);
        });
    }

    // ==================== 过期录像清理 ====================

    /**
     * 清理过期录像文件
     * <p>根据录像计划的 retentionDays 删除超期文件。建议调度频率：每天凌晨 02:00</p>
     */
    @XxlJob("cleanExpiredRecordFilesJobHandler")
    public void cleanExpiredRecordFilesJobHandler() {
        XxlJobHelper.log("[过期录像清理]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "过期录像清理", videoJobHandlerApi::cleanExpiredRecordFiles);
        });
    }

    // ==================== 流媒体缓存刷新 ====================

    /**
     * 刷新流媒体服务器缓存
     * <p>建议调度频率：5min</p>
     */
    @XxlJob("refreshMediaServerCacheJobHandler")
    public void refreshMediaServerCacheJobHandler() {
        XxlJobHelper.log("[流媒体缓存刷新]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "流媒体缓存刷新", videoJobHandlerApi::refreshMediaServerCache);
        });
    }

    // ==================== SIP 配置缓存刷新 ====================

    /**
     * 刷新租户 SIP 配置缓存到 Redis
     * <p>遍历所有租户，将 video_sip_config 表数据同步到全局 Redis Hash。建议调度频率：5min</p>
     */
    @XxlJob("refreshSipTenantConfigCacheJobHandler")
    public void refreshSipTenantConfigCacheJobHandler() {
        XxlJobHelper.log("[SIP配置缓存刷新]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "SIP配置缓存刷新", videoJobHandlerApi::refreshSipTenantConfigCache);
        });
    }

    // ==================== 设备心跳超时检测 ====================

    /**
     * 设备心跳超时检测
     * <p>从 Redis 缓存扫描在线设备，检测心跳超时并标记离线。建议调度频率：30s</p>
     */
    @XxlJob("deviceKeepaliveTimeoutCheckJobHandler")
    public void deviceKeepaliveTimeoutCheckJobHandler() {
        XxlJobHelper.log("[设备心跳超时检测]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "设备心跳超时检测", videoJobHandlerApi::deviceKeepaliveTimeoutCheck);
        });
    }

    // ==================== SSRC 池孤儿对账 ====================

    /**
     * SSRC 池孤儿对账
     * <p>
     * 扫描各租户的 SSRC 池，比对 SsrcTransaction，释放孤儿 SSRC，
     * 解决页面关闭 / 进程崩溃 / 网络中断导致的资源泄漏。建议调度频率：60s
     * </p>
     * <p>集群安全：内部通过 Redis 分布式锁按 mediaIdentification 粒度互斥</p>
     */
    @XxlJob("ssrcPoolReconcileJobHandler")
    public void ssrcPoolReconcileJobHandler() {
        XxlJobHelper.log("[SSRC池对账]开始全租户任务");
        loadTenant((tenant, param) -> {
            final Long tenantId = tenant.getId();
            ContextUtil.setTenantId(tenantId);
            executeWithTiming(tenantId, "SSRC池对账", videoJobHandlerApi::ssrcPoolReconcile);
        });
    }

    // ==================== 通用执行方法 ====================

    /**
     * 带计时的执行方法
     *
     * @param tenantId 租户ID
     * @param taskName 任务名称
     * @param action   执行动作
     */
    private void executeWithTiming(Long tenantId, String taskName, Consumer<Long> action) {
        long start = System.currentTimeMillis();
        try {
            log.info("[{}]租户任务开始 tenantId={}", taskName, tenantId);
            XxlJobHelper.log("[{}]租户任务开始 tenantId={}", taskName, tenantId);

            action.accept(tenantId);

            long cost = System.currentTimeMillis() - start;
            log.info("[{}]租户任务完成 tenantId={} | 耗时={}ms", taskName, tenantId, cost);
            XxlJobHelper.log("[{}]租户任务完成 tenantId={} | 耗时={}ms", taskName, tenantId, cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[{}]租户任务异常 tenantId={} | 耗时={}ms | error={}", taskName, tenantId, cost, e.getMessage(), e);
            XxlJobHelper.log("[{}]租户任务异常 tenantId={} | error={}", taskName, tenantId, e.getMessage());
        }
    }
}
