package com.mqttsnet.thinglinks.common.cache.link.product;

import java.time.Duration;

import com.mqttsnet.basic.base.entity.SuperEntity;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.CacheKeyModular;
import com.mqttsnet.thinglinks.common.cache.CacheKeyTable;

/**
 * 产品物模型缓存 KEY ── 按 (productIdentification, version) 双维度切分,value 为 ProductModelCacheVO。
 * 必须按版本切分:product_snapshot_json 是发布后不可变的快照,上报需按设备绑定的 bound_product_version_no
 * 解析对应版本,否则灰度发布时旧版本设备会被新物模型解析导致字段错位 / 类型不匹配。
 * 快照不可变 → 缓存值天然永久有效,无需"发布新版本时 invalidate 旧版本"的逻辑(旧版本须保留供已绑设备使用)。
 *
 * @author mqttsnet
 * @date 2023/5/30 6:45 下午
 */
public class ProductModelCacheKeyBuilder implements CacheKeyBuilder {

    /**
     * 按 (productIdentification, versionNo) 双维度构建缓存 key,唯一入口。
     * 上报路径传 device.boundProductVersionNo,发布事件路径传新发布的 product_version.versionNo。
     *
     * @param productIdentification 产品标识,不能为空
     * @param versionNo             版本序号(系统发布时生成的快照标识),不能为空
     * @return 产品物模型缓存 key
     */
    public static CacheKey build(String productIdentification, String versionNo) {
        return new ProductModelCacheKeyBuilder()
            .setTenantId(ContextUtil.getTenantId())
            .key(productIdentification, versionNo);
    }


    @Override
    public String getTable() {
        return CacheKeyTable.Link.PRODUCT_MODEL;
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
        return ValueType.obj;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(7);
    }
}
