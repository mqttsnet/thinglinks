package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ONVIF协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ONVIF协议专属配置")
public class OnvifDeviceConfig {
    @Schema(description = "ONVIF认证用户名")
    private String username;
    @Schema(description = "媒体Profile Token")
    private String mediaProfileToken;
    @Schema(description = "PTZ Profile Token")
    private String ptzProfileToken;
    @Schema(description = "设备服务地址(WSDL)")
    private String deviceServiceUrl;
    @Schema(description = "媒体服务地址")
    private String mediaServiceUrl;
    @Schema(description = "PTZ服务地址")
    private String ptzServiceUrl;
    @Schema(description = "事件服务地址")
    private String eventServiceUrl;
    @Schema(description = "截图URL")
    private String snapshotUrl;
    @Schema(description = "RTSP流地址")
    private String streamUrl;
    @Schema(description = "ONVIF版本号")
    private String onvifVersion;
}
