package com.mqttsnet.thinglinks.video.manager.sip;

import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.sip.MsgSubscribeCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.MsgSubscribeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MESSAGE 消息订阅状态 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>承担 Redis 状态写入（PENDING / 完成结果）。CompletableFuture 等待由 biz-protocol 的
 * {@code MessageSubscribe} 持有。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSubscribeManager {

    private final CachePlusOps cachePlusOps;

    public void writePending(String key, String cmdType, String sn, String deviceId) {
        try {
            CacheKey cacheKey = MsgSubscribeCacheKeyBuilder.build(key);
            MsgSubscribeCache cache = MsgSubscribeCache.builder()
                    .tenantId(ContextUtil.getTenantId())
                    .cmdType(cmdType)
                    .sn(sn)
                    .deviceIdentification(deviceId)
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[MESSAGE 订阅] 写入 Redis PENDING 失败: key={}", key, e);
        }
    }

    public void writeResult(String key, int code, String msg) {
        try {
            CacheKey cacheKey = MsgSubscribeCacheKeyBuilder.build(key);
            MsgSubscribeCache cache = MsgSubscribeCache.builder()
                    .tenantId(ContextUtil.getTenantId())
                    .resultCode(code)
                    .resultMsg(msg)
                    .createdAt(System.currentTimeMillis())
                    .build();
            cachePlusOps.set(cacheKey, cache);
        } catch (Exception e) {
            log.warn("[MESSAGE 订阅] 写入 Redis 结果失败: key={}", key, e);
        }
    }

    public void delete(String key) {
        try {
            CacheKey cacheKey = MsgSubscribeCacheKeyBuilder.build(key);
            cachePlusOps.del(cacheKey);
        } catch (Exception e) {
            log.warn("[MESSAGE 订阅] 删除 Redis 失败: key={}", key, e);
        }
    }
}
