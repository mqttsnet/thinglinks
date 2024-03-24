package com.mqttsnet.thinglinks.link.controller.cache;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.common.cache.service.DeviceCacheService;
import com.mqttsnet.thinglinks.link.common.cache.service.ProductCacheService;
import com.mqttsnet.thinglinks.link.common.cache.service.ProductModelCacheService;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * -----------------------------------------------------------------------------
 * File Name: CacheOpenAnyController
 * -----------------------------------------------------------------------------
 * Description:
 * 缓存开放接口
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/24       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/3/24 02:00
 */
@RestController
@RequestMapping("/cache/open/any")
@Slf4j
public class CacheOpenAnyController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private DeviceCacheService deviceCacheService;
    @Autowired
    private ProductCacheService productCacheService;
    @Autowired
    private ProductModelCacheService productModelCacheService;
    @Autowired
    private DeviceInfoService deviceInfoService;

    /**
     * Refreshes the cache for all devices within a tenant.
     *
     * @return Response indicating the operation was successful.
     */
    @GetMapping("/refreshAllDeviceCaches")
    public R refreshAllDeviceCaches() {
        log.info("Starting to refresh all device caches.");
        deviceCacheService.refreshDeviceCacheForTenant();
        return R.ok();
    }

    /**
     * Refreshes the cache for sub devices within a tenant.
     *
     * @return Response indicating the operation was successful.
     */
    @GetMapping("/refreshAllDeviceInfoCaches")
    public R refreshAllDeviceInfoCaches() {
        log.info("Starting to refresh all device info caches.");
        deviceInfoService.refreshDeviceInfoDataModel(new ArrayList<>());
        return R.ok();
    }

    /**
     * Refreshes the cache for all products within a tenant.
     *
     * @return Response indicating the operation was successful.
     */
    @GetMapping("/refreshAllProductCaches")
    public R refreshAllProductCaches() {
        log.info("Starting to refresh all product caches.");
        productCacheService.refreshProductCacheForTenant();
        return R.ok();
    }

    /**
     * Refreshes the cache for all product models within a tenant.
     *
     * @return Response indicating the operation was successful.
     */
    @GetMapping("/refreshAllProductModelCaches")
    public R refreshAllProductModelCaches() {
        log.info("Starting to refresh all product model caches.");
        productModelCacheService.refreshProductModelCache();
        return R.ok();
    }
}