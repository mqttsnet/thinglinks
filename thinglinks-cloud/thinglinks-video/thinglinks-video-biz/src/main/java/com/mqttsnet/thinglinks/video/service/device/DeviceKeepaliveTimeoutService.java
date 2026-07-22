package com.mqttsnet.thinglinks.video.service.device;

/**
 * 设备心跳超时检测服务。
 * <p>
 * 从 Redis 缓存读取当前租户在线设备，检测心跳超时并标记离线。
 * 由 XXL-Job 按租户调度调用，替代原 DeviceKeepaliveMonitor 的 @Scheduled 方式。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-12
 */
public interface DeviceKeepaliveTimeoutService {

    /**
     * 检测指定租户下所有在线设备的心跳超时
     *
     * @param tenantId 租户ID（调用前 ContextUtil.setTenantId 已设置）
     */
    void checkTimeoutForTenant(Long tenantId);
}
