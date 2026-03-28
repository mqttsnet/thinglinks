package com.mqttsnet.thinglinks.product.event;

import com.mqttsnet.thinglinks.product.event.source.ProductModelUpdatedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 产品物模型更新事件
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/3/27
 */
@Getter
public class ProductModelUpdatedEvent extends ApplicationEvent {
    private final ProductModelUpdatedEventSource eventSource;

    public ProductModelUpdatedEvent(ProductModelUpdatedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
