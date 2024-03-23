package com.mqttsnet.thinglinks.link.api.domain.ota.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * OTA升级包实体类
 */
@ApiModel(value = "OTA升级包模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class OtaUpgrades implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "应用ID")
    private String appId;


    @ApiModelProperty(value = "包名称")
    private String packageName;


    @ApiModelProperty(value = "升级包类型(0:软件包、1:固件包)")
    private Short packageType;


    @ApiModelProperty(value = "产品标识")
    private String productIdentification;


    @ApiModelProperty(value = "升级包版本号")
    private String version;


    @ApiModelProperty(value = "升级包的位置")
    private String fileLocation;


    @ApiModelProperty(value = "状态")
    private Short status;


    @ApiModelProperty(value = "升级包功能描述")
    private String description;


    @ApiModelProperty(value = "自定义信息")
    private String customInfo;


    @ApiModelProperty(value = "描述")
    private String remark;


    @ApiModelProperty(value = "创建人")
    private String createdBy;


    @ApiModelProperty(value = "创建时间")
    private Date createdTime;


    @ApiModelProperty(value = "更新人")
    private String updatedBy;

    @ApiModelProperty(value = "更新时间")
    private Date updatedTime;
}
