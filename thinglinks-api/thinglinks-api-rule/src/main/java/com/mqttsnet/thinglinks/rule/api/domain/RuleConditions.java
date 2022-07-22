package com.mqttsnet.thinglinks.rule.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.api.domain
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-07-21 18:50
**/

/**
 * 规则条件表
 */
@ApiModel(value = "规则条件表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuleConditions implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 规则ID
     */
    @ApiModelProperty(value = "规则ID")
    private Long ruleId;

    /**
     * 条件类型(0:匹配设备触发、1:指定设备触发、2:按策略定时触发)
     */
    @ApiModelProperty(value = "条件类型(0:匹配设备触发、1:指定设备触发、2:按策略定时触发)")
    private Long conditionType;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;

    /**
     * 产品标识
     */
    @ApiModelProperty(value = "产品标识")
    private String productIdentification;

    /**
     * 服务ID
     */
    @ApiModelProperty(value = "服务ID")
    private Long serviceId;

    /**
     * 属性ID
     */
    @ApiModelProperty(value = "属性ID")
    private Long propertiesId;

    /**
     * 比较模式
     * <
     * <=
     * >
     * >=
     * ==
     * !=
     * in
     * between
     */
    @ApiModelProperty(value = "比较模式,<,<=,>,>=,==,!=,in,between")
    private String comparisonMode;

    /**
     * 比较值
     * <p>
     * between类型传值例子  [10,15] 必须是两位，且数字不能重复
     * 判断数据是否处于一个离散的取值范围内，例如输入[1,2,3,4]，取值范围是1、2、3、4四个值，如果比较值类型为float(double)，两个float（double）型数值相差在0.000001范围内即为相等
     */
    @ApiModelProperty(value = "比较值,,between类型传值例子  [10,15] 必须是两位，且数字不能重复,判断数据是否处于一个离散的取值范围内，例如输入[1,2,3,4]，取值范围是1、2、3、4四个值，如果比较值类型为float(double)，两个float（double）型数值相差在0.000001范围内即为相等")
    private String comparisonValue;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}