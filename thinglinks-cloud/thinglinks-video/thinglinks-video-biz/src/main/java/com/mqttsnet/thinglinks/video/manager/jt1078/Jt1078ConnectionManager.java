package com.mqttsnet.thinglinks.video.manager.jt1078;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.jt1078.Jt1078ConnCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.jt1078.Jt1078ConnectionCache;
import com.mqttsnet.thinglinks.video.dto.jt1078.Jt1078ConnectionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * JT/T 1078 终端连接 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>租户维度 Hash 结构：Field = simNumber，Value = {@link Jt1078ConnectionCache}。
 * 调用方必须在调用前已设置 {@code ContextUtil.setTenantId(...)}。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Jt1078ConnectionManager {

    private final CachePlusOps cachePlusOps;

    public void online(Jt1078ConnectionInfo connectionInfo) {
        try {
            CacheHashKey fieldKey = Jt1078ConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), connectionInfo.simNumber());
            Jt1078ConnectionCache cache = Jt1078ConnectionCache.builder()
                    .simNumber(connectionInfo.simNumber())
                    .plateNumber(connectionInfo.plateNumber())
                    .channelCount(connectionInfo.channelCount())
                    .ip(connectionInfo.ip())
                    .port(connectionInfo.port())
                    .loginTime(connectionInfo.loginTime())
                    .build();
            cachePlusOps.hSet(fieldKey, cache);
            log.info("[JT1078] 终端上线: simNumber={}, plateNumber={}, ip={}:{}",
                    connectionInfo.simNumber(), connectionInfo.plateNumber(),
                    connectionInfo.ip(), connectionInfo.port());
        } catch (Exception e) {
            log.error("[JT1078] 终端上线写入Redis失败: simNumber={}", connectionInfo.simNumber(), e);
        }
    }

    public void offline(String simNumber) {
        try {
            CacheHashKey fieldKey = Jt1078ConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), simNumber);
            cachePlusOps.hDel(fieldKey);
            log.info("[JT1078] 终端下线: simNumber={}", simNumber);
        } catch (Exception e) {
            log.error("[JT1078] 终端下线删除Redis失败: simNumber={}", simNumber, e);
        }
    }

    public Optional<Jt1078ConnectionInfo> getConnection(String simNumber) {
        try {
            CacheHashKey fieldKey = Jt1078ConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), simNumber);
            CacheResult<Object> result = cachePlusOps.hGet(fieldKey);
            if (result != null && result.getRawValue() != null) {
                Jt1078ConnectionCache cache = BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), Jt1078ConnectionCache.class);
                if (cache != null) {
                    return Optional.of(toInfo(cache));
                }
            }
        } catch (Exception e) {
            log.warn("[JT1078] 查询连接失败: simNumber={}", simNumber, e);
        }
        return Optional.empty();
    }

    public boolean isOnline(String simNumber) {
        return getConnection(simNumber).isPresent();
    }

    public Collection<Jt1078ConnectionInfo> getAllConnections() {
        try {
            CacheHashKey hashKey = Jt1078ConnCacheKeyBuilder.builder(ContextUtil.getTenantId());
            Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
            if (entries == null || entries.isEmpty()) {
                return Collections.emptyList();
            }
            Collection<Jt1078ConnectionInfo> connections = new ArrayList<>(entries.size());
            for (CacheResult<Object> cacheResult : entries.values()) {
                if (cacheResult != null && cacheResult.getRawValue() != null) {
                    Jt1078ConnectionCache cache = BeanPlusUtil.toBeanIgnoreError(cacheResult.getRawValue(), Jt1078ConnectionCache.class);
                    if (cache != null) {
                        connections.add(toInfo(cache));
                    }
                }
            }
            return Collections.unmodifiableCollection(connections);
        } catch (Exception e) {
            log.warn("[JT1078] 查询所有连接失败", e);
            return Collections.emptyList();
        }
    }

    public int getOnlineCount() {
        return getAllConnections().size();
    }

    private Jt1078ConnectionInfo toInfo(Jt1078ConnectionCache cache) {
        return new Jt1078ConnectionInfo(
                cache.getSimNumber(),
                cache.getPlateNumber(),
                cache.getChannelCount(),
                cache.getIp(),
                cache.getPort(),
                cache.getLoginTime()
        );
    }
}
