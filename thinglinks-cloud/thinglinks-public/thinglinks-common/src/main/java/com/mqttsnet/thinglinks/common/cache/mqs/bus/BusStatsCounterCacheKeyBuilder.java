package com.mqttsnet.thinglinks.common.cache.mqs.bus;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 协议总线指标计数器缓存 key 构造器 ── Redis Hash 桶 + 维度 field 模式,集群天然一致。
 *
 * <h3>Key 设计</h3>
 * <pre>
 *   bus:{tenantId}:stats_counter:bucket:obj:{date}:{dimension}
 * </pre>
 *
 * <ul>
 *   <li>{@code date} ── 日期(yyyyMMdd)分桶,自动按天归档</li>
 *   <li>{@code dimension} ── 维度名称:dispatch_total / stage_executions / no_route / canonicalize_fail / relay_send 等</li>
 *   <li>field ── 子标签拼接,如 {@code MQTT:PUBLISH:DEVICE_DATA:SUCCESS}</li>
 *   <li>value ── Long 计数</li>
 * </ul>
 *
 * <h3>租户隔离</h3>
 * 工厂方法内部 {@link ContextUtil#getTenantId()} 自动注入当前租户 ID;
 * {@link CacheKeyBuilder} 在拼接 key 时把 tenant 段插到 modular 之前。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public class BusStatsCounterCacheKeyBuilder implements CacheKeyBuilder {
    /**
     * 构造 hash bucket key(整桶定位用,适合 hGetAll / del 整桶)。
     *
     * @param date      日期(yyyyMMdd)
     * @param dimension 维度名(如 dispatch_total / stage_executions)
     * @return CacheKey
     */
    public static CacheKey builder(String date, String dimension) {
        return new BusStatsCounterCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(date, dimension);
    }

    /**
     * 构造 hash field key(精确到单 label 组合,适合 hIncrBy 单条递增)。
     *
     * @param date      日期(yyyyMMdd)
     * @param dimension 维度名
     * @param labelKey  子标签拼接(如 MQTT:PUBLISH:DEVICE_DATA:SUCCESS)
     * @return CacheHashKey
     */
    public static CacheHashKey buildHashFieldKey(String date, String dimension, String labelKey) {
        return new BusStatsCounterCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .hashFieldKey(labelKey, date, dimension);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Mqs.BUS_STATS_COUNTER;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.MQS;
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
        // 30 天滚动 ── 每日自动归档,集群无需主动清理
        return Duration.ofDays(30);
    }
}
