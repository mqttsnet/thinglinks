package com.mqttsnet.thinglinks.video.manager.device;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.entity.device.VideoSipConfig;

import java.util.List;

public interface VideoSipConfigManager extends SuperManager<VideoSipConfig> {

    List<VideoSipConfig> listEnabled();

    /**
     * 按 sipId 精确查 SIP 配置（cache miss 时溯源 DB 用）。
     */
    VideoSipConfig getOneBySipId(String sipId);

    /**
     * 按 sipId 写入或更新单条 SIP 配置缓存（全局 Hash, field=sipId）。
     */
    void putSipConfigCache(String sipId, TenantSipConfigCacheVO cacheVO);

    /**
     * 清空全局 SIP 配置 Hash（refreshAll 入口用）。
     */
    void clearAllSipConfigCache();

    /**
     * 判断指定 sipId 在缓存中是否存在（用于 sipId 唯一性校验）。
     */
    boolean existsSipConfigCache(String sipId);
}
