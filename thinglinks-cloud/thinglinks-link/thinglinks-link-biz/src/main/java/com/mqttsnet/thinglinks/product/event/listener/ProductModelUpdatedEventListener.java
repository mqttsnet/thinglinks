package com.mqttsnet.thinglinks.product.event.listener;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.product.event.ProductModelUpdatedEvent;
import com.mqttsnet.thinglinks.product.event.handler.ProductModelUpdatedCacheHandler;
import com.mqttsnet.thinglinks.product.event.source.ProductModelUpdatedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Description:
 * 产品物模型更新事件监听器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/3/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductModelUpdatedEventListener {

    private final ProductModelUpdatedCacheHandler productModelUpdatedCacheHandler;

    @EventListener
    public CompletableFuture<Boolean> handleProductModelUpdatedEvent(ProductModelUpdatedEvent event) {
        Map<String, String> localMap = ContextUtil.getLocalMap();
        return CompletableFuture.supplyAsync(() -> {
            ContextUtil.setLocalMap(localMap);
            if (event.getSource() instanceof ProductModelUpdatedEventSource source) {
                return productModelUpdatedCacheHandler.handleProductModelUpdatedCache(source.getProductIdentificationList());
            }

            log.warn("无效的事件源类型: {}", event.getSource().getClass());
            return false;
        }).exceptionally(ex -> {
            log.error("处理产品物模型更新事件失败", ex);
            return false;
        });
    }
}
