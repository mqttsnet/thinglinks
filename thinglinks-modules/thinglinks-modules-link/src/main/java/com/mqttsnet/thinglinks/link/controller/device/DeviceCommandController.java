package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.PublishMessageRequestParam;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.link.service.device.DeviceCommandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 前端控制器
 * 设备命令下发及响应表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-20 17:27:25
 * @create [2023-10-20 17:27:25] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceCommand")
@Api(value = "DeviceCommand", tags = "Device Command Controller")
public class DeviceCommandController extends BaseController {

    @Resource
    private DeviceCommandService deviceCommandService;

    /**
     * Issues a list of commands to devices, handling both serial and parallel execution.
     *
     * @param commandWrapper The list of commands to be issued.
     * @return The result of the command execution.
     */
    @ApiOperation(value = "Issue commands to devices", httpMethod = "POST", notes = "Issues a list of commands to devices, handling both serial and parallel execution.")
    @PostMapping("/issueCommands")
    public R<?> issueCommands(@RequestBody @Valid DeviceCommandWrapperParam commandWrapper) {
        deviceCommandService.processDeviceCommands(commandWrapper);
        return R.ok();
    }

    /**
     * Creates a new device command entry in the database.
     *
     * @param deviceCommandSaveVO The device command data to be saved.
     * @return The saved device command data.
     */
    @ApiOperation(value = "Create Device Command", httpMethod = "POST", notes = "Saves a new device command to the database.")
    @PostMapping("/save")
    public R<DeviceCommand> saveDeviceCommand(@RequestBody DeviceCommandSaveVO deviceCommandSaveVO) {
        DeviceCommand savedDeviceCommand = deviceCommandService.saveDeviceCommand(deviceCommandSaveVO);
        return R.ok(savedDeviceCommand);
    }


    /**
     * Send a custom message to a specified MQTT topic with the provided details.
     *
     * @param publishMessageRequestParam The message details.
     * @return The result of the message sending operation.
     */
    @ApiOperation(value = "Send a custom message", notes = "Sends a custom message to a specified MQTT topic with the provided details.")
    @PostMapping("/sendCustomMessage")
    public R sendCustomMessage(@RequestBody PublishMessageRequestParam publishMessageRequestParam) {
        R response = deviceCommandService.sendCustomMessage(publishMessageRequestParam);
        if (!response.isSuccess()) {
            return R.fail("Failed to send message: " + response.getMsg());
        }
        return R.ok("Message sent successfully.");
    }


}


