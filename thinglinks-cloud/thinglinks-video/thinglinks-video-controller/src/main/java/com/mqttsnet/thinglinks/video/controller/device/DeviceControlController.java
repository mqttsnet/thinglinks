package com.mqttsnet.thinglinks.video.controller.device;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.service.device.DeviceControlService;
import com.mqttsnet.thinglinks.video.vo.save.device.DeviceControlSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * 设备远程控制控制器。
 * 提供设备远程启动、录像控制、布防撤防等 REST API。
 * <p>
 *
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceControl")
@Tag(name = "设备远程控制")
public class DeviceControlController {

    private final DeviceControlService deviceControlService;

    /**
     * 统一设备控制入口
     *
     * @param saveVO 设备控制请求参数
     * @return 操作结果
     */
    @PostMapping("/control")
    @Operation(summary = "设备控制", description = "统一设备远程控制入口")
    @ApiResponse(responseCode = "200", description = "控制命令发送成功")
    public R<Boolean> control(@Valid @RequestBody DeviceControlSaveVO saveVO) {
        log.info("设备控制: deviceIdentification={}, type={}, value={}",
                saveVO.getDeviceIdentification(), saveVO.getControlType(), saveVO.getControlValue());
        deviceControlService.deviceControl(saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(),
                saveVO.getControlType(), saveVO.getControlValue());
        return R.success(true);
    }

    /**
     * 快捷接口 - 远程启动
     */
    @PostMapping("/teleBoot")
    @Operation(summary = "远程启动", description = "远程重启设备")
    public R<Boolean> teleBoot(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.teleBoot(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 开始录像
     */
    @PostMapping("/record/start")
    @Operation(summary = "开始录像", description = "控制设备开始录像")
    public R<Boolean> startRecord(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.startRecord(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 停止录像
     */
    @PostMapping("/record/stop")
    @Operation(summary = "停止录像", description = "控制设备停止录像")
    public R<Boolean> stopRecord(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.stopRecord(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 布防
     */
    @PostMapping("/guard/set")
    @Operation(summary = "布防", description = "设置设备布防")
    public R<Boolean> setGuard(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.setGuard(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 撤防
     */
    @PostMapping("/guard/reset")
    @Operation(summary = "撤防", description = "设置设备撤防")
    public R<Boolean> resetGuard(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.resetGuard(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 强制关键帧
     */
    @PostMapping("/forceKeyFrame")
    @Operation(summary = "强制关键帧", description = "请求设备发送关键帧")
    public R<Boolean> forceKeyFrame(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        deviceControlService.forceKeyFrame(deviceIdentification, channelIdentification);
        return R.success(true);
    }
}
