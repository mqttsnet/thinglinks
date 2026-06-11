package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.enumeration.DeviceEncryptMethodEnum;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoSecretKeyResponseResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理SECRET_KEY主题
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:57
 **/
@Slf4j
@Service
public class SecretKeyHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SecretKeyHandler(LinkCacheDataHelper linkCacheDataHelper,
                            DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                            MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                            ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * 处理SECRET_KEY主题的MQTT消息
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received SECRET_KEY message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
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
            log.warn("Device {} not found in cache", deviceId);
            return;
        }

        try {
            ProtocolDataMessageDTO protocolDataMessageDTO = protocolMessageAdapter.parseProtocolDataMessage(body);
            // 构造 EncryptionDetails 对象
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .cipherFlag(DeviceEncryptMethodEnum.PLAINTEST.getValue())
                    .build();

            TopoSecretKeyResponseResultVO responseResultVO = new TopoSecretKeyResponseResultVO();
            responseResultVO.setDeviceIdentification(deviceCacheVO.getDeviceIdentification());
            responseResultVO.setEncryptMethod(deviceCacheVO.getEncryptMethod());
            responseResultVO.setEncryptKey(deviceCacheVO.getEncryptKey());
            responseResultVO.setEncryptVector(deviceCacheVO.getEncryptVector());
            responseResultVO.setSignKey(deviceCacheVO.getSignKey());

            // 处理返回结果
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, JSONUtil.toJsonStr(responseResultVO), encryptionDetailsDTO);

            // Determine response topic based on request topic
            String responseTopic = "/topo/secretKeyResponse";
            // Generate response topic string
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // 序列化 handleResult 对象为 JSON 字符串
            String resultData = OBJECT_MAPPER.writeValueAsString(handleResult);

            // Push message to MQTT to notify device of successful/failed secret key retrieval
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(ContextUtil.getTenantId()));
        } catch (Exception e) {
            log.error("Failed to parse the message", e);
        }
    }

    /**
     * Process /secret/key Topic for secret key retrieval
     *
     * @param secretKeyParam secret key data
     * @return Processing result json
     */
    @Override
    protected String processingTopicMessage(Object secretKeyParam) throws Exception {

        return JSON.toJSONString("");
    }
}

