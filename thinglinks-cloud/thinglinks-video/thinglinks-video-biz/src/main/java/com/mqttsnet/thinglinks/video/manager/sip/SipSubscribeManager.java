package com.mqttsnet.thinglinks.video.manager.sip;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipSubscribeCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.SipSubscribeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SIP 事务订阅状态 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>承担 Redis 状态写入（PENDING / 完成结果）。CompletableFuture 等待 + JAIN-SIP 事件由
 * biz-protocol 的 {@code SipSubscribe} 持有。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipSubscribeManager {

    private final CachePlusOps cachePlusOps;

    public void writePending(String key) {
        try {
            CacheKey cacheKey = SipSubscribeCacheKeyBuilder.build(key);
            SipSubscribeCache cache = SipSubscribeCache.builder()
                    .tenantId(ContextUtil.getTenantId())
                    .key(key)
                    .type("PENDING")
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[SIP 订阅] 写入 Redis PENDING 失败: key={}", key, e);
        }
    }

    public void writeResult(String key, String type, int statusCode, String msg, String callId) {
        try {
            CacheKey cacheKey = SipSubscribeCacheKeyBuilder.build(key);
            SipSubscribeCache cache = SipSubscribeCache.builder()
                    .tenantId(ContextUtil.getTenantId())
                    .key(key)
                    .statusCode(statusCode)
                    .type(type)
                    .msg(msg)
                    .callId(callId)
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[SIP 订阅] 写入 Redis 结果失败: key={}", key, e);
        }
    }

    public void delete(String key) {
        try {
            CacheKey cacheKey = SipSubscribeCacheKeyBuilder.build(key);
            cachePlusOps.del(cacheKey);
        } catch (Exception e) {
            log.warn("[SIP 订阅] 删除 Redis 失败: key={}", key, e);
        }
    }
}
