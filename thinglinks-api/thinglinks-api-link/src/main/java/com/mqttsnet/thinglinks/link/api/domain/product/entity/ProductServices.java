package com.mqttsnet.thinglinks.link.api.domain.product.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.Accessors;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.link.api.domain.product.entity
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-11-18 20:38
**/

/**
 * 产品模型服务表
 */
@ApiModel(value = "产品模型服务表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class ProductServices implements Serializable {
    /**
     * 服务id
     */
    @ApiModelProperty(value = "服务id")
    private Long id;

    /**
     * 服务名称:支持英文大小写、数字、下划线和中划线
     */
    @ApiModelProperty(value = "服务名称:支持英文大小写、数字、下划线和中划线,")
    private String serviceName;

    /**
     * 产品模版标识
     */
    @ApiModelProperty(value = "产品模版标识")
    private String templateIdentification;

    /**
     * 产品标识
     */
    @ApiModelProperty(value = "产品标识")
    private String productIdentification;

    /**
     * 状态(字典值：0启用  1停用)
     */
    @ApiModelProperty(value = "状态(字典值：0启用  1停用)")
    private String status;

    /**
     * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串""。
     */
    @ApiModelProperty(value = "服务的描述信息:文本描述，不影响实际功能，可配置为空字符串''。,")
    private String description;

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