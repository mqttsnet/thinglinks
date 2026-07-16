package com.mqttsnet.thinglinks.mqs.uplink.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import com.mqttsnet.thinglinks.mqs.uplink.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.mqs.uplink.service.EventOtaReadResponseService;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理OTA_READ_RESPONSE主题mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-03-13 15:00
 **/
@Slf4j
@Service
public class OtaReadResponseHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private EventOtaReadResponseService mqttEventOtaReadResponseService;

    public OtaReadResponseHandler(LinkCacheDataHelper linkCacheDataHelper,
                                  DeviceOpenInnerFacade deviceOpenInnerApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenInnerApi, protocolMessageAdapter);
    }

    /**
     * 本处理器完整匹配的 topic 正则。
     *
     * @return OTA_READ_RESPONSE 主题正则
     * @author mqttsnet
     * @since 2026-06-03
     */
    @Override
    public String topicPattern() {
        return "^/([^/]+)/devices/([^/]+)/topo/otaReadResponse$";
    }

    /**
     * Handles MQTT messages, decrypts them, and processes the command.
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(UplinkMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received OTA_READ_RESPONSE message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        Map<String, String> variables = protocolMessageAdapter.extractVariables(topic);
        String version = variables.get(CommonIotConstants.VERSION);
        String deviceId = variables.get(CommonIotConstants.DEVICE_ID);

        DeviceCacheVO deviceCacheVO = resolveDeviceCache(eventSource, deviceId);
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
            TopoOtaReadResponseParam topoOtaReadResponseParam = JSON.parseObject(decryptedBody, TopoOtaReadResponseParam.class);


            processingTopicMessage(topoOtaReadResponseParam);
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
        mqttEventOtaReadResponseService.handleMqttEventOtaReadResponse((TopoOtaReadResponseParam) messageParam);
        return null;
    }

}
