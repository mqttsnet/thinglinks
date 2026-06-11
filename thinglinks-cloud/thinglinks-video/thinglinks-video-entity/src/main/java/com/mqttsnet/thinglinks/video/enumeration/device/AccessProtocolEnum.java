package com.mqttsnet.thinglinks.video.enumeration.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 设备接入协议类型
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
@Getter
@AllArgsConstructor
@Schema(title = "AccessProtocolEnum", description = "设备接入协议类型")
public enum AccessProtocolEnum {

    /** 国标 GB/T 28181 协议(2016/2022) */
    GB28181("GB28181", "国标GB/T 28181协议"),
    /** ONVIF 开放网络视频接口协议 */
    ONVIF("ONVIF", "ONVIF开放网络视频接口协议"),
    /** 海康 ISUP(Ehome) 私有协议 */
    ISUP("ISUP", "海康ISUP(Ehome)私有协议"),
    /** 交通部 JT/T 1078 协议 */
    JT1078("JT1078", "交通部JT/T 1078协议"),
    /** 通用 SIP 协议 */
    SIP("SIP", "通用SIP协议"),
    /** 通用 RTSP 协议（直接给 URL，平台通过 ZLM addStreamProxy 拉流） */
    RTSP("RTSP", "RTSP拉流协议"),
    /** Pelco-D 串行控制协议 */
    PELCO_D("PELCO_D", "Pelco-D串行控制协议"),
    /** Pelco-P 串行控制协议 */
    PELCO_P("PELCO_P", "Pelco-P串行控制协议");

    @Schema(description = "协议标识值")
    private final String value;
    @Schema(description = "协议描述")
    private final String desc;

    public static Optional<AccessProtocolEnum> fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
