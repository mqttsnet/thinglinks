package com.mqttsnet.thinglinks.productproperty.vo.update;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import com.mqttsnet.thinglinks.product.constant.ThingModelCodeRule;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
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
@Schema(title = "ProductPropertyUpdateVO", description = "产品模型服务属性表")
public class ProductPropertyUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "属性id")
    @NotNull(message = "请填写属性id", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 服务ID
     */
    @Schema(description = "服务ID")
    @NotNull(message = "请填写服务ID")
    private Long serviceId;
    /**
     * 指示属性编码(用作底层数据列标识)。
     */
    @Schema(description = "属性编码(用作数据列标识):小写字母开头,仅含小写字母、数字、下划线,长度2-50")
    @NotEmpty(message = "请填写属性编码")
    @Pattern(regexp = ThingModelCodeRule.PATTERN, message = ThingModelCodeRule.PATTERN_MSG)
    private String propertyCode;
    /**
     * 指示属性名称
     */
    @Schema(description = "指示属性名称")
    @Size(max = 255, message = "指示属性名称长度不能超过{max}")
    private String propertyName;
    /**
     * 指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传
     */
    @Schema(description = "指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传")
    @NotEmpty(message = "请填写指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传")
    @Size(max = 255, message = "指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传长度不能超过{max}")
    private String datatype;
    /**
     * 属性描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "属性描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "属性描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String description;
    /**
     * 指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @Schema(description = "指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    @Size(max = 255, message = "指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。长度不能超过{max}")
    private String enumlist;
    /**
     * 指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @Schema(description = "指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。")
    @Size(max = 255, message = "指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。长度不能超过{max}")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string、DateTime时生效。
     */
    @Schema(description = "指示字符串长度。仅当dataType为string、DateTime时生效。")
    @Size(max = 255, message = "指示字符串长度。仅当dataType为string、DateTime时生效。长度不能超过{max}")
    private String maxlength;
    /**
     * 指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE
     */
    @Schema(description = "指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE")
    @Size(max = 255, message = "指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE长度不能超过{max}")
    private String method;
    /**
     * 指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @Schema(description = "指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。")
    @Size(max = 255, message = "指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。长度不能超过{max}")
    private String min;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)
     */
    @Schema(description = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    @NotEmpty(message = "请填写指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    @Size(max = 255, message = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)长度不能超过{max}")
    private String required;
    /**
     * 指示步长。
     */
    @Schema(description = "指示步长。")
    @Size(max = 255, message = "指示步长。长度不能超过{max}")
    private String step;
    /**
     * 指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @Schema(description = "指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    @Size(max = 255, message = "指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”长度不能超过{max}")
    private String unit;
    /**
     * 图标png图
     */
    @Schema(description = "图标png图")
    @Size(max = 255, message = "图标png图长度不能超过{max}")
    private String icon;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
