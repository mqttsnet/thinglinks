package com.mqttsnet.thinglinks.video.manager.hook;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.hook.HookSubscribeCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.Hook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ZLM Hook 订阅 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>Redis Key-Value 存储 Hook 元数据，TTL 5min 自动过期。本地回调注册表（lambda 不可序列化）由
 * {@code HookSubscribe} 自身的 ConcurrentHashMap 持有。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookSubscribeManager {

    private final CachePlusOps cachePlusOps;

    public void put(Hook hook) {
        if (hook == null) {
            return;
        }
        try {
            CacheKey cacheKey = HookSubscribeCacheKeyBuilder.build(hook.toString());
            cachePlusOps.set(cacheKey, hook);
        } catch (Exception e) {
            log.warn("[HookSubscribe] Redis 写入失败: hookKey={}", hook, e);
        }
    }

    public void remove(Hook hook) {
        try {
            CacheKey cacheKey = HookSubscribeCacheKeyBuilder.build(hook.toString());
            cachePlusOps.del(cacheKey);
        } catch (Exception e) {
            log.warn("[HookSubscribe] Redis 删除失败: hookKey={}", hook, e);
        }
    }

    public boolean exists(String hookKey) {
        try {
            CacheKey cacheKey = HookSubscribeCacheKeyBuilder.build(hookKey);
            return cachePlusOps.exists(cacheKey);
        } catch (Exception e) {
            log.warn("[HookSubscribe] Redis 检查存在性失败: hookKey={}", hookKey, e);
            return false;
        }
    }

    public Hook get(String hookKey) {
        try {
            CacheKey cacheKey = HookSubscribeCacheKeyBuilder.build(hookKey);
            CacheResult<Object> result = cachePlusOps.get(cacheKey);
            if (result != null && result.getRawValue() != null) {
                return BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), Hook.class);
            }
        } catch (Exception e) {
            log.warn("[HookSubscribe] Redis 读取失败: hookKey={}", hookKey, e);
        }
        return null;
    }
}
