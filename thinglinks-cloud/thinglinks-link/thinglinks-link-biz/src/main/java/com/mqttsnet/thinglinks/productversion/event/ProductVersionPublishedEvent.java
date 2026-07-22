package com.mqttsnet.thinglinks.productversion.event;

import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import org.springframework.context.ApplicationEvent;

/**
 * 产品物模型版本发布事件。
 *
 * <p>由 ProductVersionServiceImpl#publish 在事务提交后发布;
 * 监听方负责异步执行 TD DDL 同步并回写
 * {@link com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord} 状态。</p>
 *
 * @author mqttsnet
 */
public class ProductVersionPublishedEvent extends ApplicationEvent {

    public ProductVersionPublishedEvent(ProductVersionLifecycleEventSource source) {
        super(source);
    }

    @Override
    public ProductVersionLifecycleEventSource getSource() {
        return (ProductVersionLifecycleEventSource) super.getSource();
    }
}
