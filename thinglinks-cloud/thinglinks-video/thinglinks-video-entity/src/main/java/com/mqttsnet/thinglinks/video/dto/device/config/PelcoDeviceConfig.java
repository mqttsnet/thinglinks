package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pelco-D/P串行控制协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Pelco-D/P串行控制协议专属配置")
public class PelcoDeviceConfig {
    @Schema(description = "设备地址码(1-255)")
    private Integer deviceAddress = 1;
    @Schema(description = "波特率")
    private Integer baudRate = 9600;
    @Schema(description = "数据位")
    private Integer dataBits = 8;
    @Schema(description = "停止位")
    private Integer stopBits = 1;
    @Schema(description = "校验位(NONE/ODD/EVEN)")
    private String parity = "NONE";
    @Schema(description = "串口标识(COM1/ttyUSB0等)")
    private String serialPort;
    @Schema(description = "串口转网络转换器地址")
    private String converterHost;
    @Schema(description = "串口转网络转换器端口")
    private Integer converterPort;
}
