//package com.mqttsnet.thinglinks.link.common.cache.service;
//
//import cn.hutool.core.bean.BeanUtil;
//import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
//import com.mqttsnet.thinglinks.common.redis.service.RedisService;
//import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
//import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductCacheVO;
//import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
//import com.mqttsnet.thinglinks.link.common.cache.CacheSuperAbstract;
//import com.mqttsnet.thinglinks.link.service.device.DeviceService;
//import com.mqttsnet.thinglinks.link.service.product.ProductService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.cache.CacheKey;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
///**
// * -----------------------------------------------------------------------------
// * File Name: DeviceCacheService.java
// * -----------------------------------------------------------------------------
// * Description:
// * Service layer for Device cache management.
// * -----------------------------------------------------------------------------
// *
// * @author ShiHuan Sun
// * @version 1.0
// * -----------------------------------------------------------------------------
// * Revision History:
// * Date         Author          Version     Description
// * --------      --------     -------   --------------------
// * <p>
// * -----------------------------------------------------------------------------
// * @email 13733918655@163.com
// * @date 2023-10-21 22:42
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DeviceCacheService extends CacheSuperAbstract {
//    private final RedisService redisService;
//    private final DeviceService deviceService;
//    private final ProductService productService;
//
//    /**
//     * Refresh the cache with device data for a specific tenant.
//     *
//     * @param tenantId Identifier of the tenant whose device cache needs to be refreshed.
//     */
//    public void refreshDeviceCacheForTenant(Long tenantId) {
//        int totalDataCount = deviceService.findDeviceTotal().intValue();
//        int totalPages = (int) Math.ceil((double) totalDataCount / PAGE_SIZE);
//
//        List<Device> deviceList = IntStream.range(0, totalPages)
//                .mapToObj(currentPage -> {
//                    Page<Device> page = new Page<>(currentPage, PAGE_SIZE);
//                    HashMap<String, Object> pageHashMap = new HashMap<>();
//                    Device device = new Device();
//                    device.setParams(pageHashMap);
//                    Page<Device> content = deviceService.findPageAll(page, null);
//                    return Optional.ofNullable(content)
//                            .map(Page::getRecords)
//                            .orElse(Collections.emptyList());
//                })
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//
//        cacheDevicesForTenant(tenantId, deviceList);
//    }
//
//    /**
//     * Cache a list of devices for a specific tenant.
//     *
//     * @param tenantId   Identifier of the tenant.
//     * @param deviceList List of devices to be cached.
//     */
//    public void cacheDevicesForTenant(Long tenantId, List<Device> deviceList) {
//        Optional.ofNullable(deviceList)
//                .orElse(Collections.emptyList())
//                .stream()
//                .filter(Objects::nonNull)
//                .map(device -> transformToDeviceCacheVO(tenantId, device))
//                .filter(Objects::nonNull)
//                .forEach(deviceCacheVO -> {
//                    cacheDeviceBasedOnIdentification(deviceCacheVO);
//                    cacheDeviceBasedOnClientId(deviceCacheVO);
//                });
//    }
//
//    /**
//     * Transforms a device object into a DeviceCacheVO object with associated product data.
//     *
//     * @param tenantId Identifier of the tenant.
//     * @param device   Device object to be transformed.
//     * @return Transformed DeviceCacheVO object.
//     */
//    private DeviceCacheVO transformToDeviceCacheVO(Long tenantId, Device device) {
//        DeviceCacheVO deviceCacheVO = BeanUtil.toBeanIgnoreError(device, DeviceCacheVO.class);
//        deviceCacheVO.setTenantId(tenantId);
//
//        Optional.ofNullable(deviceCacheVO.getProductIdentification())
//                .map(productService::selectProductByProductIdentificationList)
//                .ifPresent(product -> {
//                    ProductCacheVO productCacheVO = BeanPlusUtil.toBeanIgnoreError(product, ProductCacheVO.class);
//                    productCacheVO.setTenantId(tenantId);
//                    deviceCacheVO.setProductCacheVO(productCacheVO);
//                });
//
//        return deviceCacheVO;
//    }
//
//    /**
//     * Cache the DeviceCacheVO object based on its identification.
//     *
//     * @param deviceCacheVO DeviceCacheVO object to be cached.
//     */
//    private void cacheDeviceBasedOnIdentification(DeviceCacheVO deviceCacheVO) {
//        CacheKey deviceIdentKey = DeviceCacheKeyBuilder.build(deviceCacheVO.getDeviceIdentification());
//        cachePlusOps.del(deviceIdentKey);
//        cachePlusOps.set(deviceIdentKey, deviceCacheVO);
//    }
//
//    /**
//     * Cache the DeviceCacheVO object based on its client ID.
//     *
//     * @param deviceCacheVO DeviceCacheVO object to be cached.
//     */
//    private void cacheDeviceBasedOnClientId(DeviceCacheVO deviceCacheVO) {
//        CacheKey clientIdKey = DeviceCacheKeyBuilder.build(deviceCacheVO.getClientId());
//        cachePlusOps.del(clientIdKey);
//        cachePlusOps.set(clientIdKey, deviceCacheVO);
//    }
//
//}
