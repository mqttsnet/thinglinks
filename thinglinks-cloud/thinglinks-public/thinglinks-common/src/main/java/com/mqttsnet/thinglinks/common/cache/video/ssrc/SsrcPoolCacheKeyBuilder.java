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
 * SSRC 池缓存 Key 构建器。
 * 使用 Hash 结构存储 SSRC 池信息：
 * - Key: video:{tenantId}:def_ssrc_pool:mediaServerId:obj:{mediaServerId}
 * - Field: ssrc 值
 * - Value: 使用状态信息
 * <p>
 * SSRC 池永不过期，由业务逻辑管理生命周期。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class SsrcPoolCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建 SSRC 池 Hash Key（根据流媒体服务器标识）
     *
     * @param mediaServerId 流媒体服务器标识
     * @return Hash 缓存 Key
     */
    public static CacheHashKey builder(Serializable mediaServerId) {
        return new SsrcPoolCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashKey(mediaServerId);
    }

    /**
     * 构建 SSRC 池 Hash Field Key
     *
     * @param mediaServerId 流媒体服务器标识
     * @param ssrc          SSRC 值
     * @return Hash 缓存 Key（含 field）
     */
    public static CacheHashKey builder(String mediaServerId, String ssrc) {
        return new SsrcPoolCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(ssrc, mediaServerId);
    }

    @Override
    public SsrcPoolCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SSRC_POOL;
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
        // SSRC 池永不过期
        return Duration.ZERO;
    }
}
