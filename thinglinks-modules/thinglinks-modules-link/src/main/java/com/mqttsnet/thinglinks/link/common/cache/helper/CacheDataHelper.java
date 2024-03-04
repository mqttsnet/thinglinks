package com.mqttsnet.thinglinks.link.common.cache.helper;

import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * -----------------------------------------------------------------------------
 * 文件名称: CacheDataHelper.java
 * -----------------------------------------------------------------------------
 * 描述:
 * CacheDataHelper
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * 修改历史:
 * 日期           作者          版本        描述
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-10-12 22:26
 */
@Component
@Slf4j
public class CacheDataHelper {

    private final RedisService redisService;

    public CacheDataHelper(RedisService redisService) {
        this.redisService = redisService;
    }

    public DeviceCacheVO getDeviceCacheVO(String deviceIdentification) {
        if (deviceIdentification == null) {
            log.warn("Device identification is null");
            return null;
        }
        Device deviceCacheObject = redisService.getCacheObject(CacheConstants.DEVICE_RECORD_KEY + deviceIdentification);
        return BeanPlusUtil.toBeanIgnoreError(deviceCacheObject, DeviceCacheVO.class);
    }

    public ProductModelCacheVO getProductModelCacheVO(String productIdentification) {
        if (productIdentification == null) {
            log.warn("Product identification is null");
            return null;
        }

        ProductModelCacheVO objectCacheResult = redisService.getCacheObject(CacheConstants.DEVICE_RECORD_KEY + productIdentification);
        return BeanPlusUtil.toBeanIgnoreError(objectCacheResult, ProductModelCacheVO.class);
    }

    public List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification) {
        // 构造缓存键
        String cacheKey = CacheConstants.PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;

        // 尝试从缓存获取数据
        List<Object> cacheList;
        try {
            cacheList = redisService.getCacheList(cacheKey);
        } catch (Exception e) {
            log.error("Error fetching from cache for key: {}", cacheKey, e);
            return Collections.emptyList();
        }

        // 检查缓存结果是否为空
        if (cacheList == null || cacheList.isEmpty()) {
            log.warn("The product model super table is not in the cache for key: {}", cacheKey);
            return Collections.emptyList();
        }

        // 尝试将缓存结果转换为预期类型的列表
        try {
            // 这里假设所有缓存的对象都已经是SuperTableDescribeVO类型的实例
            // 为了安全起见，我们进行一次类型检查
            @SuppressWarnings("unchecked")
            List<SuperTableDescribeVO> result = (List<SuperTableDescribeVO>) cacheList.stream()
                    .filter(SuperTableDescribeVO.class::isInstance)
                    .map(SuperTableDescribeVO.class::cast)
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                log.warn("The product model super table cache list is empty for key: {}", cacheKey);
            }
            return result;
        } catch (ClassCastException e) {
            log.error("Unexpected type in cached list for key: {}", cacheKey, e);
            return Collections.emptyList();
        }
    }


    public void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification,
                                                 List<SuperTableDescribeVO> superTableDescribeOpt) {
        String cacheKey = CacheConstants.PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, superTableDescribeOpt);
    }

}
