package com.mqttsnet.thinglinks.video.facade;

import com.mqttsnet.basic.base.R;

/**
 * Video 服务调度任务相关API
 * <p>
 * 由 thinglinks-iot-executor (XXL-Job) 调用，通过 boot-impl 本地执行。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
public interface VideoJobHandlerFacade {

    /**
     * ZLM 流媒体服务器心跳检测 & 指标采集
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> zlmMediaServerHeartbeat(Long tenantId);

    /**
     * ABL 流媒体服务器心跳检测 & 指标采集
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> ablMediaServerHeartbeat(Long tenantId);

    /**
     * 执行录像计划调度
     * <p>扫描启用的计划，按 scheduleRule 判断是否需要触发录像开始/停止</p>
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> executeRecordPlanSchedule(Long tenantId);

    /**
     * 清理过期录像文件
     * <p>根据录像计划的 retentionDays 删除超期文件</p>
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> cleanExpiredRecordFiles(Long tenantId);

    /**
     * 刷新流媒体服务器缓存
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> refreshMediaServerCache(Long tenantId);

    /**
     * 刷新租户 SIP 配置缓存到 Redis
     * <p>由 XXL-Job 遍历所有租户后逐个调用</p>
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> refreshSipTenantConfigCache(Long tenantId);

    /**
     * 设备心跳超时检测
     * <p>从 Redis 缓存扫描在线设备，检测心跳超时并标记离线。建议调度频率：30s</p>
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> deviceKeepaliveTimeoutCheck(Long tenantId);

    /**
     * SSRC 池孤儿对账回收
     * <p>
     * 扫描指定租户的所有流媒体服务器 SSRC 池，对比 SsrcTransaction，
     * 释放无对应活跃会话的孤儿 SSRC。集群部署下通过 Redis 分布式锁保证
     * 同一 {@code mediaIdentification} 同一时刻只有一个节点执行。
     * </p>
     * <p>建议调度频率：60s</p>
     *
     * @param tenantId 租户ID
     * @return 操作结果
     */
    R<?> ssrcPoolReconcile(Long tenantId);
}
