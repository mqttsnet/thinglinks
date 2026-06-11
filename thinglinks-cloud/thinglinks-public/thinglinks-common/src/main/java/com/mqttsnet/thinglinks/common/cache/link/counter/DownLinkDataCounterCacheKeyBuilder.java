package com.mqttsnet.thinglinks.common.cache.link.counter;

import java.io.Serializable;
import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * -----------------------------------------------------------------------------
 * File Name: DownLinkDataCounterCacheKeyBuilder.java
 * -----------------------------------------------------------------------------
 * Description:
 * 下行数据计数器 KEY
 * -----------------------------------------------------------------------------
 * [服务模块名:]业务类型[:业务字段][:value类型][:yyyyMMdd] -> number
 * link:def_down_link_data_counter:id:number:yyyyMMdd -> number
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * <p>
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-23 23:53
 */
public class DownLinkDataCounterCacheKeyBuilder implements CacheKeyBuilder {


    public static CacheHashKey build(Serializable key) {
        return new DownLinkDataCounterCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashKey(key);
    }

    /**
     * @param key   日期 yyyyMMdd
     * @param field 时分 HHmm
     * @return {@link CacheHashKey} hash key
     */
    public static CacheHashKey build(String key, String field) {
        return new DownLinkDataCounterCacheKeyBuilder().setTenantId(ContextUtil.getTenantId()).hashFieldKey(field, key);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Link.DOWN_LINK_DATA_COUNTER;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.LINK;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.number;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(90L);
    }
}
