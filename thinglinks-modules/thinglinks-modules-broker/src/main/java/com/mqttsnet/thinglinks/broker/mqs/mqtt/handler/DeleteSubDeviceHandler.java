package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoDeviceOperationResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: thinglinks
 * @description: 处理DELETE_SUB_DEVICE主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:59
 **/
@Slf4j
@Service
public class DeleteSubDeviceHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public DeleteSubDeviceHandler(RedisService redisService,
                                  RemoteDeviceOpenAnyService remoteDeviceOpenAnyService,
                                  RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        super(redisService, remoteDeviceOpenAnyService, remoteMqttBrokerOpenApi, protocolMessageAdapter);
    }

    /**
     * @param topic the MQTT topic the message was received on.
     * @param qos   the quality of service level of the message.
     * @param body  the payload of the message.
     */
    @Override
    public void handle(String topic, String qos, String body) {
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        // Extract variables from the topic
        Map<String, String> stringStringMap = protocolMessageAdapter.extractVariables(topic);
        String version = stringStringMap.get("version");
        String deviceId = stringStringMap.get("deviceId");

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            return;
        }

        try {
            ProtocolDataMessageDTO protocolDataMessageDTO = protocolMessageAdapter.parseProtocolDataMessage(body);
            // 构造 EncryptionDetails 对象
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            // Parse body
            TopoDeleteSubDeviceParam topoDeleteSubDeviceParam = JSON.toJavaObject(JSON.parseObject(dataBody), TopoDeleteSubDeviceParam.class);
            topoDeleteSubDeviceParam.setGatewayIdentification(deviceId);
            String resultDataBody = processingTopicMessage(topoDeleteSubDeviceParam);

            // Handle result
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // Determine response topic based on request topic
            String responseTopic = "/topo/deleteResponse";
            // Generate response topic string
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // 序列化 handleResult 对象为 JSON 字符串
            String resultData = objectMapper.writeValueAsString(handleResult);

            // Push message to MQTT to notify device of successful/failed sub-device deletion
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(deviceCacheVO.getAppId()));
        } catch (Exception e) {
            log.error("Failed to decrypt the message", e);
        }
    }

    /**
     * Process /topo/delete Topic for gateway device to delete sub-device
     *
     * @param topoDeleteSubDeviceParam delete device data
     * @return Processing result json
     */
    @Override
    protected String processingTopicMessage(Object topoDeleteSubDeviceParam) throws Exception {
        R<TopoDeviceOperationResultVO> mqttTopoDeleteDeviceResultVOR = remoteDeviceOpenAnyService.deleteSubDeviceByMqtt((TopoDeleteSubDeviceParam) topoDeleteSubDeviceParam);
        log.info("processingTopoDeleteTopic Processing result:{}", JSON.toJSONString(mqttTopoDeleteDeviceResultVOR));
        return JSON.toJSONString(mqttTopoDeleteDeviceResultVOR.getData());
    }
}

