package com.mqttsnet.thinglinks.product.event.source;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 产品基础信息缓存失效事件载荷。
 *
 * @author mqttsnet
 */
@Data
@Builder
public class ProductCacheEvictSource {

    /**
     * 产品标识。
     */
    private String productIdentification;

    /**
     * 上下文快照(tenantId 等)── 供异步监听器恢复租户上下文,
     * 避免 @DS(BASE_TENANT) 切错库 / cacheKey 拼错租户。
     */
    private Map<String, String> contextMap;
}
