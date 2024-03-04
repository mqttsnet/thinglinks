package com.mqttsnet.thinglinks.broker.mqs.event.listener;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.broker.mqs.event.MqttPublishEvent;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.event.MqttMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: MqttPublish事件监听器
 * @packagename: com.mqttsnet.thinglinks.consumer.mqtt.listener
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:12
 **/
@Component
@Slf4j
public class MqttPublishEventListener {

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 处理MQTT PUBLISH事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void handleMqttPublishEvent(MqttPublishEvent event) {
        log.info("Received MQTT PUBLISH event: message={}", event.getMessage());
        // 处理PUBLISH事件
        if (StringUtils.isEmpty(event.getMessage()) && JSONUtil.isTypeJSON(event.getMessage())) {
            log.warn("The message is empty and ignored");
            return;
        }
        JSONObject mqttMessage = JSON.parseObject(event.getMessage());
        String topic = mqttMessage.getString("topic");
        String qos = mqttMessage.getString("qos");
        String body = mqttMessage.getString("body");
        String time = mqttMessage.getString("time");
        if (!JSONUtil.isTypeJSON(body)) {
            log.error("Topic:{},The body is empty and ignored", topic);
            return;
        }
        log.info("MqttPublishEventListener handleMqttPublishEvent mqttMessage topic:{}, qos:{}, body:{}, time:{}", topic, qos, body, time);
        // 发布MQTT消息事件
        publisher.publishEvent(new MqttMessageEvent(this, topic, qos, body, time));
    }
}
