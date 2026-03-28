package com.mqttsnet.thinglinks.product.event.handler;

import java.util.List;

import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 产品物模型更新缓存处理器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/3/27
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductModelUpdatedCacheHandler {

    private final ProductModelCacheService productModelCacheService;


    /**
     * 处理产品物模型更新缓存
     *
     * <p>刷新指定产品标识集合的物模型缓存</p>
     *
     * @param productIdentificationList 产品标识集合
     * @return 是否成功处理
     * @see ProductModelCacheService#refreshProductModelCache(String)
     */
    public boolean handleProductModelUpdatedCache(List<String> productIdentificationList) {
        try {
            ArgumentAssert.notEmpty(productIdentificationList, "productIdentificationList is null");
            log.info("处理产品物模型更新缓存...productIdentificationList:{}", productIdentificationList);
            // 刷新产品物模型缓存
            productIdentificationList.forEach(productModelCacheService::refreshProductModelCache);
            return true;
        } catch (Exception e) {
            log.error("处理产品物模型更新缓存失败...productIdentificationList:{}", productIdentificationList, e);
            return false;
        }
    }

}
