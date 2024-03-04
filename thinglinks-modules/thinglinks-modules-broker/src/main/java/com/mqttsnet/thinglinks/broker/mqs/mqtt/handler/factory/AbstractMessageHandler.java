package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory;


import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 通用逻辑处理器
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-30 15:07
 **/
@Slf4j
public abstract class AbstractMessageHandler {

    protected final CacheDataHelper cacheDataHelper;
    protected final RemoteDeviceService remoteDeviceService;
    protected final RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi;
    protected final ProtocolMessageAdapter protocolMessageAdapter;

    public AbstractMessageHandler(CacheDataHelper cacheDataHelper,
                                  RemoteDeviceService remoteDeviceService,
                                  RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        this.cacheDataHelper = cacheDataHelper;
        this.remoteDeviceService = remoteDeviceService;
        this.remoteMqttBrokerOpenApi = remoteMqttBrokerOpenApi;
        this.protocolMessageAdapter = protocolMessageAdapter;
    }

    protected DeviceCacheVO getDeviceCacheVO(String deviceIdentification) {
        return cacheDataHelper.getDeviceCacheVO(deviceIdentification);
    }

    protected ProductModelCacheVO getProductModelCacheVO(String productIdentification) {
        return cacheDataHelper.getProductModelCacheVO(productIdentification);
    }

    protected List<SuperTableDescribeVO> getProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification) {
        return cacheDataHelper.getProductModelSuperTableCacheVO(productIdentification, serviceCode, deviceIdentification);
    }

    protected void setProductModelSuperTableCacheVO(String productIdentification, String serviceCode, String deviceIdentification,
                                                    List<SuperTableDescribeVO> superTableDescribeOpt) {
        cacheDataHelper.setProductModelSuperTableCacheVO(productIdentification, serviceCode, deviceIdentification, superTableDescribeOpt);
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
