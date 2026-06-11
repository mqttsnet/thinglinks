package com.mqttsnet.thinglinks.product.event;

import com.mqttsnet.thinglinks.product.event.source.ProductCacheEvictSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 产品基础信息缓存失效事件。产品基础元数据变更(发布切版本 / 回滚)后发布。
 * 与 ProductModelChangedEvent 区分:本事件只触发缓存失效,不记变更日志、不刷草稿快照。
 *
 * @author mqttsnet
 */
@Getter
public class ProductCacheEvictEvent extends ApplicationEvent {

    private final transient ProductCacheEvictSource eventSource;

    public ProductCacheEvictEvent(ProductCacheEvictSource source) {
        super(source);
        this.eventSource = source;
    }
}
