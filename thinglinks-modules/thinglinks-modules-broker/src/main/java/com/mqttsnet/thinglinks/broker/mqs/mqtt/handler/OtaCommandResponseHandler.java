package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler;

import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.service.MqttEventOtaCommandResponseService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: thinglinks
 * @description: 处理OTA_COMMAND_RESPONSE主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-01-15 23:00
 **/
@Slf4j
@Service
public class OtaCommandResponseHandler extends AbstractMessageHandler implements TopicHandler {
    public OtaCommandResponseHandler(CacheDataHelper cacheDataHelper,
                                     RemoteDeviceOpenAnyService remoteDeviceOpenAnyService,
                                     RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi,
                                     ProtocolMessageAdapter protocolMessageAdapter) {
        super(cacheDataHelper, remoteDeviceOpenAnyService, remoteMqttBrokerOpenApi, protocolMessageAdapter);
    }

    @Autowired
    private MqttEventOtaCommandResponseService mqttEventOtaCommandResponseService;

    /**
     * Handles MQTT messages, decrypts them, and processes the command.
     *
     * @param topic The topic the message was published to.
     * @param qos   The Quality of Service level of the message.
     * @param body  The raw body of the MQTT message.
     */
    @Override
    public void handle(String topic, String qos, String body) {
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
