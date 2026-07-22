package com.mqttsnet.thinglinks.common.cache.video.ssrc;

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
 * SSRC 事务缓存 Key 构建器。
 * 使用 Hash 结构存储 SSRC 事务信息：
 * - Key: video:{tenantId}:def_ssrc_transaction:deviceId:obj:{deviceId}
 * - Field: callId
 * - Value: SsrcTransaction 对象
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class SsrcTransactionCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建 SSRC 事务 Hash Key（根据设备编号）
     *
     * @param deviceId 设备国标编号
     * @return Hash 缓存 Key
     */
    public static CacheHashKey builder(Serializable deviceId) {
        return new SsrcTransactionCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashKey(deviceId);
    }

    /**
     * 构建 SSRC 事务 Hash Field Key
     *
     * @param deviceId 设备国标编号
     * @param callId   SIP 会话的 Call-ID
     * @return Hash 缓存 Key（含 field）
     */
    public static CacheHashKey builder(String deviceId, String callId) {
        return new SsrcTransactionCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(callId, deviceId);
    }

    @Override
    public SsrcTransactionCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SSRC_TRANSACTION;
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
