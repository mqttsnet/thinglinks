package com.mqttsnet.thinglinks.broker.mqs.mqtt.event.listener;

import com.google.gson.Gson;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.event.MqttSubscribeEvent;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.service.MqttEventActionService;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private RedisService redisService;

    /**
     * 发布MQTT SUBSCRIBE事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void publishMqttSubscribeEvent(MqttSubscribeEvent event) {
        log.info("Publishing MQTT SUBSCRIBE event: message={}", event.getMessage());
        Gson gson = new Gson();
        Map<Object, Object> map = new HashMap<>();
        map = gson.fromJson(event.getMessage(), map.getClass());
        String clientId = String.valueOf(map.get("clientId"));

        DeviceCacheVO deviceCacheVO = redisService.getCacheObject(CacheConstants.DEF_DEVICE + clientId);
        if (deviceCacheVO == null) {
            log.warn("processingDeviceDataTopic Device not found clientId:{}", clientId);
            return;
        }
        mqttEventActionService.saveMqttEventAction(deviceCacheVO.getDeviceIdentification(), event.getMessage(), DeviceActionTypeEnum.SUBSCRIBE, DeviceActionTypeEnum.SUBSCRIBE.getDescription());
    }
}
