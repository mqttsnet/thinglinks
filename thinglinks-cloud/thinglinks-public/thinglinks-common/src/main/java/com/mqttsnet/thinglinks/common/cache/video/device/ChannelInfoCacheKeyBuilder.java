package com.mqttsnet.thinglinks.common.cache.video.device;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * <p>
 * 通道信息 KEY
 * <p>
 * [服务模块名:]业务类型[:业务字段][:value类型][:ID] -> obj
 * video:def_device_channel:channelIdentification:obj:{channelIdentification} -> {}
 *
 * @author mqttsnet
 * @date 2026-04-17
 */
public class ChannelInfoCacheKeyBuilder implements CacheKeyBuilder {
    private Long tenantId;

    /**
     * @param channelIdentification 通道唯一标识
     * @return {@link CacheKey} key
     */
    public static CacheKey build(String channelIdentification) {
        return new ChannelInfoCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).key(channelIdentification);
    }

    @Override
    public ChannelInfoCacheKeyBuilder setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String getTenant() {
        return String.valueOf(this.tenantId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Video.DEVICE_CHANNEL;
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
