package com.mqttsnet.thinglinks.common.cache.video.platform;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;
import com.mqttsnet.thinglinks.common.constant.BizConstant;

import java.time.Duration;

/**
 * 平台级联注册缓存 Key 构建器（全局 Hash，非租户维度）。
 * <p>
 * 原因：@Scheduled 续期/心跳在无租户上下文环境下运行，需要遍历所有租户的注册平台。
 * 参考 SipTenantConfigCacheKeyBuilder（getTenant() = null）。
 * <p>
 * Key:   lc:video:def_platform_register:id:obj:all
 * Field: platformId
 * Value: PlatformRegisterCache（含 tenantId 字段，用于恢复租户上下文）
 * TTL:   10min
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
public class PlatformRegisterCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 构建 Hash Key（用于 hGetAll / del）
     */
    public static CacheKey buildKey() {
        return new PlatformRegisterCacheKeyBuilder().hashKey(BizConstant.ALL);
    }

    /**
     * 构建 Hash Field Key（用于 hGet / hSet / hDel）
     */
    public static CacheHashKey buildHash(Long platformId) {
        return new PlatformRegisterCacheKeyBuilder().hashFieldKey(platformId, BizConstant.ALL);
    }

    @Override
    public String getTenant() {
        return null;  // 全局，不含租户 ID
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.PLATFORM_REGISTER;
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
        return Duration.ofMinutes(10);
    }
}
