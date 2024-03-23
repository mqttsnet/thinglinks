package com.mqttsnet.thinglinks.link.common.cache.service;


import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
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
 * File Name: ProductCacheService.java
 * -----------------------------------------------------------------------------
 * Description:
 * Service layer for Product cache management.
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
 * @date 2023-10-21 22:45
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService extends CacheSuperAbstract {
    private final RedisService redisService;

    private final ProductService productService;

    /**
     * Refreshes the product cache for a specific tenant.
     *
     */
    public void refreshProductCacheForTenant() {
        int totalDataCount = productService.findProductTotal().intValue();
        int totalPages = (int) Math.ceil((double) totalDataCount / PAGE_SIZE);

        List<Product> productList = IntStream.range(0, totalPages)
                .mapToObj(this::fetchProductPageContent)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (!productList.isEmpty()) {
            cacheProductsForTenant(productList);
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
     * Caches a list of products for a specific tenant.
     *
     * @param productList List of products to be cached.
     */
    private void cacheProductsForTenant( List<Product> productList) {
        productList.stream()
                .map(this::transformToProductCacheVO)
                .forEach(this::cacheProduct);
    }

    /**
     * Transforms a product object into a ProductCacheVO object.
     *
     * @param product  Product object to be transformed.
     * @return Transformed ProductCacheVO object.
     */
    private ProductCacheVO transformToProductCacheVO( Product product) {
        return BeanPlusUtil.toBeanIgnoreError(product, ProductCacheVO.class);
    }

    /**
     * Caches the ProductCacheVO object.
     *
     * @param productCacheVO ProductCacheVO object to be cached.
     */
    private void cacheProduct(ProductCacheVO productCacheVO) {
        String cacheKey = productCacheVO.getProductIdentification();
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, productCacheVO,THIRTY_MINUTES, TimeUnit.MINUTES);
    }

}
