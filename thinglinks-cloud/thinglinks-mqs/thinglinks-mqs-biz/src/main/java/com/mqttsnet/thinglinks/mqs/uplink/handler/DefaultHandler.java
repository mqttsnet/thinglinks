package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 其他默认Topic处理器
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:55
 **/
@Service
@Slf4j
public class DefaultHandler implements TopicHandler {

    /**
     * 处理默认情况的逻辑
     *
     * @param eventSource the MQTT message event source.
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        // 处理默认情况的逻辑
        log.info("处理默认情况的逻辑, topic: {}, qos: {}, payload(body): {}", topic, qos, body);
    }
}
