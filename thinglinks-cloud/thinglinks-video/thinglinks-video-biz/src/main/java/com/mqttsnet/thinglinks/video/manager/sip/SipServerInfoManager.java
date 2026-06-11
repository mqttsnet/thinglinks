package com.mqttsnet.thinglinks.video.manager.sip;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipServerInfoCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.cache.SipServerInfoCacheVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SIP 物理节点信息 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>SipLayer 心跳上报落 Redis，仅纯物理层信息。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipServerInfoManager {

    private final CachePlusOps cachePlusOps;

    public void register(SipServerInfoCacheVO cacheVO) {
        if (cacheVO == null || cacheVO.getInstanceId() == null) {
            return;
        }
        CacheKey cacheKey = SipServerInfoCacheKeyBuilder.build(cacheVO.getInstanceId());
        cachePlusOps.set(cacheKey, cacheVO);
        log.debug("[SIP Redis] 节点注册/续期成功: instanceId={}", cacheVO.getInstanceId());
    }
}
