package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用SIP协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "通用SIP协议专属配置")
public class SipDeviceConfig {
    @Schema(description = "SIP域")
    private String sipDomain;
    @Schema(description = "SIP服务器地址")
    private String sipServerHost;
    @Schema(description = "SIP服务器端口")
    private Integer sipServerPort = 5060;
    @Schema(description = "SIP用户名")
    private String sipUsername;
    @Schema(description = "SIP显示名称")
    private String sipDisplayName;
    @Schema(description = "注册服务器地址")
    private String registrar;
    @Schema(description = "代理服务器地址")
    private String proxyServer;
}
