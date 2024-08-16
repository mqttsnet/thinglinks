package com.mqttsnet.thinglinks.link.api.domain.device.vo.result;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @program: thinglinks
 * @description: 设备认证结果VO
 * @packagename: com.mqttsnet.thinglinks.link.api.domain.device.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-08-15 11:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@ApiModel(value = "DeviceAuthenticationResultVO", description = "设备认证结果")
public class DeviceAuthenticationResultVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @ApiModelProperty(value = "认证结果")
    private boolean certificationResult;

    @ApiModelProperty(value = "认证失败原因")
    private String errorMessage;

    @ApiModelProperty(value = "设备信息")
    private DeviceResultVO deviceResult;

    @ApiModelProperty(value = "租户ID")
    private String tenantId;

}
