package com.mqttsnet.thinglinks.video.manager.ssrc;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.thinglinks.common.cache.video.ssrc.SsrcPoolCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * SSRC 池 Redis 缓存管理器。
 * <p>
 * 通过 Hash 结构管理每个流媒体服务器的 SSRC 池：
 * Key 为流媒体服务器标识，Field 为 SSRC 值，Value 为使用状态（"free" 或使用者信息）。
 * SSRC 池在流媒体服务器注册时初始化，分配/释放操作为原子操作，支持多实例共享。
 * <p>
 * 本类只做 Redis 读写，不依赖 SIP 协议栈，因此放在 biz 层，供 biz-protocol / controller / boot-impl（执行器）共用。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsrcPoolManager {

    private final CachePlusOps cachePlusOps;

    private static final String STATUS_FREE = "free";

    /**
     * 初始化指定流媒体服务器的 SSRC 池（批量写入版）。
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param ssrcSet             可用的 SSRC 集合
     */
    public void initPool(String mediaIdentification, Set<String> ssrcSet) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        // 先清空旧数据
        cachePlusOps.del(hashKey);
        // 一次 HMSET 批量写入：相比 ~20000 次单条 hSet（每次一次 round-trip），HMSET 单命令完成
        Map<String, String> batch = new LinkedHashMap<>(ssrcSet.size() * 2);
        for (String ssrc : ssrcSet) {
            batch.put(ssrc, STATUS_FREE);
        }
        cachePlusOps.hMSet(hashKey, batch);
        log.info("SSRC池初始化完成: mediaIdentification={}, size={}", mediaIdentification, ssrcSet.size());
    }

    /**
     * 从池中分配一个可用的 SSRC（任意前缀）。等价于 {@code allocate(mediaId, null, usedBy)}。
     */
    public String allocate(String mediaIdentification, String usedBy) {
        return allocate(mediaIdentification, null, usedBy);
    }

    /**
     * 从池中分配一个可用的 SSRC，可按前缀筛选。
     * <p>
     * 核心实现：一次 hGetAll 读回全池，遍历过程中只选**前缀匹配且 status=free** 的，
     * 一次 hSet 标记为 used。相比之前的"allocate → 不匹配就 release 再重试"循环，
     * Redis round-trip 从 O(N) 降为 O(1)，也解决了 HashMap 迭代顺序稳定导致的死循环。
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param prefixFilter        前缀过滤（null 表示不过滤，否则必须以该前缀开头）
     * @param usedBy              使用者标识（如 deviceIdentification:channelIdentification）
     * @return 分配到的 SSRC，无匹配项返回 null
     */
    public String allocate(String mediaIdentification, String prefixFilter, String usedBy) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            log.warn("SSRC池为空: mediaIdentification={}", mediaIdentification);
            return null;
        }

        for (Map.Entry<Object, CacheResult<Object>> entry : entries.entrySet()) {
            String ssrc = String.valueOf(entry.getKey());
            if (prefixFilter != null && !ssrc.startsWith(prefixFilter)) {
                continue;
            }
            String status = entry.getValue() != null ? String.valueOf(entry.getValue().getRawValue()) : "";
            if (STATUS_FREE.equals(status)) {
                // 标记为已使用（单次 hSet）
                CacheHashKey fieldKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification, ssrc);
                cachePlusOps.hSet(fieldKey, usedBy);
                log.debug("SSRC分配成功: mediaIdentification={}, ssrc={}, prefix={}, usedBy={}",
                        mediaIdentification, ssrc, prefixFilter, usedBy);
                return ssrc;
            }
        }

        if (prefixFilter != null) {
            log.warn("SSRC池中前缀为 {} 的可用 SSRC 已耗尽: mediaIdentification={}", prefixFilter, mediaIdentification);
        } else {
            log.warn("SSRC池已耗尽: mediaIdentification={}", mediaIdentification);
        }
        return null;
    }

    /**
     * 释放指定的 SSRC，归还到池中
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param ssrc          要释放的 SSRC 值
     */
    public void release(String mediaIdentification, String ssrc) {
        CacheHashKey fieldKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification, ssrc);
        cachePlusOps.hSet(fieldKey, STATUS_FREE);
        log.debug("SSRC释放成功: mediaIdentification={}, ssrc={}", mediaIdentification, ssrc);
    }

    /**
     * 查询池中可用 SSRC 数量
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 可用数量
     */
    public int getAvailableCount(String mediaIdentification) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            return 0;
        }
        return (int) entries.values().stream()
                .filter(v -> v != null && STATUS_FREE.equals(String.valueOf(v.getRawValue())))
                .count();
    }

    /**
     * 查询池总大小
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 池总大小
     */
    public int getPoolSize(String mediaIdentification) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        return entries == null ? 0 : entries.size();
    }

    /**
     * 检查指定 SSRC 是否正在使用
     *
     * @param mediaIdentification 流媒体服务器标识
     * @param ssrc          SSRC 值
     * @return 是否正在使用
     */
    public boolean isInUse(String mediaIdentification, String ssrc) {
        CacheHashKey fieldKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification, ssrc);
        var result = cachePlusOps.hGet(fieldKey);
        if (result == null || result.getRawValue() == null) {
            return false;
        }
        return !STATUS_FREE.equals(String.valueOf(result.getRawValue()));
    }

    /**
     * 获取指定流媒体服务器当前所有已分配 SSRC 的 {ssrc → usedBy} 映射。
     * usedBy 格式通常为 "deviceIdentification:channelIdentification"，用于对账定位会话归属。
     *
     * @param mediaIdentification 流媒体服务器标识
     * @return 已分配 SSRC 映射（不含 free 项）；池不存在时返回空 Map
     */
    public Map<String, String> getAllAllocations(String mediaIdentification) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            return Collections.emptyMap();
        }
        Map<String, String> allocated = new HashMap<>();
        for (Map.Entry<Object, CacheResult<Object>> entry : entries.entrySet()) {
            String ssrc = String.valueOf(entry.getKey());
            String value = entry.getValue() != null ? String.valueOf(entry.getValue().getRawValue()) : "";
            if (!STATUS_FREE.equals(value)) {
                allocated.put(ssrc, value);
            }
        }
        return allocated;
    }

    /**
     * 清空并重置指定流媒体服务器的 SSRC 池（管理紧急恢复用）。
     * 调用方需要同步清理对应的 SsrcTransaction，并重新调用 initPool 恢复池容量。
     *
     * @param mediaIdentification 流媒体服务器标识
     */
    public void resetPool(String mediaIdentification) {
        CacheHashKey hashKey = SsrcPoolCacheKeyBuilder.builder(mediaIdentification);
        cachePlusOps.del(hashKey);
        log.warn("SSRC池已重置（清空全部分配）: mediaIdentification={}", mediaIdentification);
    }
}
