package com.mqttsnet.thinglinks.video.controller.onvif;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifDevice;
import com.mqttsnet.thinglinks.video.dto.onvif.OnvifProfile;
import com.mqttsnet.thinglinks.video.onvif.OnvifService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ONVIF 设备发现与导入。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/onvif")
@Tag(name = "ONVIF 设备发现")
public class OnvifController {

    private final OnvifService onvifService;

    /**
     * 局域网扫描 ONVIF 设备。
     */
    @PostMapping("/discover")
    @Operation(summary = "扫描 ONVIF 设备", description = "通过 WS-Discovery 发现局域网内 ONVIF 摄像头")
    @ApiResponse(responseCode = "200", description = "扫描完成（即使没发现设备也返回空列表）")
    public R<List<OnvifDevice>> discover(
            @Parameter(description = "扫描超时秒数，建议 3-5") @RequestParam(defaultValue = "4") int timeoutSeconds) {
        return R.success(onvifService.discover(timeoutSeconds));
    }

    /**
     * 取指定设备的 Media Profile 列表（主码流 / 子码流等）。
     */
    @PostMapping("/profiles")
    @Operation(summary = "查询 ONVIF Profile", description = "调用 SOAP GetProfiles，列出设备所有码流")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<List<OnvifProfile>> getProfiles(
            @Parameter(description = "设备 SOAP 端点", required = true) @RequestParam @NotBlank String xaddr,
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String username,
            @Parameter(description = "密码（可选）") @RequestParam(required = false) String password) {
        return R.success(onvifService.getProfiles(xaddr, username, password));
    }

    /**
     * 把发现到的 ONVIF 设备 + 选定 Profile 导入为平台 RTSP 设备。
     */
    @PostMapping("/import")
    @Operation(summary = "导入 ONVIF 设备", description = "拉取 RTSP URL，注册为平台设备，后续可走通用 /play/start 点播")
    @ApiResponse(responseCode = "200", description = "导入成功，返回新设备的 deviceIdentification")
    public R<String> importDevice(
            @Parameter(description = "设备 SOAP 端点", required = true) @RequestParam @NotBlank String xaddr,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "密码") @RequestParam(required = false) String password,
            @Parameter(description = "Profile token", required = true) @RequestParam @NotBlank String profileToken,
            @Parameter(description = "关联流媒体标识", required = true) @RequestParam @NotBlank String mediaIdentification,
            @Parameter(description = "自定义名称") @RequestParam(required = false) String customName) {
        String deviceIdentification = onvifService.importDevice(
                xaddr, username, password, profileToken, mediaIdentification, customName);
        return R.success(deviceIdentification);
    }
}
