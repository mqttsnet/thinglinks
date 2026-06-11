package com.mqttsnet.thinglinks.common.cache.video.sip;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;
import com.mqttsnet.thinglinks.common.constant.BizConstant;

/**
 * Description:
 * 租户 SIP 配置全局 Hash 缓存 Key 构建器。
 * <p>
 * 参考 DictCacheKeyBuilder 模式: getTenant() = null（全局 key，不含租户 ID）。
 * <p>
 * Key:   lc:video:def_sip_tenant_config:id:string:all
 * Field: {sipId}
 * Value: TenantSipConfigCacheVO { tenantId, sipId, sipDomain, sipPassword, ... }
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
public class SipTenantConfigCacheKeyBuilder implements CacheKeyBuilder {

    public static CacheKey buildKey() {
        return new SipTenantConfigCacheKeyBuilder().hashKey(BizConstant.ALL);
    }

    public static CacheHashKey buildHash(String sipId) {
        return new SipTenantConfigCacheKeyBuilder().hashFieldKey(sipId, BizConstant.ALL);
    }

    public static CacheHashKey buildHash() {
        return new SipTenantConfigCacheKeyBuilder().hashFieldKey("", BizConstant.ALL);
    }

    @Override
    public String getTenant() {
        return null;  // 全局，不含租户 ID
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SIP_TENANT_CONFIG;
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
        return ValueType.string;
    }
}
