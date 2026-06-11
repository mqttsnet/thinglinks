package com.mqttsnet.thinglinks.common.cache.rule.bridge;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;


/**
 * 数据桥接-数据源 缓存 key 构造器。
 *
 * <p>命中场景:{@code SinkDispatcher.dispatch} 热路径每条消息按 {@code dataSourceId} 取数据源,
 * 缓存 miss 时穿透 DB;cache hit 0 IO。{@code DataSourceServiceImpl} 在 update / delete 时
 * 主动 {@code cachePlusOps.del} 失效。</p>
 *
 * <p><b>Key 设计</b>(对齐项目内 {@code DeviceAclRuleCacheKeyBuilder} 等模式):
 * <pre>
 *   rule:{tenantId}:data_source:id:obj:{dataSourceId}
 * </pre>
 * 工厂方法内部 {@code setTenantId(ContextUtil.getTenantId())} 自动注入当前租户 ID,
 * {@link CacheKeyBuilder} 框架拼 key 时把 tenant 段放在 modular 之前,调用方只需传业务字段。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public class DataSourceCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    public static CacheKey builder(Long dataSourceId) {
        return new DataSourceCacheKeyBuilder()
                .setTenantId(ContextUtil.getTenantId())
                .key(dataSourceId);
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public DataSourceCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Rule.DATA_SOURCE;
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
