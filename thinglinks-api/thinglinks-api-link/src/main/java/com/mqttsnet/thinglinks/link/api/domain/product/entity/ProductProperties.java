package com.mqttsnet.thinglinks.link.api.domain.product.entity;

import com.mqttsnet.thinglinks.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/25$ 23:52$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/25$ 23:52$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
/**
    * 产品模型服务属性表
    */
@ApiModel(value="产品模型服务属性表")
@Data
public class ProductProperties extends BaseEntity implements Serializable {
    /**
    * 属性id
    */
    @ApiModelProperty(value="属性id")
    private Long id;

    /**
    * 指示属性名称。
    */
    @ApiModelProperty(value="指示属性名称。")
    private String name;

    /**
    * 服务ID
    */
    @ApiModelProperty(value="服务ID")
    private Long serviceId;

    /**
    * 指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：
•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传

    */
    @ApiModelProperty(value="指示数据类型：取值范围：string、int、decimal（float和double都可以使用此类型）、DateTime、jsonObject上报数据时，复杂类型数据格式如下：,•DateTime:yyyyMMdd’T’HHmmss’Z’如:20151212T121212Z•jsonObject：自定义json结构体，平台不理解只透传,")
    private String datatype;

    /**
    * 属性描述，不影响实际功能，可配置为空字符串""。
    */
    @ApiModelProperty(value="属性描述，不影响实际功能，可配置为空字符串''。")
    private String description;

    /**
    * 指示枚举值:如开关状态status可有如下取值"enumList" : ["OPEN","CLOSE"]目前本字段是非功能性字段，仅起到描述作用。建议准确定义。

    */
    @ApiModelProperty(value="指示枚举值:如开关状态status可有如下取值'enumList' : ['OPEN','CLOSE']目前本字段是非功能性字段，仅起到描述作用。建议准确定义。,")
    private String enumlist;

    /**
    * 指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。

    */
    @ApiModelProperty(value="指示最大值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑小于等于。,")
    private Integer max;

    /**
    * 指示字符串长度。仅当dataType为string、DateTime时生效。

    */
    @ApiModelProperty(value="指示字符串长度。仅当dataType为string、DateTime时生效。,")
    private Integer maxlength;

    /**
    * 指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE

    */
    @ApiModelProperty(value="指示访问模式。R:可读；W:可写；E属性值更改时上报数据取值范围：R、RW、RE、RWE,")
    private String method;

    /**
    * 指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。

    */
    @ApiModelProperty(value="指示最小值。支持长度不超过50的数字。仅当dataType为int、decimal时生效，逻辑大于等于。,")
    private Integer min;

    /**
    * 指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)。
    */
    @ApiModelProperty(value="指示本条属性是否必填，取值为0或1，默认取值1（必填）。目前本字段是非功能性字段，仅起到描述作用。(字典值link_product_isRequired：0非必填 1必填)")
    private Integer required;

    /**
    * 指示步长。
    */
    @ApiModelProperty(value="指示步长。")
    private Integer step;

    /**
    * 指示单位。支持长度不超过50。
取值根据参数确定，如：
•温度单位：“C”或“K”
•百分比单位：“%”
•压强单位：“Pa”或“kPa”

    */
    @ApiModelProperty(value="指示单位。支持长度不超过50。,取值根据参数确定，如：,•温度单位：“C”或“K”,•百分比单位：“%”,•压强单位：“Pa”或“kPa”,")
    private String unit;


    private static final long serialVersionUID = 1L;
}
