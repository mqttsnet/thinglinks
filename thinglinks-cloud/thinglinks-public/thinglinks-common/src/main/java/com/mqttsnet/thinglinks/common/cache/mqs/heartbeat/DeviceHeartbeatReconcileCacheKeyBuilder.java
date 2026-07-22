package com.mqttsnet.thinglinks.common.cache.mqs.heartbeat;

import java.time.Duration;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 设备心跳补偿节流缓存 key 构造器.
 * <p>同一 clientId 60s 内最多触发一次 status 修正写,避免高频 PING 把 DB 写穿;
 * 节流落空时 {@code DeviceHeartbeatStage} 调 facade 把 status 强写为 ONLINE,自愈 link 偶发写失败.
 *
 * <h3>Key 格式</h3>
 * <pre>
 *   mqs:{tenantId}:heartbeat_reconcile:id:string:{clientId} → "1"
 * </pre>
 *
 * @author mqttsnet
 * @since 2026-05-17
 */
public class DeviceHeartbeatReconcileCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 构造节流 key.
     *
     * @param clientId 设备 clientId
     * @return {@link CacheKey} key,SET 后 60s 自动过期
     */
    public static CacheKey build(String clientId) {
        return new DeviceHeartbeatReconcileCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(clientId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Mqs.HEARTBEAT_RECONCILE;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.MQS;
    }

    @Override
    public String getField() {
        return "id";
    }

    @Override
    public ValueType getValueType() {
        return ValueType.string;
    }

    /**
     * TTL = 60s ── 节流窗口,期间设备每个 PING 都跳过 reconcile,期满后下一个 PING 重新触发.
     */
    @Override
    public Duration getExpire() {
        return Duration.ofSeconds(60);
    }
}
