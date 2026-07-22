package com.mqttsnet.thinglinks.common.cache.video.sip;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * SIP 服务信息 Redis 缓存 Key 构建器
 * <p>
 * 每个 video-server 实例启动 SipLayer 后注册自身信息到 Redis，
 * 设置 TTL 并由本地轻量定时任务续期。
 * </p>
 * <p>
 * SIP 信息不区分租户，因此 tenantId 固定为 null。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
public class SipServerInfoCacheKeyBuilder implements CacheKeyBuilder {

    /** 默认过期时间 120 秒，心跳续期间隔 60 秒 */
    private static final Duration DEFAULT_EXPIRE = Duration.ofSeconds(120);

    private SipServerInfoCacheKeyBuilder() {
    }

    /**
     * 构建 SIP 服务信息缓存 Key
     *
     * @param instanceId 实例唯一标识（如 IP:PORT）
     * @return CacheKey
     */
    public static CacheKey build(String instanceId) {
        return new SipServerInfoCacheKeyBuilder().key(instanceId);
    }

    @Override
    public SipServerInfoCacheKeyBuilder setTenantId(Long tenantId) {
        // SIP 信息不区分租户
        return this;
    }

    @Override
    public String getTenant() {
        return null;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.SIP_SERVER_INFO;
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
        return DEFAULT_EXPIRE;
    }
}
