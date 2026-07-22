package com.mqttsnet.thinglinks.common.cache.video.sip;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * SIP 事务订阅缓存 Key 构建器（租户维度）。
 * <p>
 * Key:   lc:video:{tenantId}:def_sip_subscribe:id:obj:{callId_cSeq}
 * Value: SipSubscribeCache {tenantId, deviceId, channelId, status, responseCode, errorMsg}
 * TTL:   30s（SIP 事务超时）
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
public class SipSubscribeCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建 Key（不含 field，用于 DEL 等操作）
     */
    public static CacheKey build(String callIdCSeq) {
        return new SipSubscribeCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(callIdCSeq);
    }

    /**
     * 构建完整 Key
     */
    public static CacheKey buildKey(String callIdCSeq) {
        return new SipSubscribeCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(callIdCSeq);
    }

    @Override
    public SipSubscribeCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return this.tenantId != null ? String.valueOf(this.tenantId) : null;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SIP_SUBSCRIBE;
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
        return Duration.ofSeconds(30);
    }
}
