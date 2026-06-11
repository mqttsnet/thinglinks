package com.mqttsnet.thinglinks.video.manager.sip;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.subscribe.SubscribeHolderCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.SubscribeHolderCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SIP Subscribe 订阅 KV Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>使用 Redis Key-Value 存储 {@link SubscribeHolderCache}，TTL 替代 DynamicTask 过期调度。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeHolderManager {

    private final CachePlusOps cachePlusOps;

    public void put(String key, SubscribeHolderCache cache) {
        try {
            CacheKey cacheKey = SubscribeHolderCacheKeyBuilder.build(key);
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[SubscribeHolder] Redis 写入失败: key={}", key, e);
        }
    }

    /**
     * 按 key 读取订阅缓存。
     *
     * @param key 业务键（含 catalog_/mobilePosition_ 前缀）
     * @return 命中返 {@code Optional}; 缓存中无或 Redis 异常返 {@link Optional#empty()}
     */
    public Optional<SubscribeHolderCache> get(String key) {
        try {
            CacheKey cacheKey = SubscribeHolderCacheKeyBuilder.build(key);
            CacheResult<Object> result = cachePlusOps.get(cacheKey);
            if (result != null && result.getRawValue() != null) {
                return Optional.ofNullable(BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), SubscribeHolderCache.class));
            }
        } catch (Exception e) {
            log.warn("[SubscribeHolder] Redis 读取失败: key={}", key, e);
        }
        return Optional.empty();
    }

    public void remove(String key) {
        try {
            CacheKey cacheKey = SubscribeHolderCacheKeyBuilder.build(key);
            cachePlusOps.del(cacheKey);
        } catch (Exception e) {
            log.warn("[SubscribeHolder] Redis 删除失败: key={}", key, e);
        }
    }
}
