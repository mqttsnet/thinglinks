package com.mqttsnet.thinglinks.link.api.domain.product.vo.param;

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
 * 表单保存方法VO
 * 产品模型设备响应服务命令属性表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "ProductCommandResponseParamVO", description = "产品模型设备响应服务命令属性表")
public class ProductCommandResponseParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 命令ID
     */
    @ApiModelProperty(value = "命令ID")
    @NotNull(message = "请填写命令ID")
    private Long commandId;
    /**
     * 服务ID
     */
    @ApiModelProperty(value = "服务ID")
    private Long serviceId;
    /**
     * 指示数据类型。取值范围：string、int、decimal
     */
    @ApiModelProperty(value = "指示数据类型。取值范围：string、int、decimal")
    @NotEmpty(message = "请填写指示数据类型。取值范围：string、int、decimal")
    @Size(max = 255, message = "指示数据类型。取值范围：string、int、decimal长度不能超过{max}")
    private String datatype;
    /**
     * 指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @ApiModelProperty(value = "指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    @Size(max = 255, message = "指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。长度不能超过{max}")
    private String enumlist;
    /**
     * 指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @ApiModelProperty(value = "指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。")
    @Size(max = 255, message = "指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。长度不能超过{max}")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string时生效。
     */
    @ApiModelProperty(value = "指示字符串长度。仅当dataType为string时生效。")
    @Size(max = 255, message = "指示字符串长度。仅当dataType为string时生效。长度不能超过{max}")
    private String maxlength;
    /**
     * 指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @ApiModelProperty(value = "指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。")
    @Size(max = 255, message = "指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。长度不能超过{max}")
    private String min;
    /**
     * 命令中参数的描述，不影响实际功能，可配置为空字符串。
     */
    @ApiModelProperty(value = "命令中参数的描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "命令中参数的描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String parameterDescription;
    /**
     * 参数编码
     */
    @ApiModelProperty(value = "参数编码")
    private String parameterCode;

    /**
     * 命令中参数的名字。
     */
    @ApiModelProperty(value = "命令中参数的名字。")
    @Size(max = 255, message = "命令中参数的名字。长度不能超过{max}")
    private String parameterName;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。
     */
    @ApiModelProperty(value = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。")
    @NotEmpty(message = "请填写指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。")
    @Size(max = 255, message = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。长度不能超过{max}")
    private String required;
    /**
     * 指示步长。
     */
    @ApiModelProperty(value = "指示步长。")
    @Size(max = 255, message = "指示步长。长度不能超过{max}")
    private String step;
    /**
     * 指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @ApiModelProperty(value = "指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    @Size(max = 255, message = "指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”长度不能超过{max}")
    private String unit;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;



}
