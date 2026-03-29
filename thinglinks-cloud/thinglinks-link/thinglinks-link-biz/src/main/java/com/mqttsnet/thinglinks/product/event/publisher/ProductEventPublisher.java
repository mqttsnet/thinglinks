package com.mqttsnet.thinglinks.product.event.publisher;

import cn.hutool.json.JSONUtil;
import com.mqttsnet.thinglinks.product.event.ProductInfoUpdatedEvent;
import com.mqttsnet.thinglinks.product.event.ProductModelUpdatedEvent;
import com.mqttsnet.thinglinks.product.event.source.ProductInfoUpdatedEventSource;
import com.mqttsnet.thinglinks.product.event.source.ProductModelUpdatedEventSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 产品事件发布器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/1/19
 */
@Component
@Slf4j
public class ProductEventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishProductInfoUpdatedEvent(ProductInfoUpdatedEventSource source) {
        log.info("Publishing Product Info Updated event:{}", JSONUtil.toJsonStr(source));
        eventPublisher.publishEvent(new ProductInfoUpdatedEvent(source));
    }

    public void publishProductModelUpdatedEvent(ProductModelUpdatedEventSource source) {
        log.info("Publishing Product Model Updated event:{}", JSONUtil.toJsonStr(source));
        eventPublisher.publishEvent(new ProductModelUpdatedEvent(source));
    }
}
