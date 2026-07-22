package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * JT1078协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "JT1078协议专属配置")
public class Jt1078DeviceConfig {
    @Schema(description = "车牌号")
    private String plateNumber;
    @Schema(description = "车牌颜色")
    private String plateColor;
    @Schema(description = "终端ID")
    private String terminalId;
}
