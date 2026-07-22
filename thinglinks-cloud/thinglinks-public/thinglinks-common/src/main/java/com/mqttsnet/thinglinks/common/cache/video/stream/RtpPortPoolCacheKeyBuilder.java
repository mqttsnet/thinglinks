package com.mqttsnet.thinglinks.common.cache.video.stream;

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
 * RTP 端口池缓存 Key 构建器。
 * 使用 Hash 结构管理 RTP 端口池：
 * - Key: video:{tenantId}:def_rtp_port_pool:mediaServerId:obj:{mediaServerId}
 * - Field: 端口号
 * - Value: 使用状态（"free" 或使用者信息）
 * <p>
 * RTP 端口池永不过期，由业务逻辑管理生命周期。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public class RtpPortPoolCacheKeyBuilder implements CacheKeyBuilder {

    private Long tenantId;

    /**
     * 构建 RTP 端口池 Hash Key
     *
     * @param mediaServerId 流媒体服务器标识
     * @return Hash 缓存 Key
     */
    public static CacheHashKey builder(Serializable mediaServerId) {
        return new RtpPortPoolCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashKey(mediaServerId);
    }

    /**
     * 构建 RTP 端口池 Hash Field Key
     *
     * @param mediaServerId 流媒体服务器标识
     * @param port          端口号
     * @return Hash 缓存 Key（含 field）
     */
    public static CacheHashKey builder(String mediaServerId, String port) {
        return new RtpPortPoolCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(port, mediaServerId);
    }

    @Override
    public RtpPortPoolCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.RTP_PORT_POOL;
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
        return Duration.ZERO;
    }
}
