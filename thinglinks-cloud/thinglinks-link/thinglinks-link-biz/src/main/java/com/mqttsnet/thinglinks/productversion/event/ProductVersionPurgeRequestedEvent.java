package com.mqttsnet.thinglinks.productversion.event;

import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import org.springframework.context.ApplicationEvent;

/**
 * 产品物模型版本历史清理事件,监听方负责异步 DROP STABLE。
 *
 * @author mqttsnet
 */
public class ProductVersionPurgeRequestedEvent extends ApplicationEvent {

    public ProductVersionPurgeRequestedEvent(ProductVersionLifecycleEventSource source) {
        super(source);
    }

    @Override
    public ProductVersionLifecycleEventSource getSource() {
        return (ProductVersionLifecycleEventSource) super.getSource();
    }
}
