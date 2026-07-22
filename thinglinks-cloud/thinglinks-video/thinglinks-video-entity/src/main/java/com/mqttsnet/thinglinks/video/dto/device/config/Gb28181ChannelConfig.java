package com.mqttsnet.thinglinks.video.dto.device.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * GB28181通道国标属性配置
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "GB28181通道国标属性配置")
public class Gb28181ChannelConfig {
    @Schema(description = "警区")
    private String block;
    @Schema(description = "信令安全模式")
    private Integer safetyWay;
    @Schema(description = "注册方式")
    private Integer registerWay;
    @Schema(description = "证书序列号")
    private String certNum;
    @Schema(description = "证书有效性(0=有效/1=无效/2=过期/3=吊销)")
    private Integer certStatus;
    @Schema(description = "证书终止有效期")
    private String certExpiryTime;
    @Schema(description = "摄像机位置类型")
    private Integer positionType;
    @Schema(description = "安装位置类型(室内/室外)")
    private Integer roomType;
    @Schema(description = "用途属性")
    private Integer useType;
    @Schema(description = "补光类型")
    private Integer supplyLightType;
    @Schema(description = "监控方向")
    private Integer directionType;
    @Schema(description = "分辨率")
    private String resolution;
    @Schema(description = "下载速度")
    private String downloadSpeed;
    @Schema(description = "空域编码能力")
    private Integer svcSpaceSupportMod;
    @Schema(description = "时域编码能力")
    private Integer svcTimeSupportMode;
}
