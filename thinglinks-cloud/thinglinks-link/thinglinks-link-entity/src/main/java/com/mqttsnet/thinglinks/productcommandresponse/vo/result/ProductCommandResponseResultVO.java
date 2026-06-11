package com.mqttsnet.thinglinks.productcommandresponse.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.productversionchangelog.vo.DiffIgnore;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProductCommandResponseResultVO", description = "产品模型服务命令属性响应参数")
public class ProductCommandResponseResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 命令ID(结构外键,不参与变更记录 diff)
     */
    @Schema(description = "命令ID")
    @DiffIgnore
    private Long commandId;
    /**
     * 服务ID(结构外键,不参与变更记录 diff)
     */
    @Schema(description = "服务ID")
    @DiffIgnore
    private Long serviceId;
    /**
     * 指示数据类型。取值范围：string、int、decimal
     */
    @Schema(description = "指示数据类型。取值范围：string、int、decimal")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_COMMAND_DATA_TYPE)
    private String datatype;
    /**
     * 指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @Schema(description = "指示枚举值。如开关状态status可有如下取值enumList : [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    private String enumlist;
    /**
     * 指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @Schema(description = "指示最大值。仅当dataType为int、decimal时生效，逻辑小于等于。")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string时生效。
     */
    @Schema(description = "指示字符串长度。仅当dataType为string时生效。")
    private String maxlength;
    /**
     * 指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @Schema(description = "指示最小值。仅当dataType为int、decimal时生效，逻辑大于等于。")
    private String min;
    /**
     * 命令中参数的描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "命令中参数的描述，不影响实际功能，可配置为空字符串。")
    private String parameterDescription;
    /**
     * 参数编码
     */
    @Schema(description = "参数编码")
    private String parameterCode;
    /**
     * 命令中参数的名字。
     */
    @Schema(description = "命令中参数的名字。")
    private String parameterName;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。
     */
    @Schema(description = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。")
    private String required;
    /**
     * 指示步长。
     */
    @Schema(description = "指示步长。")
    private String step;
    /**
     * 指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @Schema(description = "指示单位。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    private String unit;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
