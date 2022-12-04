package com.mqttsnet.thinglinks.rule.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.api.domain
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-12-04 21:39
**/
/**
    * 动作命令信息表
    */
@ApiModel(value="动作命令信息表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionCommands implements Serializable {
    @ApiModelProperty(value="")
    private Integer id;

    /**
    * 业务类型:0规则生效1产品生效2设备生效
    */
    @ApiModelProperty(value="业务类型:0规则生效1产品生效2设备生效")
    private Integer businessType;

    /**
    * 规则标识
    */
    @ApiModelProperty(value="规则标识")
    private String ruleIdentification;

    /**
    * 产品标识
    */
    @ApiModelProperty(value="产品标识")
    private String productIdentification;

    /**
    * 设备标识
    */
    @ApiModelProperty(value="设备标识")
    private String deviceIdentificaiton;

    /**
    * 服务id
    */
    @ApiModelProperty(value="服务id")
    private Integer serviceId;

    /**
    * 命令id
    */
    @ApiModelProperty(value="命令id")
    private Integer commandId;

    /**
    * json命令参数及参数值{"key":"value","key1":"value1"}
    */
    @ApiModelProperty(value="json命令参数及参数值{'key':'value','key1':'value1'}")
    private String commandBody;

    /**
    * 创建者
    */
    @ApiModelProperty(value="创建者")
    private String ctreateBy;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime ctreateTime;

    /**
    * 更新者
    */
    @ApiModelProperty(value="更新者")
    private String updateBy;

    /**
    * 更新时间
    */
    @ApiModelProperty(value="更新时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}