package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.mqtt.service.MqttEventOtaCommandResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理OTA_COMMAND_RESPONSE主题mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-01-15 23:00
 **/
@Slf4j
@Service
public class OtaCommandResponseHandler extends AbstractMessageHandler implements TopicHandler {
    @Autowired
    private MqttEventOtaCommandResponseService mqttEventOtaCommandResponseService;

    public OtaCommandResponseHandler(LinkCacheDataHelper linkCacheDataHelper,
                                     DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                                     MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                                     ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * Handles MQTT messages, decrypts them, and processes the command.
     *
     * @param eventSource The MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received OTA_COMMAND_RESPONSE message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        Map<String, String> variables = protocolMessageAdapter.extractVariables(topic);
        String deviceId = variables.get("deviceId");

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            log.warn("Device with ID {} not found.", deviceId);
            return;
        }

        try {
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .cipherFlag(deviceCacheVO.getEncryptMethod())
                    .build();
            String decryptedBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);
            mqttEventOtaCommandResponseService.saveMqttEventOtaCommandResponse(deviceCacheVO, decryptedBody);
        } catch (Exception e) {
            log.error("Failed to decrypt the message", e);
        }
    }


    /**
     * Processes the message and returns the response body.
     *
     * @param messageParam The message body.
     * @return The response body.
     * @throws Exception If an error occurs while processing the message.
     */
    @Override
    protected String processingTopicMessage(Object messageParam) throws Exception {

        return null;
    }

}
