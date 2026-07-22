package com.mqttsnet.thinglinks.video.manager.stream;

import cn.hutool.core.util.NumberUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.video.stream.StreamRecoveryCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流断流重试计数 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>跨多实例共享重试状态，替代内存 ConcurrentHashMap。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamRecoveryCounterManager {

    private final CachePlusOps cachePlusOps;

    public int get(String key) {
        try {
            CacheKey cacheKey = StreamRecoveryCacheKeyBuilder.build(key);
            CacheResult<Object> result = cachePlusOps.get(cacheKey);
            if (result != null && result.getRawValue() != null) {
                return NumberUtil.parseInt(result.getRawValue().toString(), 0);
            }
        } catch (Exception e) {
            log.warn("[流恢复] 读取 Redis 重试计数失败: key={}", key, e);
        }
        return 0;
    }

    public void set(String key, int count) {
        try {
            CacheKey cacheKey = StreamRecoveryCacheKeyBuilder.build(key);
            cachePlusOps.set(cacheKey, count);
        } catch (Exception e) {
            log.warn("[流恢复] 写入 Redis 重试计数失败: key={}", key, e);
        }
    }

    public void delete(String key) {
        try {
            CacheKey cacheKey = StreamRecoveryCacheKeyBuilder.build(key);
            cachePlusOps.del(cacheKey);
        } catch (Exception e) {
            log.warn("[流恢复] 删除 Redis 重试计数失败: key={}", key, e);
        }
    }
}
