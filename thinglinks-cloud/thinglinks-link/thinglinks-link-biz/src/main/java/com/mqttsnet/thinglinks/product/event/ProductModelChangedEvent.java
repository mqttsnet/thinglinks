package com.mqttsnet.thinglinks.product.event;

import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 产品物模型变更事件。产品基础信息 / 服务 / 属性 / 命令 的每次 CRUD 持久化都发一条,
 * 消费方落变更记录、刷草稿快照、刷产品 / 物模型缓存。
 *
 * @author mqttsnet
 */
@Getter
public class ProductModelChangedEvent extends ApplicationEvent {

    private final ProductModelChangedSource eventSource;

    public ProductModelChangedEvent(ProductModelChangedSource source) {
        super(source);
        this.eventSource = source;
    }
}
