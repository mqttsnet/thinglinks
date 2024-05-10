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
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoAddDeviceResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: thinglinks
 * @description: 处理ADD_SUB_DEVICE主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:59
 **/
@Slf4j
@Service
public class AddSubDeviceHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AddSubDeviceHandler(RedisService redisService,
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
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            // 解析body
            TopoAddSubDeviceParam topoAddSubDeviceParam = JSON.toJavaObject(JSON.parseObject(dataBody), TopoAddSubDeviceParam.class);
            topoAddSubDeviceParam.setGatewayIdentification(deviceId);
            String resultDataBody = processingTopicMessage(topoAddSubDeviceParam);

            // 处理返回结果
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // 根据请求主题确定响应主题
            String responseTopic = "/topo/addResponse";
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
     * Processes the message received on the /topo/add Topic for adding a sub-device to a gateway device.
     *
     * @param topoAddSubDeviceParam The data for adding a sub-device.
     * @return The JSON representation of the processing result.
     * @throws Exception if there is an issue processing the message.
     */
    @Override
    protected String processingTopicMessage(Object topoAddSubDeviceParam) throws Exception {
        if (!(topoAddSubDeviceParam instanceof TopoAddSubDeviceParam)) {
            throw new IllegalArgumentException("Invalid parameter type for adding sub-device");
        }

        TopoAddSubDeviceParam addParam = (TopoAddSubDeviceParam) topoAddSubDeviceParam;
        R<TopoAddDeviceResultVO> mqttTopoAddDeviceResultVOR = remoteDeviceOpenAnyService.saveSubDeviceByMqtt(addParam);

        log.info("Processing /topo/add Topic result: {}", JSON.toJSONString(mqttTopoAddDeviceResultVOR));
        return JSON.toJSONString(mqttTopoAddDeviceResultVOR.getData());
    }


}