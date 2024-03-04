package com.mqttsnet.thinglinks.broker.mqs.event.publisher;

import com.mqttsnet.thinglinks.broker.api.domain.enumeration.MqttEventEnum;
import com.mqttsnet.thinglinks.broker.mqs.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: MQTT事件发布器 用于发布MQTT事件 TODO MqttEventEnum 事件枚举类 用于定义MQTT事件 预留
 * @packagename: com.mqttsnet.thinglinks.consumer.mqtt.publisher
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:07
 **/
@Component
@Slf4j
public class MqttEventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishMqttConnectEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT CONNECT event: message={}", message);
        eventPublisher.publishEvent(new MqttConnectEvent(this, eventEnum, message));
    }

    public void publishMqttCloseEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT CLOSE event: message={}", message);
        eventPublisher.publishEvent(new MqttCloseEvent(this, eventEnum, message));
    }

    public void publishMqttDisconnectEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT DISCONNECT event: message={}", message);
        eventPublisher.publishEvent(new MqttDisconnectEvent(this, eventEnum, message));
    }

    public void publishMqttPublishEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT PUBLISH event: message={}", message);
        eventPublisher.publishEvent(new MqttPublishEvent(this, eventEnum, message));
    }

    public void publishMqttSubscribeEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT SUBSCRIBE event: message={}", message);
        eventPublisher.publishEvent(new MqttSubscribeEvent(this, eventEnum, message));
    }

    public void publishMqttUnsubscribeEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT UNSUBSCRIBE event: message={}", message);
        eventPublisher.publishEvent(new MqttUnsubscribeEvent(this, eventEnum, message));
    }

    public void publishMqttPingEvent(MqttEventEnum eventEnum, String message) {
        log.info("Publishing MQTT PING event: message={}", message);
        eventPublisher.publishEvent(new MqttPingEvent(this, eventEnum, message));
    }

}
