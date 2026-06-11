package com.mqttsnet.thinglinks.config.acl;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.entity.config.AuthProviderConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 * ACL 缓存配置.
 * <p>★ 改用 {@link AsyncCache}: 自动 dedup 并发 miss 同一 key 的缓存击穿,
 * Caffeine 内部把 in-flight CompletableFuture 共享给后续请求,只跑一次 loader.
 *
 * @author Sun ShiHuan
 * @version 1.0.5
 * @since 2025/6/17
 */
@Slf4j
public final class AclCacheConfig {

    private AclCacheConfig() {
    }

    /**
     * 构建 ACL 异步缓存(AsyncCache).
     * <p>对比之前 {@code Cache + getIfPresent/put} 模式的优势:
     * <ul>
     *   <li>并发 miss 同一 key,Caffeine 自动 dedup,仅一次 loader 调用(防缓存击穿)</li>
     *   <li>loader 异常完成时不缓存失败结果,后续请求自动重试</li>
     *   <li>get/put 原子操作,无 race condition</li>
     * </ul>
     */
    public static AsyncCache<CacheKey, Boolean> buildCache(
        AuthProviderConfig.AclConfig.CacheConfig aclCacheConfig,
        Executor executor) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .expireAfterWrite(aclCacheConfig.getExpireMinutes(), TimeUnit.MINUTES)
            .maximumSize(aclCacheConfig.getMaxSize())
            .executor(executor)
            // ★ removalListener 从 INFO 改为 TRACE:1M 容量高频驱逐会日志风暴(每条 INFO = 1 行 log)
            .removalListener((key, value, cause) -> {
                if (log.isTraceEnabled()) {
                    log.trace("ACL cache removed: key={}, reason={}", key, cause);
                }
            });

        if (aclCacheConfig.isRecordStats()) {
            builder.recordStats();
        }

        log.info("ACL 缓存已启用(AsyncCache) - 最大容量: {}, 过期时间: {}分钟",
            aclCacheConfig.getMaxSize(), aclCacheConfig.getExpireMinutes());
        return builder.buildAsync();
    }
}
