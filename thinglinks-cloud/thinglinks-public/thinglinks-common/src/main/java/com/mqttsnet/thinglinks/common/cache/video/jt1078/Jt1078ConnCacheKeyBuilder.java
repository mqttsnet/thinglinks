package com.mqttsnet.thinglinks.common.cache.video.jt1078;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

import java.io.Serializable;
import java.time.Duration;

/**
 * JT/T 1078 连接缓存 Key 构建器（租户维度，Hash 结构）。
 * <p>
 * Key:   lc:video:{tenantId}:def_jt1078_conn:id:obj:{tenantId}
 * Field: simNumber
 * Value: Jt1078ConnectionCache
 * TTL:   2h
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
public class Jt1078ConnCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建 Hash Key（用于 hGetAll / del）
     */
    public static CacheHashKey builder(Serializable tenantKey) {
        return new Jt1078ConnCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .hashKey(tenantKey);
    }

    /**
     * 构建 Hash Field Key（用于 hGet / hSet / hDel）
     */
    public static CacheHashKey builder(Serializable tenantKey, String simNumber) {
        return new Jt1078ConnCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .hashFieldKey(simNumber, tenantKey);
    }

    @Override
    public Jt1078ConnCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return this.tenantId != null ? String.valueOf(this.tenantId) : null;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.JT1078_CONN;
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
        return Duration.ofHours(2);
    }
}
