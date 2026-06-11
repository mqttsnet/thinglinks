package com.mqttsnet.thinglinks.cache.vo.product;

import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 产品模型缓存VO
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
@Schema(title = "ProductModelCacheVO", description = "产品模型缓存VO")
public class ProductModelCacheVO implements Serializable {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "产品标识")
    private String productIdentification;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品类型")
    private Integer productType;

    @Schema(description = "厂商ID")
    private String manufacturerId;

    @Schema(description = "厂商名称")
    private String manufacturerName;

    @Schema(description = "产品型号")
    private String model;

    @Schema(description = "数据格式")
    private String dataFormat;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "版本序号(系统发布时生成的快照标识)")
    private String activeVersionNo;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "产品描述")
    private String remark;

    @Schema(description = "产品模型服务")
    private List<ProductServiceParamVO> services;

}
