package com.mqttsnet.thinglinks.productcommandresponse.vo.update;

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

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
 * 产品模型服务命令属性响应参数
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
@Schema(title = "ProductCommandResponseUpdateVO", description = "产品模型服务命令属性响应参数")
public class ProductCommandResponseUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 命令ID
     */
    @Schema(description = "命令ID")
    @NotNull(message = "请填写命令ID")
    private Long commandId;
    /**
     * 服务ID
     */
    @Schema(description = "服务ID")
    private Long serviceId;
    /**
     * 指示数据类型。取值范围：string、int、decimal
     */
    @Schema(description = "指示数据类型。取值范围：string、int、decimal")
    @NotEmpty(message = "请填写指示数据类型。取值范围：string、int、decimal")
    @Size(max = 255, message = "指示数据类型。取值范围：string、int、decimal长度不能超过{max}")
    private String datatype;
    /**
     * 指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @Schema(description = "指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    @Size(max = 255, message = "指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。长度不能超过{max}")
    private String enumlist;
    /**
     * 指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @Schema(description = "指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。")
    @Size(max = 255, message = "指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。长度不能超过{max}")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string时生效。
     */
    @Schema(description = "指示字符串长度。仅当dataType为string时生效。")
    @Size(max = 255, message = "指示字符串长度。仅当dataType为string时生效。长度不能超过{max}")
    private String maxlength;
    /**
     * 指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @Schema(description = "指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。")
    @Size(max = 255, message = "指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。长度不能超过{max}")
    private String min;
    /**
     * 命令中参数的描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "命令中参数的描述，不影响实际功能，可配置为空字符串。")
    @Size(max = 255, message = "命令中参数的描述，不影响实际功能，可配置为空字符串。长度不能超过{max}")
    private String parameterDescription;
    /**
     * 参数编码。
     */
    @Schema(description = "参数编码:小写字母开头,仅含小写字母、数字、下划线,长度2-50")
    @NotEmpty(message = "请填写参数编码")
    @Pattern(regexp = ThingModelCodeRule.PATTERN, message = ThingModelCodeRule.PATTERN_MSG)
    private String parameterCode;
    /**
     * 命令中参数的名字。
     */
    @Schema(description = "命令中参数的名字。")
    @Size(max = 255, message = "命令中参数的名字。长度不能超过{max}")
    private String parameterName;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。
     */
    @Schema(description = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。")
    @NotEmpty(message = "请填写指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。")
    @Size(max = 255, message = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。长度不能超过{max}")
    private String required;
    /**
     * 指示步长。
     */
    @Schema(description = "指示步长。")
    @Size(max = 255, message = "指示步长。长度不能超过{max}")
    private String step;
    /**
     * 指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @Schema(description = "指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    @Size(max = 255, message = "指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”长度不能超过{max}")
    private String unit;
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
