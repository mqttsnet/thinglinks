package com.mqttsnet.thinglinks.product.event.publisher;

import com.mqttsnet.thinglinks.product.event.ProductCacheEvictEvent;
import com.mqttsnet.thinglinks.product.event.ProductModelChangedEvent;
import com.mqttsnet.thinglinks.product.event.source.ProductCacheEvictSource;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 产品事件发布器。
 *
 * @author mqttsnet
 */
@Component
@Slf4j
public class ProductEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 发布产品物模型变更事件。
     *
     * @param source 物模型变更事件源
     */
    public void publishProductModelChangedEvent(ProductModelChangedSource source) {
        log.info("Publishing ProductModelChanged event: productIdentification={} changeType={} targetType={}",
                source.getProductIdentification(), source.getChangeType(), source.getTargetType());
        eventPublisher.publishEvent(new ProductModelChangedEvent(source));
    }

    /**
     * 发布产品基础信息缓存失效事件(发布切版本 / 回滚后用,只失效缓存)。
     *
     * @param source 缓存失效事件源
     */
    public void publishProductCacheEvictEvent(ProductCacheEvictSource source) {
        eventPublisher.publishEvent(new ProductCacheEvictEvent(source));
    }
}
