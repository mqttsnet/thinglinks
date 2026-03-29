package com.mqttsnet.thinglinks.product.event.listener;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.product.event.ProductInfoUpdatedEvent;
import com.mqttsnet.thinglinks.product.event.handler.ProductInfoUpdatedCacheHandler;
import com.mqttsnet.thinglinks.product.event.source.ProductInfoUpdatedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Description:
 * 产品信息更新事件监听器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/1/19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductInfoUpdatedEventListener {

    private final ProductInfoUpdatedCacheHandler productInfoUpdatedCacheHandler;

    @EventListener
    public CompletableFuture<Boolean> handleProductInfoUpdatedEvent(ProductInfoUpdatedEvent event) {
        Map<String, String> localMap = ContextUtil.getLocalMap();
        return CompletableFuture.supplyAsync(() -> {
            ContextUtil.setLocalMap(localMap);
            if (event.getSource() instanceof ProductInfoUpdatedEventSource source) {
                return productInfoUpdatedCacheHandler.handleProductUpdatedCache(source.getProductIdentificationList());
            }

            log.warn("无效的事件源类型: {}", event.getSource().getClass());
            return false;
        }).exceptionally(ex -> {
            log.error("处理产品更新事件失败", ex);
            return false;
        });
    }
}
