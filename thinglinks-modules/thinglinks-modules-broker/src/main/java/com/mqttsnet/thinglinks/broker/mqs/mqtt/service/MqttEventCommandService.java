package com.mqttsnet.thinglinks.broker.mqs.mqtt.service;

import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks
 * @description: MqttEventCommandService
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-11-12 16:09
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttEventCommandService {

    private final DeviceCommandApi deviceCommandApi;

    /**
     * Processes the received message, converts it to a DeviceCommandSaveVO, and saves the command.
     *
     * @param deviceCacheVO The cached device information.
     * @param dataBody      The body of the MQTT message.
     * @return A JSON string representing the saved device command.
     */
    public String processCommand(DeviceCacheVO deviceCacheVO, String dataBody) {
        DeviceCommandSaveVO saveVO = convertToSaveVO(deviceCacheVO, dataBody);
        R<DeviceCommand> response = deviceCommandApi.saveDeviceCommand(saveVO);

        if (Boolean.FALSE.equals(response.getIsSuccess())) {
            log.error("Failed to process device command: {}", JSON.toJSONString(response));
            throw new IllegalStateException("Failed to save device command");
        }

        log.info("Device command processed and saved: {}", JSON.toJSONString(response.getData()));
        return JSON.toJSONString(response.getData());
    }

    /**
     * Converts the received message body to a DeviceCommandSaveVO object.
     *
     * @param deviceCacheVO The cached device information.
     * @param dataBody      The body of the MQTT message.
     * @return The DeviceCommandSaveVO object.
     */
    private DeviceCommandSaveVO convertToSaveVO(DeviceCacheVO deviceCacheVO, String dataBody) {
        // Your conversion logic here
        // For now, creating a new object with some default values
        DeviceCommandSaveVO saveVO = new DeviceCommandSaveVO();
        saveVO.setDeviceIdentification(deviceCacheVO.getDeviceIdentification());
        saveVO.setCommandType(DeviceCommandTypeEnum.COMMAND_RESPONSE.getValue());
        saveVO.setStatus(DeviceCommandStatusEnum.SUCCESS.getValue());
        saveVO.setContent(dataBody);
        saveVO.setRemark("Processed command response");
        return saveVO;
    }
}