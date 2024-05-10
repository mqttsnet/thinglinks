package com.mqttsnet.thinglinks.link.common.cache.helper;

import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
        return redisService.getCacheObject(CacheConstants.DEF_DEVICE + deviceIdentification);
    }

    public ProductModelCacheVO getProductModelCacheVO(String productIdentification) {
        if (productIdentification == null) {
            log.warn("Product identification is null");
            return null;
        }

        return redisService.getCacheObject(CacheConstants.DEF_PRODUCT_MODEL + productIdentification);
    }

    @SuppressWarnings("unchecked")
    protected List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;

        try {
            // 直接获取缓存的对象
            Object cachedObject = redisService.getCacheObject(cacheKey);
            if (cachedObject == null) {
                log.warn("The product model super table is not in the cache for key: {}", cacheKey);
                return Collections.emptyList();
            }

            // 尝试将对象转换为预期的列表类型
            return (List<SuperTableDescribeVO>) cachedObject;
        } catch (ClassCastException e) {
            log.error("Unexpected type in cached object for key: {}", cacheKey, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching from cache for key: {}", cacheKey, e);
            return Collections.emptyList();
        }
    }


    public void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification,
                                                 List<SuperTableDescribeVO> superTableDescribeOpt) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, superTableDescribeOpt);
    }

    public void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, List<SuperTableDescribeVO> superTableDescribeOpt) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode;
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, superTableDescribeOpt);
    }

}
