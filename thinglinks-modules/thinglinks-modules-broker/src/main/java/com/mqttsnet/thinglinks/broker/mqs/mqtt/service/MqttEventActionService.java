package com.mqttsnet.thinglinks.broker.mqs.mqtt.service;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceActionService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.DeviceActionTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: thinglinks
 * @description: MqttEventActionHandler
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-08-20 16:09
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttEventActionService {

    @Resource
    private RemoteDeviceActionService remoteDeviceActionService;

    /**
     * 保存MQTT事件动作
     *
     * @param deviceIdentification 设备标识
     * @param eventMessage         事件消息
     * @param actionType           动作类型
     * @param describable          描述
     */
    public void saveMqttEventAction(String deviceIdentification, String eventMessage, DeviceActionTypeEnum actionType, String describable) {
        log.info("Save MQTT event action: deviceIdentification={}, actionType={}, describable={}", deviceIdentification, actionType, describable);

        // save device action
        DeviceAction deviceAction = new DeviceAction()
                .setDeviceIdentification(deviceIdentification)
                .setActionType(actionType.getAction())
                .setStatus(ResultEnum.SUCCESS.getMessage())
                .setMessage(eventMessage);

        R deviceActionR = remoteDeviceActionService.add(deviceAction);
        if (ResultEnum.SUCCESS.getCode() != deviceActionR.getCode()) {
            log.info("Save device action success: deviceAction={}", deviceActionR.getData());
        } else {
            log.error("Save device action failed: deviceAction={}", deviceActionR.getData());
        }
    }
}