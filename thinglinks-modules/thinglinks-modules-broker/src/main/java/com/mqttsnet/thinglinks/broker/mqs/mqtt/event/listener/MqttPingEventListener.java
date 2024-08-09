package com.mqttsnet.thinglinks.broker.mqs.mqtt.event.listener;

import com.google.gson.Gson;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.event.MqttPingEvent;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.service.MqttEventActionService;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @program: thinglinks
 * @description: MQTT PING事件监听器
 * @packagename: com.mqttsnet.thinglinks.mqtt.listener
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:48
 **/
@Component
@Slf4j
public class MqttPingEventListener {

    @Resource
    private RemoteDeviceService remoteDeviceService;

    @Autowired
    private MqttEventActionService mqttEventActionService;

    @Autowired
    private RedisService redisService;

    /**
     * 发布MQTT PING事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void publishMqttPingEvent(MqttPingEvent event) {
        log.info("Publishing MQTT PING event: message={}", event.getMessage());
        Gson gson = new Gson();
        Map<Object, Object> map = new HashMap<>();
        map = gson.fromJson(event.getMessage(), map.getClass());
        String clientId = String.valueOf(map.get("clientId"));

        DeviceCacheVO deviceCacheVO = redisService.getCacheObject(CacheConstants.DEF_DEVICE + clientId);
        if (deviceCacheVO == null) {
            log.warn("processingDeviceDataTopic Device not found clientId:{}", clientId);
            return;
        }


        // 构造设备对象并设置客户端ID和连接状态
        Device deviceToUpdate = new Device()
                .setClientId(clientId)
                .setConnectStatus(DeviceConnectStatusEnum.ONLINE.getKey());

        // 更新设备连接状态
        R updateDeviceConnectionStatus = remoteDeviceService.updateConnectStatusByClientId(deviceToUpdate);

        // 检查更新操作的结果
        if (ResultEnum.SUCCESS.getCode() != updateDeviceConnectionStatus.getCode()) {
            log.error("Failed to update the device connection status to OFFLINE, clientId={}", clientId);
            return;
        }


        String describable = Optional.ofNullable(DeviceConnectStatusEnum.ONLINE.getKey())
                .flatMap(DeviceConnectStatusEnum::fromValue)
                .map(DeviceConnectStatusEnum::getValue)
                .map(desc -> "The device connection status is updated to " + desc + " via a ping operation")
                .orElse("The device connection status is updated to ONLINE via a ping operation");

        mqttEventActionService.saveMqttEventAction(deviceCacheVO.getDeviceIdentification(), event.getMessage(), DeviceActionTypeEnum.PING, describable);
    }
}
