package com.mqttsnet.thinglinks.productversion.event;

import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import org.springframework.context.ApplicationEvent;

/**
 * 产品物模型版本回滚事件。
 *
 * @author mqttsnet
 */
public class ProductVersionRolledBackEvent extends ApplicationEvent {

    public ProductVersionRolledBackEvent(ProductVersionLifecycleEventSource source) {
        super(source);
    }

    @Override
    public ProductVersionLifecycleEventSource getSource() {
        return (ProductVersionLifecycleEventSource) super.getSource();
    }
}
