package com.mqttsnet.thinglinks.common.cache.rule.bridge;


import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 桥接规则(启用列表) 缓存 key 构造器 ── Redis Hash 桶 + ruleCode field 模式。
 *
 * <p><b>Hash bucket key</b>(同 (tenantId+appId+direction) 维度共享一个 hash):
 * <pre>
 *   rule:{tenantId}:data_bridge_rule:id:obj:{appId}:{direction}
 * </pre>
 * <p><b>Hash field</b>: ruleCode(单条桥接规则的业务编码)
 * <p><b>Hash value</b>: DataBridge 实体
 *
 * <p><b>租户注入</b>:工厂 {@link #builder} / {@link #buildHashFieldKey} 内部
 * {@code setTenantId(ContextUtil.getTenantId())} 自动从当前线程上下文取租户 ID;
 * {@link CacheKeyBuilder} 框架在拼接 key 时自动把租户段插到 modular 之前,
 * 调用方只需传业务维度(appId / direction / ruleCode),无需手动拼 tenantId。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public class BridgeRuleEnabledCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构造 hash bucket key(整桶定位用,适合 hGetAll / del 整桶)。
     *
     * @param appId     应用 ID(必填)
     * @param direction 方向 10/20(必填)
     * @return CacheKey,bucket 维度,无 hash field
     */
    public static CacheKey builder(String appId, String direction) {
        return new BridgeRuleEnabledCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .key(appId, direction);
    }

    /**
     * 构造 hash field key(精确到单条规则,适合 hDel / hGet 单 field)。
     *
     * @param appId     应用 ID(必填,bucket 维度)
     * @param direction 方向 10/20(必填,bucket 维度)
     * @param ruleCode  桥接规则业务编码(必填,hash field)
     * @return CacheHashKey 持有 bucket key + ruleCode field
     */
    public static CacheHashKey buildHashFieldKey(String appId, String direction, String ruleCode) {
        return new BridgeRuleEnabledCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .hashFieldKey(ruleCode, appId, direction);
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public BridgeRuleEnabledCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Rule.DATA_BRIDGE_RULE;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.RULE;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.obj;
    }
}
