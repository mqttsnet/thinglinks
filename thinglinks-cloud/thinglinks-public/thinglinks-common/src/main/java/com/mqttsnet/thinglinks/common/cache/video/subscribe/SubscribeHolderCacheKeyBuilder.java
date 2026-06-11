package com.mqttsnet.thinglinks.common.cache.video.subscribe;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * SIP Subscribe 订阅持有者缓存 Key 构建器（租户维度）。
 * <p>
 * Key:   lc:video:{tenantId}:def_subscribe_holder:id:obj:{type_platformId}
 * TTL:   动态（取决于订阅 expires）
 * <p>
 * 注意：此构建器使用默认 TTL 1h，实际写入时根据 SubscribeInfo.expires 覆盖。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
public class SubscribeHolderCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * @param typePlatformId 类型+平台ID，如 "catalog_123" 或 "mobilePosition_456"
     */
    public static CacheKey build(String typePlatformId) {
        return new SubscribeHolderCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .key(typePlatformId);
    }

    @Override
    public SubscribeHolderCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return this.tenantId != null ? String.valueOf(this.tenantId) : null;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SUBSCRIBE_HOLDER;
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
        return Duration.ofHours(1);
    }
}
