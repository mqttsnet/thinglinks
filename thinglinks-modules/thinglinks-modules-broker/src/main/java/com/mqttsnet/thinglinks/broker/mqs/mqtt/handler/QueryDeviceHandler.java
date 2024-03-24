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
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoQueryDeviceResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: thinglinks
 * @description: 处理QUERY_DEVICE主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-01-10 11:59
 **/
@Slf4j
@Service
public class QueryDeviceHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public QueryDeviceHandler(RedisService redisService,
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
        // 解析Topic
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

            // 解析body
            TopoQueryDeviceParam topoQueryDeviceParam = JSON.toJavaObject(JSON.parseObject(dataBody), TopoQueryDeviceParam.class);
            String resultDataBody = processingTopicMessage(topoQueryDeviceParam);

            // 处理返回结果
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // 根据请求主题确定响应主题
            String responseTopic = "/topo/queryResponse";
            // 生成响应主题字符串
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // 序列化 handleResult 对象为 JSON 字符串
            String resultData = objectMapper.writeValueAsString(handleResult);

            // 推送消息到 MQTT 通知设备添加子设备成功&失败
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(deviceCacheVO.getAppId()));
        } catch (Exception e) {
            log.error("Failed to decrypt the message", e);
        }
    }

    /**
     * Processes the message received on the /topo/query Topic to query device information.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return The JSON representation of the query result.
     * @throws Exception if there is an issue processing the message.
     */
    @Override
    protected String processingTopicMessage(Object topoQueryDeviceParam) throws Exception {
        if (!(topoQueryDeviceParam instanceof TopoQueryDeviceParam)) {
            throw new IllegalArgumentException("Invalid parameter type for device query");
        }

        TopoQueryDeviceParam queryParam = (TopoQueryDeviceParam) topoQueryDeviceParam;
        R<TopoQueryDeviceResultVO> topoQueryDeviceResultVOR = remoteDeviceOpenAnyService.queryDeviceByMqtt(queryParam);

        log.info("Processing /topo/query result: {}", JSON.toJSONString(topoQueryDeviceResultVOR));
        return JSON.toJSONString(topoQueryDeviceResultVOR.getData());
    }


}