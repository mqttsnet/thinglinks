package com.mqttsnet.thinglinks.product.event.handler;

import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品物模型更新缓存处理器 ── 单产品维度刷新物模型缓存。
 *
 * @author mqttsnet
 * @see ProductModelCacheService#refreshProductModelCache(String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductModelUpdatedCacheHandler {

    private final ProductModelCacheService productModelCacheService;

    /**
     * 处理单产品物模型更新缓存。
     *
     * @param productIdentification 产品标识
     * @return 是否成功处理
     */
    public boolean handleProductModelUpdatedCache(String productIdentification) {
        try {
            ArgumentAssert.notEmpty(productIdentification, "productIdentification is null");
            log.info("处理产品物模型更新缓存...productIdentification:{}", productIdentification);
            productModelCacheService.refreshProductModelCache(productIdentification);
            return true;
        } catch (Exception e) {
            log.error("处理产品物模型更新缓存失败...productIdentification:{}", productIdentification, e);
            return false;
        }
    }
}
