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
 * 产品模型服务属性表
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
@ApiModel(value = "ProductPropertyParamVO", description = "产品模型服务属性参数VO")
public class ProductPropertyParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;


    /**
     * 服务ID
     */
    @ApiModelProperty(value = "服务ID")
    @NotNull(message = "请填写服务ID")
    private Long serviceId;
    /**
     * 指示属性编码
     */
    @ApiModelProperty(value = "指示属性编码")
    @NotEmpty(message = "请填写指示属性编码")
    @Size(max = 255, message = "指示属性编码长度不能超过{max}")
    private String propertyCode;
    /**
     * 指示属性名称
     */
    @ApiModelProperty(value = "指示属性名称")
    @Size(max = 255, message = "指示属性名称长度不能超过{max}")
    private String propertyName;
    /**
     * 指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传
     */
    @ApiModelProperty(value = "指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传")
    @NotEmpty(message = "请填写指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传")
    @Size(max = 255, message = "指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传长度不能超过{max}")
    private String datatype;
    /**
     * 属性描述，不影响实际功能，可配置为空字符串。
     */
    @ApiModelProperty(value = "属性描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "属性描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String description;
    /**
     * 指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @ApiModelProperty(value = "指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    @Size(max = 255, message = "指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。长度不能超过{max}")
    private String enumlist;
    /**
     * 指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @ApiModelProperty(value = "指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。")
    @Size(max = 255, message = "指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。长度不能超过{max}")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string、DateTime时生效。
     */
    @ApiModelProperty(value = "指示字符串长度。仅当dataType为string、DateTime时生效。")
    @Size(max = 255, message = "指示字符串长度。仅当dataType为string、DateTime时生效。长度不能超过{max}")
    private String maxlength;
    /**
     * 指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE
     */
    @ApiModelProperty(value = "指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE")
    @Size(max = 255, message = "指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE长度不能超过{max}")
    private String method;
    /**
     * 指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @ApiModelProperty(value = "指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。")
    @Size(max = 255, message = "指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。长度不能超过{max}")
    private String min;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)
     */
    @ApiModelProperty(value = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    @NotEmpty(message = "请填写指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    @Size(max = 255, message = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)长度不能超过{max}")
    private String required;
    /**
     * 指示步长。
     */
    @ApiModelProperty(value = "指示步长。")
    @Size(max = 255, message = "指示步长。长度不能超过{max}")
    private String step;
    /**
     * 指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @ApiModelProperty(value = "指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    @Size(max = 255, message = "指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”长度不能超过{max}")
    private String unit;
    /**
     * 图标png图
     */
    @ApiModelProperty(value = "图标png图")
    @Size(max = 255, message = "图标png图长度不能超过{max}")
    private String icon;
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
