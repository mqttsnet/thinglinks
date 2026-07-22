package com.mqttsnet.thinglinks.product.event.handler;

import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.cache.product.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品信息更新缓存处理器 ── 单产品维度刷新产品信息缓存。
 *
 * @author mqttsnet
 * @see ProductCacheService#refreshProductCache(String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInfoUpdatedCacheHandler {

    private final ProductCacheService productCacheService;

    /**
     * 处理单产品信息更新缓存。
     *
     * @return 是否成功处理
     */
    public boolean handleProductUpdatedCache(String productIdentification) {
        try {
            ArgumentAssert.notEmpty(productIdentification, "productIdentification is null");
            log.info("处理产品信息更新缓存...productIdentification:{}", productIdentification);
            productCacheService.refreshProductCache(productIdentification);
            return true;
        } catch (Exception e) {
            log.error("处理产品信息更新缓存失败...productIdentification:{}", productIdentification, e);
            return false;
        }
    }
}
