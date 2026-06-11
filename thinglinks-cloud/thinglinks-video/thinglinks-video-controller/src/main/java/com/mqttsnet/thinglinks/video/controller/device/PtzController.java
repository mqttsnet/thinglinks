package com.mqttsnet.thinglinks.video.controller.device;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.service.device.PtzService;
import com.mqttsnet.thinglinks.video.vo.save.device.PtzControlSaveVO;
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
 * PTZ 云台控制控制器。
 * 提供方向控制、变倍控制、预置位操作、巡航控制、
 * 扫描控制、辅助开关控制等 REST API。
 * <p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ptz")
@Tag(name = "PTZ云台控制")
public class PtzController {

    private final PtzService ptzService;

    /**
     * 统一 PTZ 控制入口
     *
     * @param saveVO PTZ 控制请求参数
     * @return 操作结果
     */
    @PostMapping("/control")
    @Operation(summary = "PTZ控制", description = "统一PTZ控制入口，支持方向、变倍、预置位、巡航、扫描、辅助开关等")
    @ApiResponse(responseCode = "200", description = "控制命令发送成功")
    public R<Boolean> control(@Valid @RequestBody PtzControlSaveVO saveVO) {
        ptzService.executeCommand(saveVO);
        return R.success(true);
    }

    /**
     * 快捷接口 - 方向控制
     */
    @PostMapping("/direction")
    @Operation(summary = "方向控制", description = "控制云台上下左右移动")
    public R<Boolean> direction(
        @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
        @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
        @Parameter(description = "方向(UP/DOWN/LEFT/RIGHT等)", required = true) @RequestParam String direction,
        @Parameter(description = "移动速度(0-255)") @RequestParam(required = false) Integer speed) {
        ptzService.directionControl(deviceIdentification, channelIdentification, direction, speed);
        return R.success(true);
    }

    /**
     * 快捷接口 - 停止控制
     */
    @PostMapping("/stop")
    @Operation(summary = "停止PTZ", description = "停止云台移动")
    public R<Boolean> stop(
        @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
        @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        ptzService.stopPtz(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 快捷接口 - 预置位设置
     */
    @PostMapping("/preset/set")
    @Operation(summary = "设置预置位", description = "保存当前云台位置为预置位")
    public R<Boolean> presetSet(
        @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
        @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
        @Parameter(description = "预置位编号(1-255)", required = true) @RequestParam int presetId) {
        ptzService.presetSet(deviceIdentification, channelIdentification, presetId);
        return R.success(true);
    }

    /**
     * 快捷接口 - 预置位调用
     */
    @PostMapping("/preset/call")
    @Operation(summary = "调用预置位", description = "调用指定预置位")
    public R<Boolean> presetCall(
        @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
        @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
        @Parameter(description = "预置位编号(1-255)", required = true) @RequestParam int presetId) {
        ptzService.presetCall(deviceIdentification, channelIdentification, presetId);
        return R.success(true);
    }

    /**
     * 快捷接口 - 预置位删除
     */
    @PostMapping("/preset/delete")
    @Operation(summary = "删除预置位", description = "删除指定预置位")
    public R<Boolean> presetDelete(
        @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
        @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
        @Parameter(description = "预置位编号(1-255)", required = true) @RequestParam int presetId) {
        ptzService.presetDelete(deviceIdentification, channelIdentification, presetId);
        return R.success(true);
    }
}
