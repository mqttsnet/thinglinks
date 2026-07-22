package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * ISUP协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ISUP协议专属配置")
public class IsupDeviceConfig {
    @Schema(description = "设备类型(DVR/NVR/IPC等)")
    private String deviceType;
    @Schema(description = "登录句柄")
    private Integer loginHandle;
}
