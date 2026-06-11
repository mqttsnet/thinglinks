package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
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
import com.mqttsnet.thinglinks.protocol.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoQueryDeviceResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理QUERY_DEVICE主题
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-01-10 11:59
 **/
@Slf4j
@Service
public class QueryDeviceHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public QueryDeviceHandler(LinkCacheDataHelper linkCacheDataHelper,
                              DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                              MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                              ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }

    /**
     * 处理QUERY_DEVICE主题的MQTT消息
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received QUERY_DEVICE message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }
        // 解析Topic
        Map<String, String> stringStringMap = protocolMessageAdapter.extractVariables(topic);
        String version = stringStringMap.get(CommonConstants.VERSION);
        String deviceId = stringStringMap.get(CommonConstants.DEVICE_ID);

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
                    .cipherFlag(deviceCacheVO.getEncryptMethod())
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            // 解析body
            TopoQueryDeviceParam topoQueryDeviceParam = JSON.parseObject(dataBody, TopoQueryDeviceParam.class);
            String resultDataBody = processingTopicMessage(topoQueryDeviceParam);

            // 处理返回结果
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

            // 根据请求主题确定响应主题
            String responseTopic = "/topo/queryResponse";
            // 生成响应主题字符串
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // 序列化 handleResult 对象为 JSON 字符串
            String resultData = OBJECT_MAPPER.writeValueAsString(handleResult);

            // 推送消息到 MQTT 通知设备添加子设备成功&失败
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(ContextUtil.getTenantId()));
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
            throw BizException.wrap("Invalid parameter type for device query");
        }

        TopoQueryDeviceParam queryParam = (TopoQueryDeviceParam) topoQueryDeviceParam;
        R<TopoQueryDeviceResultVO> topoQueryDeviceResultVOR = deviceOpenAnyUserApi.queryDeviceByMqtt(queryParam);

        log.info("Processing /topo/query result: {}", JSON.toJSONString(topoQueryDeviceResultVOR));
        return JSON.toJSONString(topoQueryDeviceResultVOR.getData());
    }


}