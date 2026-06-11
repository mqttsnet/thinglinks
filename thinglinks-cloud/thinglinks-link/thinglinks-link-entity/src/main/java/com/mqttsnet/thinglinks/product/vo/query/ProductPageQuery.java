package com.mqttsnet.thinglinks.product.vo.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * 表单查询条件VO
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
@Builder(toBuilder = true)
@Schema(title = "ProductPageQuery", description = "产品模型")
public class ProductPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 产品id
     */
    @Schema(description = "产品id")
    private Long templateId;
    /**
     * 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
     */
    @Schema(description = "产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线")
    private String productName;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;

    /**
     * 产品标识集合
     */
    @Schema(description = "产品标识集合")
    private List<String> productIdentificationList;
    /**
     * 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
     */
    @Schema(description = "支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品")
    private Integer productType;
    /**
     * 厂商ID:支持英文大小写，数字，下划线和中划线
     */
    @Schema(description = "厂商ID:支持英文大小写，数字，下划线和中划线")
    private String manufacturerId;
    /**
     * 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
     */
    @Schema(description = "厂商名称 :支持中文、英文大小写、数字、下划线和中划线")
    private String manufacturerName;
    /**
     * 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
     */
    @Schema(description = "产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线")
    private String model;
    /**
     * 数据格式，默认为JSON无需修改。
     */
    @Schema(description = "数据格式，默认为JSON无需修改。")
    private String dataFormat;
    /**
     * 设备类型:支持英文大小写、数字、下划线和中划线
     */
    @Schema(description = "设备类型:支持英文大小写、数字、下划线和中划线")
    private String deviceType;
    /**
     * 设备接入平台的协议类型，默认为MQTT无需修改。
     */
    @Schema(description = "设备接入平台的协议类型，默认为MQTT无需修改。")
    private String protocolType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @Schema(description = "状态(字典值：0启用  1停用)")
    private Integer productStatus;
    /**
     * 按版本序号精确匹配过滤(系统发布生成的快照标识)。
     */
    @Schema(description = "版本序号(系统发布时生成的快照标识,等值匹配)")
    private String activeVersionNo;
    /**
     * 产品描述
     */
    @Schema(description = "产品描述")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
