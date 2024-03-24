package com.mqttsnet.thinglinks.link.api.domain.product.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 表单保存方法VO
 * 产品模型
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
@ApiModel(value = "ProductParamVO", description = "产品模型参数VO")
public class ProductParamVO implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;



    @ApiModelProperty(value = "应用ID")
    private String appId;

    @ApiModelProperty(value = "产品标识")
    private String productIdentification;

    @ApiModelProperty(value = "模板ID")
    private Long templateId;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "产品类型")
    private Integer productType;

    @ApiModelProperty(value = "厂商ID")
    private String manufacturerId;

    @ApiModelProperty(value = "厂商名称")
    private String manufacturerName;

    @ApiModelProperty(value = "产品型号")
    private String model;

    @ApiModelProperty(value = "数据格式")
    private String dataFormat;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "协议类型")
    private String protocolType;

    @ApiModelProperty(value = "产品版本")
    private String productVersion;

    @ApiModelProperty(value = "产品描述")
    private String remark;

    @ApiModelProperty(value = "产品模型服务")
    private List<ProductServiceParamVO> services;

}
