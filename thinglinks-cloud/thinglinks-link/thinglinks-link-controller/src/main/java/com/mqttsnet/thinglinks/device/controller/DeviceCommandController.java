package com.mqttsnet.thinglinks.device.controller;

import java.util.List;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.service.DeviceCommandService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceCommandPageQuery;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceCommandUpdateVO;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.protocol.vo.param.PublishMqttMessageRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.param.PublishWebSocketMessageRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceCommandResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "设备命令下发及响应")
public class DeviceCommandController extends SuperController<DeviceCommandService, Long, DeviceCommand, DeviceCommandSaveVO,
        DeviceCommandUpdateVO, DeviceCommandPageQuery, DeviceCommandResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    /**
     * Issues a list of commands to devices, handling both serial and parallel execution.
     *
     * @param commandWrapper The list of commands to be issued.
     * @return {@link R<List<DeviceCommandResultVO>>} The result of the command execution.
     */
    @Operation(summary = "Issue commands to devices", description = "Issues a list of commands to devices, handling both serial and parallel execution.")
    @PostMapping("/issueCommands")
    public R<List<DeviceCommandResultVO>> issueCommands(@RequestBody @Valid DeviceCommandWrapperParam commandWrapper) {
        List<DeviceCommandResultVO> result = superService.processDeviceCommands(commandWrapper);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * Creates a new device command entry in the database.
     *
     * @param deviceCommandSaveVO The device command data to be saved.
     * @return The saved device command data.
     */
    @Operation(summary = "Create Device Command", description = "Saves a new device command to the database.")
    @PostMapping("/save")
    @WebLog(value = "Save Device Command", request = false)
    public R<DeviceCommand> saveDeviceCommand(@RequestBody DeviceCommandSaveVO deviceCommandSaveVO) {
        DeviceCommand savedDeviceCommand = superService.saveDeviceCommand(deviceCommandSaveVO);
        return R.success(savedDeviceCommand);
    }

    /**
     * Send a Mqtt custom message to a specified MQTT topic with the provided details.
     *
     * @param publishMqttMessageRequestParam The message details.
     * @return The result of the message sending operation.
     */
    @Operation(summary = "Send a Mqtt custom message", description = "Sends a Mqtt custom message to a specified MQTT topic with the provided details.")
    @PostMapping("/sendMqttCustomMessage")
    public R<?> sendMqttCustomMessage(@RequestBody PublishMqttMessageRequestParam publishMqttMessageRequestParam) {
        try {
            log.info("【发送MQTT自定义消息】请求参数: {}", publishMqttMessageRequestParam);
            superService.sendMqttCustomMessage(publishMqttMessageRequestParam);
            return R.success("Message sent successfully.");
        } catch (Exception e) {
            return R.fail("Failed to send message");
        }
    }


    /**
     * Send a WebSocket custom message to a specified MQTT topic with the provided details.
     *
     * @param publishWebSocketMessageRequestParam The message details.
     * @return The result of the message sending operation.
     */
    @Operation(summary = "Send a WebSocket custom message", description = "Sends a WebSocket custom message to a specified MQTT topic with the provided details.")
    @PostMapping("/sendWebSocketCustomMessage")
    public R<?> sendCustomMessage(@RequestBody PublishWebSocketMessageRequestParam publishWebSocketMessageRequestParam) {
        try {
            superService.sendWebSocketCustomMessage(publishWebSocketMessageRequestParam);
            return R.success("Message sent successfully.");
        } catch (Exception e) {
            return R.fail("Failed to send message");
        }
    }


}


