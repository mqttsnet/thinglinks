package com.mqttsnet.thinglinks.link.common.cache.service;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.enums.DeviceType;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceInfoCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.common.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 设备缓存处理
 *
 * @author xiaonannet
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceCacheService extends CacheSuperAbstract {
    private final RedisService redisService;
    private final DeviceService deviceService;
    private final DeviceInfoService deviceInfoService;
    private final ProductService productService;


    /**
     * Refresh the cache with device data for a specific tenant.
     */
    public void refreshDeviceCacheForTenant() {
        int totalDataCount = deviceService.findDeviceTotal().intValue();
        int totalPages = (int) Math.ceil((double) totalDataCount / PAGE_SIZE);
        List<Device> deviceList = IntStream.range(0, totalPages).mapToObj(currentPage -> {
                    PageHelper.startPage(currentPage + 1, PAGE_SIZE);
                    return deviceService.findDevices();
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        cacheDevicesForTenant(deviceList);
    }

    /**
     * Cache a list of devices for a specific tenant.
     *
     * @param deviceList List of devices to be cached.
     */
    public void cacheDevicesForTenant(List<Device> deviceList) {
        Optional.ofNullable(deviceList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(this::transformToDeviceCacheVO)
                .forEach(deviceCacheVO -> {
                    cacheDeviceBasedOnIdentification(deviceCacheVO);
                    cacheDeviceBasedOnClientId(deviceCacheVO);
                });
    }

    /**
     * Transforms a device object into a DeviceCacheVO object with associated product data.
     *
     * @param device Device object to be transformed.
     * @return Transformed DeviceCacheVO object.
     */
    public DeviceCacheVO transformToDeviceCacheVO(Device device) {
        DeviceCacheVO deviceCacheVO = BeanUtil.toBeanIgnoreError(device, DeviceCacheVO.class);

        Optional.ofNullable(deviceCacheVO.getProductIdentification())
                .map(productService::findOneByProductIdentification)
                .ifPresent(product -> {
                    ProductCacheVO productCacheVO = BeanPlusUtil.toBeanIgnoreError(product, ProductCacheVO.class);
                    deviceCacheVO.setProductCacheVO(productCacheVO);
                });

        return deviceCacheVO;
    }

    /**
     * Cache the DeviceCacheVO object based on its identification.
     *
     * @param deviceCacheVO DeviceCacheVO object to be cached.
     */
    private void cacheDeviceBasedOnIdentification(DeviceCacheVO deviceCacheVO) {
        String cacheKey = CacheConstants.DEF_DEVICE + deviceCacheVO.getDeviceIdentification();
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, deviceCacheVO, THIRTY_MINUTES, TimeUnit.MINUTES);

        // If the device is a gateway, process and cache sub-devices
        if (DeviceType.GATEWAY.getValue().equals(deviceCacheVO.getDeviceType())) {
            List<DeviceInfo> deviceInfos = deviceInfoService.selectDeviceInfoList(DeviceInfo.builder().did(deviceCacheVO.getId()).build());
            if (!deviceInfos.isEmpty()) {
                deviceInfos.forEach(subDevice -> {
                    // Create DeviceInfoCacheVO directly for sub-device
                    DeviceInfoCacheVO subDeviceCacheVO = transformToDeviceInfoCacheVO(deviceCacheVO, subDevice);

                    // Generate the cache key for the sub-device and store it
                    String subCacheKey = CacheConstants.DEF_DEVICE_INFO + subDeviceCacheVO.getDeviceId();
                    redisService.delete(subCacheKey);
                    redisService.setCacheObject(subCacheKey, subDeviceCacheVO, THIRTY_MINUTES, TimeUnit.MINUTES);
                });
            }
        }
    }

    /**
     * Transforms a DeviceInfo object and parent DeviceCacheVO into a DeviceInfoCacheVO.
     *
     * @param parentDeviceCacheVO The parent DeviceCacheVO object.
     * @param deviceInfo          The DeviceInfo object representing the sub-device.
     * @return Transformed DeviceInfoCacheVO object.
     */
    private DeviceInfoCacheVO transformToDeviceInfoCacheVO(DeviceCacheVO parentDeviceCacheVO, DeviceInfo deviceInfo) {
        DeviceInfoCacheVO deviceInfoCacheVO = BeanUtil.toBeanIgnoreError(deviceInfo, DeviceInfoCacheVO.class);

        // Include parent device data in sub-device cache
        deviceInfoCacheVO.setDeviceCacheVO(parentDeviceCacheVO);

        return deviceInfoCacheVO;
    }


    /**
     * Cache the DeviceCacheVO object based on its client ID.
     *
     * @param deviceCacheVO DeviceCacheVO object to be cached.
     */
    private void cacheDeviceBasedOnClientId(DeviceCacheVO deviceCacheVO) {
        String cacheKey = CacheConstants.DEF_DEVICE + deviceCacheVO.getClientId();
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, deviceCacheVO, THIRTY_MINUTES, TimeUnit.MINUTES);
    }

}
