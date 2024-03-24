package com.mqttsnet.thinglinks.link.common.cache.service;

import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.link.common.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProductModelCacheService.java
 * -----------------------------------------------------------------------------
 * Description:
 * Service layer for Product Model cache management.
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-10-21 22:47
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductModelCacheService extends CacheSuperAbstract {

    private final RedisService redisService;
    private final ProductService productService;

    /**
     * Refreshes the product model cache for a specific tenant.
     */
    public void refreshProductModelCache() {
        int totalDataCount = productService.findProductTotal().intValue();
        int totalPages = (int) Math.ceil((double) totalDataCount / PAGE_SIZE);
        List<Product> productList = IntStream.range(0, totalPages)
                .mapToObj(this::fetchProductPageContent)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (!productList.isEmpty()) {
            cacheProductModelsForTenant(productList);
        }
    }

    /**
     * Fetches products from a specific page.
     *
     * @param currentPage The page number to fetch products from.
     * @return List of products from the specified page.
     */
    private List<Product> fetchProductPageContent(int currentPage) {
        int offset = currentPage * PAGE_SIZE;
        List<Product> products = productService.findProductsByPage(offset, PAGE_SIZE);
        return products != null ? products : Collections.emptyList();
    }

    /**
     * Caches a list of product models for a specific tenant.
     *
     * @param productList List of products to be cached as product models.
     */
    private void cacheProductModelsForTenant(List<Product> productList) {
        productList.stream()
                .map(this::transformToProductModelCacheVO)
                .forEach(this::cacheProductModel);
    }

    /**
     * Transforms a product object into a ProductModelCacheVO object.
     *
     * @param product Product object to be transformed.
     * @return Transformed ProductModelCacheVO object.
     */
    private ProductModelCacheVO transformToProductModelCacheVO(Product product) {
        ProductParamVO productParamVO = productService.selectFullProductByProductIdentification(product.getProductIdentification());
        return BeanPlusUtil.toBeanIgnoreError(productParamVO, ProductModelCacheVO.class);
    }

    /**
     * Caches the ProductModelCacheVO object.
     *
     * @param productModelCacheVO ProductModelCacheVO object to be cached.
     */
    private void cacheProductModel(ProductModelCacheVO productModelCacheVO) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL + productModelCacheVO.getProductIdentification();
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, productModelCacheVO, THIRTY_MINUTES, TimeUnit.MINUTES);
    }

}