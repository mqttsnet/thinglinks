package com.mqttsnet.thinglinks.video.manager.isup;

import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.isup.IsupConnCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.isup.IsupConnectionCache;
import com.mqttsnet.thinglinks.video.dto.isup.IsupConnectionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * ISUP 设备连接 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>租户维度 Hash 结构：Field = deviceSerial，Value = {@link IsupConnectionCache}。
 * 调用方必须在调用前已设置 {@code ContextUtil.setTenantId(...)}。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupConnectionManager {

    private final CachePlusOps cachePlusOps;

    public void addConnection(String deviceSerial, IsupConnectionInfo connectionInfo) {
        try {
            CacheHashKey fieldKey = IsupConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), deviceSerial);
            IsupConnectionCache cache = IsupConnectionCache.builder()
                    .deviceSerial(connectionInfo.deviceSerial())
                    .userId(connectionInfo.userId())
                    .channelCount(connectionInfo.channelCount())
                    .ip(connectionInfo.ip())
                    .port(connectionInfo.port())
                    .loginTime(connectionInfo.loginTime())
                    .build();
            cachePlusOps.hSet(fieldKey, cache);
            log.info("ISUP设备连接已添加: deviceSerial={}, ip={}, port={}",
                    deviceSerial, connectionInfo.ip(), connectionInfo.port());
        } catch (Exception e) {
            log.error("ISUP设备连接写入Redis失败: deviceSerial={}", deviceSerial, e);
        }
    }

    public void removeConnection(String deviceSerial) {
        try {
            CacheHashKey fieldKey = IsupConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), deviceSerial);
            cachePlusOps.hDel(fieldKey);
            log.info("ISUP设备连接已移除: deviceSerial={}", deviceSerial);
        } catch (Exception e) {
            log.error("ISUP设备连接删除Redis失败: deviceSerial={}", deviceSerial, e);
        }
    }

    public Optional<IsupConnectionInfo> getConnection(String deviceSerial) {
        try {
            CacheHashKey fieldKey = IsupConnCacheKeyBuilder.builder(ContextUtil.getTenantId(), deviceSerial);
            CacheResult<Object> result = cachePlusOps.hGet(fieldKey);
            if (result != null && result.getRawValue() != null) {
                IsupConnectionCache cache = BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), IsupConnectionCache.class);
                if (cache != null) {
                    return Optional.of(toInfo(cache));
                }
            }
        } catch (Exception e) {
            log.warn("ISUP查询连接失败: deviceSerial={}", deviceSerial, e);
        }
        return Optional.empty();
    }

    public Collection<IsupConnectionInfo> getAllConnections() {
        try {
            CacheHashKey hashKey = IsupConnCacheKeyBuilder.builder(ContextUtil.getTenantId());
            Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
            if (entries == null || entries.isEmpty()) {
                return Collections.emptyList();
            }
            Collection<IsupConnectionInfo> connections = new ArrayList<>(entries.size());
            for (CacheResult<Object> cacheResult : entries.values()) {
                if (cacheResult != null && cacheResult.getRawValue() != null) {
                    IsupConnectionCache cache = BeanPlusUtil.toBeanIgnoreError(cacheResult.getRawValue(), IsupConnectionCache.class);
                    if (cache != null) {
                        connections.add(toInfo(cache));
                    }
                }
            }
            return Collections.unmodifiableCollection(connections);
        } catch (Exception e) {
            log.warn("ISUP查询所有连接失败", e);
            return Collections.emptyList();
        }
    }

    public boolean isConnected(String deviceSerial) {
        return getConnection(deviceSerial).isPresent();
    }

    private IsupConnectionInfo toInfo(IsupConnectionCache cache) {
        return new IsupConnectionInfo(
                cache.getDeviceSerial(),
                cache.getUserId(),
                cache.getChannelCount(),
                cache.getIp(),
                cache.getPort(),
                cache.getLoginTime()
        );
    }
}
