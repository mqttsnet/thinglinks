package com.mqttsnet.thinglinks.broker.mqs.mqtt.event.listener;

import com.mqttsnet.thinglinks.broker.mqs.mqtt.event.MqttSubscribeEvent;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.service.MqttEventActionService;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks
 * @description: MQTT SUBSCRIBE事件监听器
 * @packagename: com.mqttsnet.thinglinks.mqtt.listener
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:46
 **/
@Component
@Slf4j
public class MqttSubscribeEventListener {

    @Autowired
    private MqttEventActionService mqttEventActionService;

    /**
     * 发布MQTT SUBSCRIBE事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void publishMqttSubscribeEvent(MqttSubscribeEvent event) {
        log.info("Publishing MQTT SUBSCRIBE event: message={}", event.getMessage());
        mqttEventActionService.saveMqttEventAction(event.getMessage(), DeviceActionTypeEnum.SUBSCRIBE, DeviceActionTypeEnum.SUBSCRIBE.getDescription());
    }
}
