package com.mqttsnet.thinglinks.video.manager.device.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipTenantConfigCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.entity.device.VideoSipConfig;
import com.mqttsnet.thinglinks.video.manager.device.VideoSipConfigManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoSipConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoSipConfigManagerImpl extends SuperManagerImpl<VideoSipConfigMapper, VideoSipConfig> implements VideoSipConfigManager {

    private final CachePlusOps cachePlusOps;

    @Override
    public List<VideoSipConfig> listEnabled() {
        QueryWrap<VideoSipConfig> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoSipConfig::getStatus, 1);
        return list(wrap);
    }

    @Override
    public VideoSipConfig getOneBySipId(String sipId) {
        if (StrUtil.isBlank(sipId)) {
            return null;
        }
        QueryWrap<VideoSipConfig> wrap = new QueryWrap<>();
        wrap.lambda().eq(VideoSipConfig::getSipId, sipId);
        return getOne(wrap);
    }

    @Override
    public void putSipConfigCache(String sipId, TenantSipConfigCacheVO cacheVO) {
        if (StrUtil.isBlank(sipId) || cacheVO == null) {
            return;
        }
        cachePlusOps.hSet(SipTenantConfigCacheKeyBuilder.buildHash(sipId), cacheVO);
    }

    @Override
    public void clearAllSipConfigCache() {
        cachePlusOps.del(SipTenantConfigCacheKeyBuilder.buildKey());
    }

    @Override
    public boolean existsSipConfigCache(String sipId) {
        if (StrUtil.isBlank(sipId)) {
            return false;
        }
        Object cached = cachePlusOps.hGet(SipTenantConfigCacheKeyBuilder.buildHash(sipId));
        return cached != null;
    }
}
