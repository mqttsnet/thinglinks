package com.mqttsnet.thinglinks.broker.mqs.mqtt.event.listener;

import com.google.gson.Gson;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.event.MqttCloseEvent;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.service.MqttEventActionService;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
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
 * @description: MQTT CLOSE事件监听器
 * @packagename: com.mqttsnet.thinglinks.mqtt.listener
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-28 00:42
 **/
@Component
@Slf4j
public class MqttCloseEventListener {

    @Resource
    private RemoteDeviceService remoteDeviceService;

    @Autowired
    private MqttEventActionService mqttEventActionService;


    /**
     * 发布MQTT CLOSE事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("brokerAsync-mqttMsg")
    public void publishMqttCloseEvent(MqttCloseEvent event) {
        log.info("Publishing MQTT CLOSE event: message={}", event.getMessage());
        Gson gson = new Gson();
        Map<Object, Object> map = new HashMap<>();
        map = gson.fromJson(event.getMessage(), map.getClass());
        String clientId = String.valueOf(map.get("clientId"));

        // TODO 从缓存中获取设备信息 校验设备是否存在

        // 构造设备对象并设置客户端ID和连接状态
        Device deviceToUpdate = new Device()
                .setClientId(clientId)
                .setConnectStatus(DeviceConnectStatusEnum.OFFLINE.getKey());

        // 更新设备连接状态
        R updateDeviceConnectionStatus = remoteDeviceService.updateConnectStatusByClientId(deviceToUpdate);

        // 检查更新操作的结果
        if (ResultEnum.SUCCESS.getCode() != updateDeviceConnectionStatus.getCode()) {
            log.error("Failed to update the device connection status to OFFLINE, clientId={}", clientId);
            return;
        }


        String describable = Optional.ofNullable(DeviceConnectStatusEnum.OFFLINE.getKey())
                .flatMap(DeviceConnectStatusEnum::fromValue)
                .map(DeviceConnectStatusEnum::getKey)
                .map(desc -> "The device connection status is updated to " + desc)
                .orElse("The device connection status is updated to OFFLINE");

        mqttEventActionService.saveMqttEventAction(event.getMessage(), DeviceActionTypeEnum.CLOSE, describable);
    }
}