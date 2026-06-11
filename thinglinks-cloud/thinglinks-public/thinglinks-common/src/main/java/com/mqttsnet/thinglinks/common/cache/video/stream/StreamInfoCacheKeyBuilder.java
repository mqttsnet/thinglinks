package com.mqttsnet.thinglinks.common.cache.video.stream;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

import java.io.Serializable;
import java.time.Duration;

/**
 * Description:
 * 流信息缓存 Key 构建器。
 * 使用 Hash 结构存储流信息：
 * - Key: video:{tenantId}:def_stream_info:deviceId:obj:{deviceId}
 * - Field: channelId + "_" + streamType
 * - Value: StreamInfo 对象（序列化后的多协议 URL）
 * <p>
 * 流信息在流关闭时清除，TTL 30 分钟作为兜底保护。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class StreamInfoCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建流信息 Hash Key（根据设备编号）
     *
     * @param deviceId 设备国标编号
     * @return Hash 缓存 Key
     */
    public static CacheHashKey builder(Serializable deviceId) {
        return new StreamInfoCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashKey(deviceId);
    }

    /**
     * 构建流信息 Hash Field Key
     *
     * @param deviceId  设备国标编号
     * @param fieldKey  Field 键（如 channelId_play）
     * @return Hash 缓存 Key（含 field）
     */
    public static CacheHashKey builder(String deviceId, String fieldKey) {
        return new StreamInfoCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(fieldKey, deviceId);
    }

    /**
     * 构建 Field 键
     *
     * @param channelId  通道编号
     * @param streamType 流类型（play/playback/download）
     * @return Field 键字符串
     */
    public static String buildFieldKey(String channelId, String streamType) {
        return channelId + "_" + streamType;
    }

    @Override
    public StreamInfoCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.STREAM_INFO;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.VIDEO;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.obj;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(30);
    }
}
