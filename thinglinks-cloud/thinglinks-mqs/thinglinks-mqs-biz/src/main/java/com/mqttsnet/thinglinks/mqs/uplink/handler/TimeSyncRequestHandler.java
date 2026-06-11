package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TimeZone;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
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
import com.mqttsnet.thinglinks.protocol.vo.param.TimeSyncRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TimeSyncResponseParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理 syncTime 主题
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-07-24 18:00
 **/
@Slf4j
@Service
public class TimeSyncRequestHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TimeSyncRequestHandler(LinkCacheDataHelper linkCacheDataHelper,
                                  DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                                  MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * Synchronizing server time
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received TIME_SYNC_REQUEST message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
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
            TimeSyncRequestParam timeSyncRequestParam = JSON.parseObject(decryptedBody, TimeSyncRequestParam.class);

            log.info("timeSyncRequestParam:{} ", JsonUtil.toJson(timeSyncRequestParam));

            String resultDataBody = processingTopicMessage(timeSyncRequestParam);

            // Handle result
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // Determine response topic based on request topic
            String responseTopic = "/topo/timeSyncResponse";
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


    @Override
    protected String processingTopicMessage(Object messageParam) throws Exception {
        // 获取处理开始时间戳（服务器端），以毫秒为单位
        long serverTimeMillis = System.currentTimeMillis();

        // 获取当前时间和时区
        String serverTime = DateUtil.format(DateUtil.date(serverTimeMillis), DatePattern.UTC_MS_PATTERN);
        TimeZone timeZone = TimeZone.getDefault();
        String currentZone = timeZone.getID();

        TimeSyncResponseParam timeSyncResponseParam = TimeSyncResponseParam.builder()
                .serverTime(serverTime)
                .serverTimeMillis(serverTimeMillis)
                .timeZone(currentZone)
                .build();

        return JsonUtil.toJson(timeSyncResponseParam);
    }

}
