package com.mqttsnet.thinglinks.broker.mqs.event;

import com.mqttsnet.thinglinks.broker.api.domain.enumeration.MqttEventEnum;
import org.springframework.context.ApplicationEvent;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: MqttUnsubscribeEvent
 * @packagename: com.mqttsnet.thinglinks.mqtt.event
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 01:53
 **/
public class MqttUnsubscribeEvent extends ApplicationEvent {
    private String message;
    private MqttEventEnum eventType;

    public MqttUnsubscribeEvent(Object source, MqttEventEnum eventType, String message) {
        super(source);
        this.message = message;
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public MqttEventEnum getEventType() {
        return eventType;
    }
}

