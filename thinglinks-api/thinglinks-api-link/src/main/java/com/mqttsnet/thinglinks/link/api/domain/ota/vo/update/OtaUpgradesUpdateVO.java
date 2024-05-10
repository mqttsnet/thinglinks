package com.mqttsnet.thinglinks.link.api.domain.ota.vo.update;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
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
@EqualsAndHashCode
@Builder
@ApiModel(value = "OtaUpgradesUpdateVO", description = "OTA升级包")
public class OtaUpgradesUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @NotNull(message = "请填写主键")
    private Long id;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
    private String appId;
    /**
     * 包名称
     */
    @ApiModelProperty(value = "包名称")
    @NotEmpty(message = "请填写包名称")
    @Size(max = 100, message = "包名称长度不能超过{max}")
    private String packageName;
    /**
     * 升级包类型(0:软件包、1:固件包)
     */
    @ApiModelProperty(value = "升级包类型(0:软件包、1:固件包)")
    @NotNull(message = "请填写升级包类型(0:软件包、1:固件包)")
    private Integer packageType;
    /**
     * 产品标识
     */
    @ApiModelProperty(value = "产品标识")
    @NotEmpty(message = "请填写产品标识")
    @Size(max = 100, message = "产品标识长度不能超过{max}")
    private String productIdentification;
    /**
     * 升级包版本号
     */
    @ApiModelProperty(value = "升级包版本号")
    @NotEmpty(message = "请填写升级包版本号")
    @Size(max = 255, message = "升级包版本号长度不能超过{max}")
    private String version;
    /**
     * 升级包的位置
     */
    @ApiModelProperty(value = "升级包的位置")
    @NotEmpty(message = "请填写升级包的位置")
    @Size(max = 255, message = "升级包的位置长度不能超过{max}")
    private String fileLocation;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    @NotNull(message = "请填写状态")
    private Integer status;
    /**
     * 升级包功能描述
     */
    @ApiModelProperty(value = "升级包功能描述")
    @Size(max = 255, message = "升级包功能描述长度不能超过{max}")
    private String description;
    /**
     * 自定义信息
     */
    @ApiModelProperty(value = "自定义信息")
    @Size(max = 2147483647, message = "自定义信息长度不能超过{max}")
    private String customInfo;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @Size(max = 255, message = "描述长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;


}
