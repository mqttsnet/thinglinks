package com.mqttsnet.thinglinks.productproperty.vo.result;

import cn.hutool.core.collection.ListUtil;
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
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ProductPropertyResultVO", description = "产品模型服务属性表")
public class ProductPropertyResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @DiffIgnore
    private List<Map<String, Object>> echoList = ListUtil.toList();

    @Schema(description = "属性id")
    private Long id;

    /**
     * 服务ID(结构外键,不参与变更记录 diff)
     */
    @Schema(description = "服务ID")
    @DiffIgnore
    private Long serviceId;
    /**
     * 指示属性编码
     */
    @Schema(description = "指示属性编码")
    private String propertyCode;

    /**
     * 指示属性值
     */
    @Schema(description = "指示属性值")
    private Object propertyValue;

    /**
     * 指示属性名称
     */
    @Schema(description = "指示属性名称")
    private String propertyName;
    /**
     * 指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传
     */
    @Schema(description = "指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_PROPERTY_DATA_TYPE)
    private String datatype;
    /**
     * 属性描述，不影响实际功能，可配置为空字符串。
     */
    @Schema(description = "属性描述，不影响实际功能，可配置为空字符串。")
    private String description;
    /**
     * 指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。
     */
    @Schema(description = "指示枚举值:如开关状态status可有如下取值enumList: [OPEN,CLOSE]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。")
    private String enumlist;
    /**
     * 指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。
     */
    @Schema(description = "指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。")
    private String max;
    /**
     * 指示字符串长度。仅当dataType为string、DateTime时生效。
     */
    @Schema(description = "指示字符串长度。仅当dataType为string、DateTime时生效。")
    private String maxlength;
    /**
     * 指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE
     */
    @Schema(description = "指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_ACCESS_MODE)
    private String method;
    /**
     * 指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。
     */
    @Schema(description = "指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。")
    private String min;
    /**
     * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)
     */
    @Schema(description = "指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_PRODUCT_SERVICE_REQUIRED)
    private String required;
    /**
     * 指示步长。
     */
    @Schema(description = "指示步长。")
    private String step;
    /**
     * 指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”
     */
    @Schema(description = "指示单位。支持长度不超过50。取值根据参数确定，如：•温度单位：“C”或“K”•百分比单位：“%”•压强单位：“Pa”或“kPa”")
    private String unit;
    /**
     * 图标png图
     */
    @Schema(description = "图标png图")
    private String icon;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
