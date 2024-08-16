package com.mqttsnet.thinglinks.link.api.domain.device.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 设备认证模式（用于连接鉴权）
 * </p>
 *
 * @author shihuan sun
 * @date 2024-08-14
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DeviceAuthModeEnum", description = "设备认证模式类型")
public enum DeviceAuthModeEnum {

    /**
     * 用户名密码模式
     */
    ACCOUNT_MODE(0, "用户名密码模式"),

    /**
     * SSL/TLS证书模式
     */
    SSL_MODE(1, "SSL/TLS证书模式"),
    ;

    private Integer value;
    private String desc;

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
