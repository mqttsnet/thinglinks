package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * GB28181协议专属配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "GB28181协议专属配置")
public class Gb28181DeviceConfig {
    @Schema(description = "GB28181版本号(2016/2022)")
    private String gbVersion = "2016";
    @Schema(description = "字符集")
    private String charset = "GB2312";
    @Schema(description = "地理坐标系")
    private String geoCoordSys = "WGS84";
    @Schema(description = "SSRC校验")
    private Boolean ssrcCheck = false;
    @Schema(description = "目录订阅")
    private Boolean subscribeCycleForCatalog = false;
    @Schema(description = "移动位置订阅")
    private Boolean subscribeCycleForMobilePosition = false;
    @Schema(description = "报警订阅")
    private Boolean subscribeCycleForAlarm = false;
    @Schema(description = "位置上报间隔(秒)")
    private Integer mobilePositionSubmissionInterval = 5;
    @Schema(description = "定位能力(0=不支持/1=GPS/2=北斗)")
    private Integer positionCapability = 0;
    @Schema(description = "是否作为消息通道")
    private Boolean asMessageChannel = false;
    @Schema(description = "语音广播ACK后推流")
    private Boolean broadcastPushAfterAck = false;
}
