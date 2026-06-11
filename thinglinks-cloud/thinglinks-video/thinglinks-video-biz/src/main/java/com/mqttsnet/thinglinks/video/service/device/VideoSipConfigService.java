package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.device.VideoSipConfig;

import java.util.List;

/**
 * Description:
 * 租户 SIP 配置业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
public interface VideoSipConfigService extends SuperService<Long, VideoSipConfig> {

    List<VideoSipConfig> listEnabled();

    /**
     * 刷新当前租户的 SIP 配置到 Redis
     */
    void refreshTenantCache(Long tenantId);

    /**
     * 刷新所有租户的 SIP 配置到 Redis（全量）
     */
    void refreshAllTenantCache();

    /**
     * 设为默认配置（自动取消旧默认）
     */
    void setDefault(Long id);

    /**
     * 校验 sipId 全局唯一（查 Redis Hash）
     */
    boolean isSipIdExists(String sipId);

    /**
     * 获取当前租户的默认 SIP 配置的 sipId（出站消息用）
     */
    String getDefaultSipId();
}
