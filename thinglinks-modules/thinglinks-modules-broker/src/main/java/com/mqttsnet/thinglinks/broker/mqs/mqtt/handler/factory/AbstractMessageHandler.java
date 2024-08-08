package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory;


import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        return redisService.getCacheObject(CacheConstants.DEF_DEVICE + deviceIdentification);
    }

    protected ProductModelCacheVO getProductModelCacheVO(String productIdentification) {
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

    protected void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification,
                                                    List<SuperTableDescribeVO> superTableDescribeOpt) {
        String cacheKey = CacheConstants.DEF_PRODUCT_MODEL_SUPER_TABLE + productIdentification + ":" + serviceCode + ":" + deviceIdentification;
        redisService.delete(cacheKey);
        redisService.setCacheObject(cacheKey, superTableDescribeOpt, 10L, TimeUnit.MINUTES);
    }

    protected void sendMessage(String topic, String qos, String message, String tenantId) {
        PublishMessageRequestVO publishMessageRequestVO = new PublishMessageRequestVO();
        publishMessageRequestVO.setReqId(Long.valueOf(SnowflakeIdUtil.nextId()));
        publishMessageRequestVO.setTenantId(tenantId);
        publishMessageRequestVO.setTopic(topic);
        publishMessageRequestVO.setQos(qos);
        publishMessageRequestVO.setClientType("web");
        publishMessageRequestVO.setPayload(message);
        publishMessageRequestVO.setExpirySeconds("3600");
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
