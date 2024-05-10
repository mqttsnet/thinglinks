package com.mqttsnet.thinglinks.link.api.domain.device.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 设备认证Query
 * @packagename: com.mqttsnet.thinglinks.device.vo.query
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-04-20 20:27
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "DeviceAuthenticationQuery", description = "设备认证Query")
public class DeviceAuthenticationQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 客户端标识
     */
    @ApiModelProperty(value = "客户端标识")
    @Size(max = 255, message = "客户端标识长度不能超过{max}")
    private String clientIdentifier;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    @Size(max = 255, message = "用户名长度不能超过{max}")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @Size(max = 255, message = "密码长度不能超过{max}")
    private String password;

    /**
     * 协议类型
     */
    @ApiModelProperty(value = "协议类型")
    @Size(max = 255, message = "协议类型长度不能超过{max}")
    private String protocolType;

    /**
     * 认证方式0-用户名密码，1-ssl证书
     */
    @ApiModelProperty(value = "认证方式0-用户名密码，1-ssl证书")
    @NotNull(message = "请填写认证方式0-用户名密码，1-ssl证书")
    private Integer authMode;
}
