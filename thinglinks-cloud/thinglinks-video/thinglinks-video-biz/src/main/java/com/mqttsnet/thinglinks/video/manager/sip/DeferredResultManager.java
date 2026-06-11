package com.mqttsnet.thinglinks.video.manager.sip;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.sip.DeferredResultCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.DeferredResultCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 异步请求 Redis 信号 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>承担可序列化信号的写入；本地 {@code DeferredResult} 实例由 biz-protocol 的
 * {@code DeferredResultHolder} 自身的 ConcurrentHashMap 持有（{@code DeferredResult} 不可序列化）。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeferredResultManager {

    private final CachePlusOps cachePlusOps;

    public void writePending(String commandType, String requestId) {
        try {
            CacheKey cacheKey = DeferredResultCacheKeyBuilder.build(commandType + "_" + requestId);
            DeferredResultCache cache = DeferredResultCache.builder()
                    .commandType(commandType)
                    .requestId(requestId)
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[DeferredResult] Redis PENDING 写入失败: {}/{}", commandType, requestId, e);
        }
    }

    public void writeResult(String commandType, String requestId) {
        try {
            CacheKey cacheKey = DeferredResultCacheKeyBuilder.build(commandType + "_" + requestId);
            DeferredResultCache cache = DeferredResultCache.builder()
                    .commandType(commandType)
                    .requestId(requestId)
                    .resultJson("COMPLETED")
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[DeferredResult] Redis 结果写入失败: {}/{}", commandType, requestId, e);
        }
    }
}
