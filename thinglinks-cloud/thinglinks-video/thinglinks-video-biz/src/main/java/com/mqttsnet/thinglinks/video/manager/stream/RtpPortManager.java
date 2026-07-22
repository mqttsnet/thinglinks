package com.mqttsnet.thinglinks.video.manager.stream;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.thinglinks.common.cache.video.stream.RtpPortPoolCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * RTP 端口池 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>Hash 结构：Key = 流媒体服务器标识，Field = 端口（字符串），Value = "free" 或使用者标识。
 * RTP 端口必须为偶数（RTCP = RTP + 1）。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RtpPortManager {

    private final CachePlusOps cachePlusOps;

    private static final String STATUS_FREE = "free";

    public void initPool(String mediaIdentification, Set<Integer> portSet) {
        CacheHashKey hashKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification);
        cachePlusOps.del(hashKey);
        // HMSET 单命令批量写入，避免几百到几千次单条 hSet 的 round-trip 累积
        Map<String, String> batch = new LinkedHashMap<>(portSet.size() * 2);
        for (Integer port : portSet) {
            batch.put(String.valueOf(port), STATUS_FREE);
        }
        cachePlusOps.hMSet(hashKey, batch);
        log.info("RTP端口池初始化完成: mediaIdentification={}, size={}", mediaIdentification, portSet.size());
    }

    public int allocate(String mediaIdentification, String usedBy) {
        CacheHashKey hashKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            log.warn("RTP端口池为空: mediaIdentification={}", mediaIdentification);
            return -1;
        }

        for (Map.Entry<Object, CacheResult<Object>> entry : entries.entrySet()) {
            String port = String.valueOf(entry.getKey());
            String status = entry.getValue() != null ? String.valueOf(entry.getValue().getRawValue()) : "";
            if (STATUS_FREE.equals(status)) {
                CacheHashKey fieldKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification, port);
                cachePlusOps.hSet(fieldKey, usedBy);
                log.debug("RTP端口分配成功: mediaIdentification={}, port={}, usedBy={}", mediaIdentification, port, usedBy);
                return Integer.parseInt(port);
            }
        }

        log.warn("RTP端口池已耗尽: mediaIdentification={}", mediaIdentification);
        return -1;
    }

    public void release(String mediaIdentification, int port) {
        CacheHashKey fieldKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification, String.valueOf(port));
        cachePlusOps.hSet(fieldKey, STATUS_FREE);
        log.debug("RTP端口释放成功: mediaIdentification={}, port={}", mediaIdentification, port);
    }

    public int getAvailableCount(String mediaIdentification) {
        CacheHashKey hashKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            return 0;
        }
        return (int) entries.values().stream()
                .filter(v -> v != null && STATUS_FREE.equals(String.valueOf(v.getRawValue())))
                .count();
    }

    public int getPoolSize(String mediaIdentification) {
        CacheHashKey hashKey = RtpPortPoolCacheKeyBuilder.builder(mediaIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        return MapUtil.isEmpty(entries) ? 0 : entries.size();
    }
}
