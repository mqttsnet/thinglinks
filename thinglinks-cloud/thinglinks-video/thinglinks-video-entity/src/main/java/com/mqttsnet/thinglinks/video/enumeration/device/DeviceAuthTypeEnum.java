package com.mqttsnet.thinglinks.video.enumeration.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 设备认证方式
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
@Getter
@AllArgsConstructor
@Schema(title = "DeviceAuthTypeEnum", description = "设备认证方式")
public enum DeviceAuthTypeEnum {

    /** 密码认证(GB28181 SIP Digest) */
    PASSWORD("PASSWORD", "密码认证(SIP Digest)"),
    /** 验证码校验(ISUP) */
    VALIDATE_CODE("VALIDATE_CODE", "验证码校验(ISUP)"),
    /** 鉴权码(JT1078) */
    AUTH_TOKEN("AUTH_TOKEN", "鉴权码(JT1078)"),
    /** 证书认证 */
    CERTIFICATE("CERTIFICATE", "证书认证"),
    /** WS-UsernameToken认证(ONVIF) */
    WS_USERNAMETOKEN("WS_USERNAMETOKEN", "WS-UsernameToken认证(ONVIF)"),
    /** HTTP Digest认证(ONVIF) */
    DIGEST("DIGEST", "HTTP Digest认证(ONVIF)"),
    /** 无认证 */
    NONE("NONE", "无认证");

    @Schema(description = "认证方式标识值")
    private final String value;
    @Schema(description = "认证方式描述")
    private final String desc;

    public static Optional<DeviceAuthTypeEnum> fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
