package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.service.DeviceDataProcessingService;
import com.mqttsnet.thinglinks.service.ProtocolGroovyScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理DEVICE_DATA主题
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 23:00
 **/
@Slf4j
@Service
public class DeviceDatasHandler extends AbstractMessageHandler implements TopicHandler {
    @Autowired
    private ProtocolGroovyScriptService protocolGroovyScriptService;

    @Autowired
    private DeviceDataProcessingService deviceDataProcessingService;


    public DeviceDatasHandler(LinkCacheDataHelper linkCacheDataHelper,
                              DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                              MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                              ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * 处理设备上报数据消息（DEVICE_DATA主题）
     *
     * @param eventSource 包含MQTT消息事件源的对象
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received DEVICE_DATA message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        // Extract variables from the topic
        Map<String, String> stringStringMap = protocolMessageAdapter.extractVariables(topic);
        String deviceId = stringStringMap.get("deviceId");

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            log.warn("Device with ID {} not found in cache, skipping processing.", deviceId);
            return;
        }
        try {
            // 获取转换后的body
            body = protocolGroovyScriptService.datasTopicBodyTransformed(ProtocolTypeEnum.MQTT, deviceCacheVO.getDeviceIdentification(), deviceCacheVO.getProductIdentification(), body);
        } catch (Exception e) {
            log.error("转换报文失败，设备标识: {}, 错误: {}", deviceCacheVO.getDeviceIdentification(), e.getMessage(), e);
            return;
        }
        log.info("处理最终协议(可能经过脚本转换处理)报文...设备标识: {}, 报文: {}", deviceCacheVO.getDeviceIdentification(), body);
        // 验证协议格式
        if (!JSONUtil.isTypeJSON(body) || !protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("协议格式不正确，设备标识: {}, 报文: {}", deviceCacheVO.getDeviceIdentification(), body);
            return;
        }

        try {
            // 构造 EncryptionDetails 对象
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .cipherFlag(deviceCacheVO.getEncryptMethod())
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            if (StrUtil.isBlank(dataBody)) {
                log.warn("解密后的数据体为空，设备标识: {}", deviceCacheVO.getDeviceIdentification());
                return;
            }
            TopoDeviceDataReportParam dataReportParam = JSON.parseObject(dataBody, TopoDeviceDataReportParam.class);
            processingTopicMessage(dataReportParam);
        } catch (Exception e) {
            log.error("处理设备数据消息失败，设备标识: {}, 错误: {}", deviceCacheVO.getDeviceIdentification(), e.getMessage(), e);

        }
    }

    /**
     * Process /device/data Topic for device data reporting
     *
     * @param deviceDataParam device data
     * @return Processing result json
     */
    @Override
    protected String processingTopicMessage(Object deviceDataParam) throws Exception {
        deviceDataProcessingService.processDeviceDataReport((TopoDeviceDataReportParam) deviceDataParam);
        return StrPool.EMPTY;
    }


}
