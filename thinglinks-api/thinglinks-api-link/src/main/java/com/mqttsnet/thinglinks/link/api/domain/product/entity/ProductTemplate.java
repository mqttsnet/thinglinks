package com.mqttsnet.thinglinks.link.api.domain.product.entity;

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
* @packagename: com.mqttsnet.thinglinks.link.api.domain.product.entity
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-11-18 20:36
**/

/**
 * 产品模板
 */
@ApiModel(value = "产品模板")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTemplate implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    private String appId;

    /**
     * 产品模版标识
     */
    @ApiModelProperty(value = "产品模版标识")
    private String templateIdentification;

    /**
     * 产品模板名称:自定义，支持中文、英文大小写、数字、下划线和中划线
     */
    @ApiModelProperty(value = "产品模板名称:自定义，支持中文、英文大小写、数字、下划线和中划线")
    private String templateName;

    /**
     * 状态(字典值：启用  停用)
     */
    @ApiModelProperty(value = "状态(字典值：启用  停用)")
    private String status;

    /**
     * 产品模型模板描述
     */
    @ApiModelProperty(value = "产品模型模板描述")
    private String remark;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}