package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceOpenAnyService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: thinglinks
 * @description: 处理SECRET_KEY主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:57
 **/
@Slf4j
@Service
public class SecretKeyHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SecretKeyHandler(RedisService redisService,
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
        // Extract variables from the topic
        Map<String, String> stringStringMap = protocolMessageAdapter.extractVariables(topic);
        String version = stringStringMap.get("version");
        String deviceId = stringStringMap.get("deviceId");

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            return;
        }

        try {
            // Directly parse the body into SecretKeyParam class
            /*SecretKeyParam secretKeyParam = JSON.toJavaObject(JSON.parseObject(body), SecretKeyParam.class);
            secretKeyParam.setDeviceIdentification(deviceId);
            String resultDataBody = processingTopicMessage(secretKeyParam);*/

            // Determine response topic based on request topic
            String responseTopic = "/secret/keyResponse";
            // Generate response topic string
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // Push message to MQTT to notify device of successful/failed secret key retrieval
            sendMessage(responseTopicStr, qos, "", String.valueOf(deviceCacheVO.getAppId()));
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
        // Call the API to retrieve the secret key
//        R<SecretKeyResultVO> mqttSecretKeyResultVOR = deviceOpenAnyTenantApi.getSecretKeyByMqtt((SecretKeyParam) secretKeyParam);
        log.info("processingSecretKeyTopic Processing result:{}", JSON.toJSONString(secretKeyParam));
        return JSON.toJSONString("");
    }
}

