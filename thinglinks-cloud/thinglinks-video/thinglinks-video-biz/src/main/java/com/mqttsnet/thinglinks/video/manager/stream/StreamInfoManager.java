package com.mqttsnet.thinglinks.video.manager.stream;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.thinglinks.common.cache.video.stream.StreamInfoCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流信息 Manager（biz 数据持久化层 — 缓存原始操作）。
 * <p>Hash 结构：Key = 设备国标编号，Field = channelIdentification_streamType，Value = StreamInfo。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamInfoManager {

    private final CachePlusOps cachePlusOps;

    public void put(String deviceIdentification, String channelIdentification, String streamType, StreamInfo streamInfo) {
        String fieldKey = StreamInfoCacheKeyBuilder.buildFieldKey(channelIdentification, streamType);
        CacheHashKey hashFieldKey = StreamInfoCacheKeyBuilder.builder(deviceIdentification, fieldKey);
        cachePlusOps.hSet(hashFieldKey, streamInfo);
        log.debug("保存流信息: deviceIdentification={}, fieldKey={}", deviceIdentification, fieldKey);
    }

    public Optional<StreamInfo> get(String deviceIdentification, String channelIdentification, String streamType) {
        String fieldKey = StreamInfoCacheKeyBuilder.buildFieldKey(channelIdentification, streamType);
        CacheHashKey hashFieldKey = StreamInfoCacheKeyBuilder.builder(deviceIdentification, fieldKey);
        CacheResult<Object> result = cachePlusOps.hGet(hashFieldKey);
        if (result == null || result.getRawValue() == null) {
            return Optional.empty();
        }
        Object rawValue = result.getRawValue();
        if (rawValue instanceof StreamInfo info) {
            return Optional.of(info);
        }
        log.warn("流信息类型不匹配: deviceIdentification={}, fieldKey={}, type={}", deviceIdentification, fieldKey, rawValue.getClass());
        return Optional.empty();
    }

    public void remove(String deviceIdentification, String channelIdentification, String streamType) {
        String fieldKey = StreamInfoCacheKeyBuilder.buildFieldKey(channelIdentification, streamType);
        CacheHashKey hashFieldKey = StreamInfoCacheKeyBuilder.builder(deviceIdentification, fieldKey);
        cachePlusOps.hDel(hashFieldKey);
        log.debug("删除流信息: deviceIdentification={}, fieldKey={}", deviceIdentification, fieldKey);
    }

    public List<StreamInfo> getAll(String deviceIdentification) {
        CacheHashKey hashKey = StreamInfoCacheKeyBuilder.builder(deviceIdentification);
        Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
        if (MapUtil.isEmpty(entries)) {
            return Collections.emptyList();
        }
        List<StreamInfo> result = new ArrayList<>();
        for (CacheResult<Object> cacheResult : entries.values()) {
            if (cacheResult != null && cacheResult.getRawValue() instanceof StreamInfo info) {
                result.add(info);
            }
        }
        return result;
    }

    public void removeAll(String deviceIdentification) {
        CacheHashKey hashKey = StreamInfoCacheKeyBuilder.builder(deviceIdentification);
        cachePlusOps.del(hashKey);
        log.debug("删除设备所有流信息: deviceIdentification={}", deviceIdentification);
    }
}
