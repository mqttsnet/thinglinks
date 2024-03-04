package com.mqttsnet.thinglinks.broker.mqs.event.listener;

import com.mqttsnet.thinglinks.broker.mqs.event.MqttPingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: MQTT PING事件监听器
 * @packagename: com.mqttsnet.thinglinks.mqtt.listener
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:48
 **/
@Component
@Slf4j
public class MqttPingEventListener {

    /**
     * 发布MQTT PING事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void publishMqttPingEvent(MqttPingEvent event) {
        log.info("Publishing MQTT PING event: message={}", event.getMessage());
        // TODO: 处理MQTT PING事件
    }
}
