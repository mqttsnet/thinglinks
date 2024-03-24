package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory;


import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: thinglinks
 * @description: 通用逻辑处理器
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-30 15:07
 **/
@Slf4j
public abstract class AbstractMessageHandler {

    protected final RedisService redisService;
    protected final RemoteDeviceOpenAnyService remoteDeviceOpenAnyService;
    protected final RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi;
    protected final ProtocolMessageAdapter protocolMessageAdapter;

    public AbstractMessageHandler(RedisService redisService,
                                  RemoteDeviceOpenAnyService remoteDeviceOpenAnyService,
                                  RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        this.redisService = redisService;
        this.remoteDeviceOpenAnyService = remoteDeviceOpenAnyService;
        this.remoteMqttBrokerOpenApi = remoteMqttBrokerOpenApi;
        this.protocolMessageAdapter = protocolMessageAdapter;
    }

    protected DeviceCacheVO getDeviceCacheVO(String deviceIdentification) {
        if (deviceIdentification == null) {
            log.warn("Device identification is null");
            return null;
        }
        Device deviceCacheObject = redisService.getCacheObject(CacheConstants.DEF_DEVICE + deviceIdentification);
        return BeanPlusUtil.toBeanIgnoreError(deviceCacheObject, DeviceCacheVO.class);
    }

    protected ProductModelCacheVO getProductModelCacheVO(String productIdentification) {
        if (productIdentification == null) {
            log.warn("Product identification is null");
            return null;
        }

        ProductModelCacheVO objectCacheResult = redisService.getCacheObject(CacheConstants.DEF_PRODUCT_MODEL + productIdentification);
        return BeanPlusUtil.toBeanIgnoreError(objectCacheResult, ProductModelCacheVO.class);
    }

    protected List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification) {
        // 构造缓存键
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;

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

    protected void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification,
                                                    List<SuperTableDescribeVO> superTableDescribeOpt) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, superTableDescribeOpt);
    }

    protected void sendMessage(String topic, String qos, String message, String tenantId) {
        PublishMessageRequestVO publishMessageRequestVO = new PublishMessageRequestVO();
        publishMessageRequestVO.setReqId(Long.valueOf(SnowflakeIdUtil.nextId()));
        publishMessageRequestVO.setTenantId(tenantId);
        publishMessageRequestVO.setTopic(topic);
        publishMessageRequestVO.setPubQos(qos);
        publishMessageRequestVO.setClientType("web");
        publishMessageRequestVO.setPayload(message);
        publishMessageRequestVO.setRetain("false");
        remoteMqttBrokerOpenApi.sendMessage(publishMessageRequestVO);
    }

    /**
     * 生成响应主题字符串
     *
     * @param version       版本号
     * @param deviceId      设备ID
     * @param responseTopic 响应主题
     * @return 完整的响应主题字符串
     */
    protected String generateResponseTopic(String version, String deviceId, String responseTopic) {
        return String.format("/%s/devices/%s%s", version, deviceId, responseTopic);
    }

    protected abstract String processingTopicMessage(Object messageParam) throws Exception;
}
