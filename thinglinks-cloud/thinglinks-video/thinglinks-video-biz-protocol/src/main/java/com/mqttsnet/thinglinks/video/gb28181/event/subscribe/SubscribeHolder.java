package com.mqttsnet.thinglinks.video.gb28181.event.subscribe;

import com.mqttsnet.thinglinks.video.dto.gb28181.cache.SubscribeHolderCache;
import com.mqttsnet.thinglinks.video.dto.gb28181.subscribe.SubscribeInfo;
import com.mqttsnet.thinglinks.video.manager.sip.SubscribeHolderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SIP Subscribe 订阅管理（Redis 存储）。
 * <p>
 * 使用 Redis Key-Value 存储 SubscribeInfo 的可序列化子集（SubscribeHolderCache），
 * Redis TTL 替代 DynamicTask 的过期调度。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeHolder {

    private static final String CATALOG_PREFIX = "catalog_";
    private static final String MOBILE_POSITION_PREFIX = "mobilePosition_";

    private final SubscribeHolderManager subscribeHolderManager;

    public void putCatalogSubscribe(String platformId, SubscribeInfo subscribeInfo) {
        putSubscribe(CATALOG_PREFIX + platformId, subscribeInfo);
    }

    public SubscribeInfo getCatalogSubscribe(String platformId) {
        if (platformId == null) {
            return null;
        }
        return getSubscribe(CATALOG_PREFIX + platformId);
    }

    public void removeCatalogSubscribe(String platformId) {
        removeSubscribe(CATALOG_PREFIX + platformId);
    }

    public void putMobilePositionSubscribe(String platformId, SubscribeInfo subscribeInfo, Runnable gpsTask) {
        putSubscribe(MOBILE_POSITION_PREFIX + platformId, subscribeInfo);
    }

    public SubscribeInfo getMobilePositionSubscribe(String platformId) {
        return getSubscribe(MOBILE_POSITION_PREFIX + platformId);
    }

    public void removeMobilePositionSubscribe(String platformId) {
        removeSubscribe(MOBILE_POSITION_PREFIX + platformId);
    }

    public List<String> getAllCatalogSubscribePlatform() {
        // 由于 Redis 不支持按前缀列举（无 SCAN 封装），暂返回空列表
        // 实际使用场景中通常通过已知 platformId 列表查询
        return new ArrayList<>();
    }

    public List<String> getAllMobilePositionSubscribePlatform() {
        return new ArrayList<>();
    }

    public void removeAllSubscribe(String platformId) {
        removeMobilePositionSubscribe(platformId);
        removeCatalogSubscribe(platformId);
    }

    // ==================== 通过 Manager 访问缓存 ====================

    private void putSubscribe(String key, SubscribeInfo subscribeInfo) {
        subscribeHolderManager.put(key, toCache(subscribeInfo));
    }

    private SubscribeInfo getSubscribe(String key) {
        return subscribeHolderManager.get(key).map(this::toInfo).orElse(null);
    }

    private void removeSubscribe(String key) {
        subscribeHolderManager.remove(key);
    }

    private SubscribeHolderCache toCache(SubscribeInfo info) {
        return SubscribeHolderCache.builder()
                .id(info.getId())
                .expires(info.getExpires())
                .eventId(info.getEventId())
                .eventType(info.getEventType())
                .sn(info.getSn())
                .gpsInterval(info.getGpsInterval())
                .simulatedFromTag(info.getSimulatedFromTag())
                .simulatedToTag(info.getSimulatedToTag())
                .simulatedCallId(info.getSimulatedCallId())
                .build();
    }

    private SubscribeInfo toInfo(SubscribeHolderCache cache) {
        SubscribeInfo info = new SubscribeInfo();
        info.setId(cache.getId());
        info.setExpires(cache.getExpires());
        info.setEventId(cache.getEventId());
        info.setEventType(cache.getEventType());
        info.setSn(cache.getSn());
        info.setGpsInterval(cache.getGpsInterval());
        info.setSimulatedFromTag(cache.getSimulatedFromTag());
        info.setSimulatedToTag(cache.getSimulatedToTag());
        info.setSimulatedCallId(cache.getSimulatedCallId());
        return info;
    }
}
