package com.mqttsnet.thinglinks.common.cache.link.device;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 设备 ACL 规则 cache key ── Hash 模式:hash key 按产品标识,field 按设备标识。
 * evict 时产品级规则改 DEL 整个 hash(清产品下所有设备),设备级规则改 HDEL 单 field。
 *
 * <p>静态工厂方法必须用 build* 前缀,避开 {@link CacheKeyBuilder} default 实例方法
 * (key / hashKey / hashFieldKey)的同名冲突,否则 overload resolution 选中静态自身导致无限递归 StackOverflowError。
 */
public class DeviceAclRuleCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 整个 hash 的 key,用于 DEL(清产品下所有设备 field)。
     *
     * @param productIdentification 产品标识
     * @return 产品级 hash 缓存 key
     */
    public static CacheKey buildHashKey(String productIdentification) {
        return new DeviceAclRuleCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(productIdentification);
    }

    /**
     * hash field key,用于 HGET / HSET / HDEL(只动该设备字段)。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification 设备标识
     * @return 设备级 hash field 缓存 key
     */
    public static CacheHashKey buildHashFieldKey(String productIdentification, String deviceIdentification) {
        return new DeviceAclRuleCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .hashFieldKey(deviceIdentification, productIdentification);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Link.DEVICE_ACL_RULE;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.LINK;
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
        return Duration.ofDays(7);
    }
}
