package com.mqttsnet.thinglinks.video.controller.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceMobilePositionService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceMobilePositionResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备移动位置控制器。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/mobilePosition")
@Tag(name = "设备移动位置")
public class VideoDeviceMobilePositionController {

    private final VideoDeviceMobilePositionService mobilePositionService;
    private final EchoService echoService;

    /**
     * 查询设备最新位置
     */
    @GetMapping("/latest")
    @Operation(summary = "最新位置", description = "查询设备最新移动位置")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<VideoDeviceMobilePositionResultVO> getLatestPosition(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        VideoDeviceMobilePositionResultVO result = mobilePositionService.getLatestPosition(deviceIdentification);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 查询设备历史轨迹
     */
    @GetMapping("/history")
    @Operation(summary = "历史轨迹", description = "查询设备历史移动轨迹（分页，按时间升序）")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<IPage<VideoDeviceMobilePositionResultVO>> history(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "200") long size) {
        DataScopeHelper.startDataScope("video_device_mobile_position");
        Page<VideoDeviceMobilePosition> page = new Page<>(current, size);
        IPage<VideoDeviceMobilePositionResultVO> result = mobilePositionService.pageHistory(page, deviceIdentification, startTime, endTime);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 发起移动位置订阅
     */
    @PostMapping("/subscribe")
    @Operation(summary = "位置订阅", description = "发起设备移动位置定时上报订阅")
    @ApiResponse(responseCode = "200", description = "订阅成功")
    public R<Boolean> subscribe(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "上报间隔(秒)") @RequestParam(defaultValue = "5") int interval) {
        mobilePositionService.subscribe(deviceIdentification, interval);
        return R.success(true);
    }

    /**
     * 取消移动位置订阅
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "取消订阅", description = "取消设备移动位置订阅")
    @ApiResponse(responseCode = "200", description = "取消成功")
    public R<Boolean> unsubscribe(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        mobilePositionService.unsubscribe(deviceIdentification);
        return R.success(true);
    }
}
