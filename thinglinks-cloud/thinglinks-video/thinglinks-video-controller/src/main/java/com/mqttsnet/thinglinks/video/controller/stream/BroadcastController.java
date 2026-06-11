package com.mqttsnet.thinglinks.video.controller.stream;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.service.stream.BroadcastService;
import com.mqttsnet.thinglinks.video.vo.save.stream.BroadcastSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * 语音广播控制器。
 * 提供语音广播/对讲的发起和停止 REST API。
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
@RequestMapping("/broadcast")
@Tag(name = "语音广播")
public class BroadcastController {

    private final BroadcastService broadcastService;

    /**
     * 发起语音广播
     */
    @PostMapping("/start")
    @Operation(summary = "发起广播", description = "向指定设备通道发起语音广播")
    @ApiResponse(responseCode = "200", description = "广播发起成功")
    public R<Boolean> start(@Valid @RequestBody BroadcastSaveVO saveVO) {
        log.info("发起语音广播: deviceIdentification={}, channelIdentification={}", saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        broadcastService.startBroadcast(saveVO.getDeviceIdentification(), saveVO.getChannelIdentification());
        return R.success(true);
    }

    /**
     * 停止语音广播
     */
    @DeleteMapping("/stop")
    @Operation(summary = "停止广播", description = "停止指定设备通道的语音广播")
    @ApiResponse(responseCode = "200", description = "停止成功")
    public R<Boolean> stop(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        log.info("停止语音广播: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        broadcastService.stopBroadcast(deviceIdentification, channelIdentification);
        return R.success(true);
    }
}
