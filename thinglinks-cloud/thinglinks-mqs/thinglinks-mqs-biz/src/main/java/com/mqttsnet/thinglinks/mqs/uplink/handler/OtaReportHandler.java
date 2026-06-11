package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.CommonConstants;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.mqtt.service.MqttEventOtaReportService;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理OTA_REPORT主题mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-03-13 15:00
 **/
@Slf4j
@Service
public class OtaReportHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private MqttEventOtaReportService mqttEventOtaReportService;

    public OtaReportHandler(LinkCacheDataHelper linkCacheDataHelper,
                            DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                            MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                            ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * Handles MQTT messages, decrypts them, and processes the command.
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received OTA_REPORT message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        Map<String, String> variables = protocolMessageAdapter.extractVariables(topic);
        String version = variables.get(CommonConstants.VERSION);
        String deviceId = variables.get(CommonConstants.DEVICE_ID);

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            log.warn("Device with ID {} not found.", deviceId);
            return;
        }

        try {
            ProtocolDataMessageDTO protocolDataMessageDTO = protocolMessageAdapter.parseProtocolDataMessage(body);
            // 构造 EncryptionDetails 对象
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .cipherFlag(deviceCacheVO.getEncryptMethod())
                    .build();
            String decryptedBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            // Parse body
            TopoOtaReportParam topoOtaReportParam = JSON.parseObject(decryptedBody, TopoOtaReportParam.class);


            String resultDataBody = processingTopicMessage(topoOtaReportParam);

            // Handle result
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // Determine response topic based on request topic
            String responseTopic = "/topo/otaReportResponse";
            // Generate response topic string
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // 序列化 handleResult 对象为 JSON 字符串
            String resultData = OBJECT_MAPPER.writeValueAsString(handleResult);

            // Push message to MQTT to notify device of successful/failed sub-device deletion
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(ContextUtil.getTenantId()));
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
        Optional<TopoOtaReportResponseParam> topoOtaPullResponseParamOptional = mqttEventOtaReportService.handleMqttEventOtaReport((TopoOtaReportParam) messageParam);

        return topoOtaPullResponseParamOptional.map(JSON::toJSONString).orElse(StrUtil.EMPTY);
    }

}
