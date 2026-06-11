package com.mqttsnet.thinglinks.mqs.uplink.service;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceCommandTypeEnum;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: EventCommandService
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-11-12 16:09
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class EventCommandService {

    @Autowired
    private DeviceOpenAnyUserFacade deviceOpenAnyUserApi;

    /**
     * Processes the received message, converts it to a DeviceCommandSaveVO, and saves the command.
     *
     * @param deviceCacheVO The cached device information.
     * @param body          The body of the MQTT message.
     * @param dataBody      The body of the MQTT dataBody  message.
     * @return A JSON string representing the saved device command.
     */
    public String processCommand(DeviceCacheVO deviceCacheVO, String body, String dataBody) {
        DeviceCommandSaveVO saveVO = convertToSaveVO(deviceCacheVO, body, dataBody);
        R<DeviceCommand> response = deviceOpenAnyUserApi.saveDeviceCommand(saveVO);

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
     * @param body          The body of the MQTT message.
     * @param dataBody      The body of the MQTT dataBody message.
     * @return The DeviceCommandSaveVO object.
     */
    private DeviceCommandSaveVO convertToSaveVO(DeviceCacheVO deviceCacheVO, String body, String dataBody) {
        // Your conversion logic here
        // For now, creating a new object with some default values
        DeviceCommandSaveVO saveVO = new DeviceCommandSaveVO();
        saveVO.setDeviceIdentification(deviceCacheVO.getDeviceIdentification());
        saveVO.setCommandType(DeviceCommandTypeEnum.COMMAND_RESPONSE.getValue());
        saveVO.setStatus(DeviceCommandStatusEnum.SUCCESS.getValue());
        saveVO.setContent(body);
        saveVO.setRemark(dataBody);
        return saveVO;
    }
}