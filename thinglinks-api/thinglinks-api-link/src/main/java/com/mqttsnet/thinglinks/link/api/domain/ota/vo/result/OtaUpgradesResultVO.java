package com.mqttsnet.thinglinks.link.api.domain.ota.vo.result;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 表单查询方法返回值VO
 * OTA升级包
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:36:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@ApiModel(value = "OtaUpgradesResultVO", description = "OTA升级包")
public class OtaUpgradesResultVO  implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @ApiModelProperty(value = "主键")
    private Long id;

    /**
    * 应用ID
    */
    @ApiModelProperty(value = "应用ID")
    private String appId;
    /**
    * 包名称
    */
    @ApiModelProperty(value = "包名称")
    private String packageName;
    /**
    * 升级包类型(0:软件包、1:固件包)
    */
    @ApiModelProperty(value = "升级包类型(0:软件包、1:固件包)")
    private Integer packageType;
    /**
    * 产品标识
    */
    @ApiModelProperty(value = "产品标识")
    private String productIdentification;
    /**
    * 升级包版本号
    */
    @ApiModelProperty(value = "升级包版本号")
    private String version;
    /**
    * 升级包的位置
    */
    @ApiModelProperty(value = "升级包的位置")
    private String fileLocation;
    /**
    * 状态
    */
    @ApiModelProperty(value = "状态")
    private Integer status;
    /**
    * 升级包功能描述
    */
    @ApiModelProperty(value = "升级包功能描述")
    private String description;
    /**
    * 自定义信息
    */
    @ApiModelProperty(value = "自定义信息")
    private String customInfo;
    /**
    * 描述
    */
    @ApiModelProperty(value = "描述")
    private String remark;
    /**
    * 创建人组织
    */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;



}
