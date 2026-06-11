package com.mqttsnet.thinglinks.product.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(title = "ProductSaveVO", description = "产品模型")
public class ProductSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
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
    @NotEmpty(message = "请填写产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线")
    @Size(max = 255, message = "产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线长度不能超过{max}")
    private String productName;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    @NotEmpty(message = "请填写产品标识")
    @Size(max = 100, message = "产品标识长度不能超过{max}")
    private String productIdentification;
    /**
     * 支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品
     */
    @Schema(description = "支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品")
    @NotNull(message = "请填写支持以下两种产品类型1•COMMON：普通产品，需直连设备。2•GATEWAY：网关产品，可挂载子设备。 0其他未知产品")
    private Integer productType;
    /**
     * 厂商ID:支持英文大小写，数字，下划线和中划线
     */
    @Schema(description = "厂商ID:支持英文大小写，数字，下划线和中划线")
    @NotEmpty(message = "请填写厂商ID:支持英文大小写，数字，下划线和中划线")
    @Size(max = 255, message = "厂商ID:支持英文大小写，数字，下划线和中划线长度不能超过{max}")
    private String manufacturerId;
    /**
     * 厂商名称 :支持中文、英文大小写、数字、下划线和中划线
     */
    @Schema(description = "厂商名称 :支持中文、英文大小写、数字、下划线和中划线")
    @NotEmpty(message = "请填写厂商名称 :支持中文、英文大小写、数字、下划线和中划线")
    @Size(max = 255, message = "厂商名称 :支持中文、英文大小写、数字、下划线和中划线长度不能超过{max}")
    private String manufacturerName;
    /**
     * 产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线
     */
    @Schema(description = "产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线")
    @NotEmpty(message = "请填写产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线")
    @Size(max = 255, message = "产品型号，建议包含字母或数字来保证可扩展性。支持英文大小写、数字、下划线和中划线长度不能超过{max}")
    private String model;
    /**
     * 数据格式，默认为JSON无需修改。
     */
    @Schema(description = "数据格式，默认为JSON无需修改。")
    @NotEmpty(message = "请填写数据格式，默认为JSON无需修改。")
    @Size(max = 255, message = "数据格式，默认为JSON无需修改。长度不能超过{max}")
    private String dataFormat;
    /**
     * 设备类型:支持英文大小写、数字、下划线和中划线
     */
    @Schema(description = "设备类型:支持英文大小写、数字、下划线和中划线")
    @NotEmpty(message = "请填写设备类型:支持英文大小写、数字、下划线和中划线")
    @Size(max = 255, message = "设备类型:支持英文大小写、数字、下划线和中划线长度不能超过{max}")
    private String deviceType;
    /**
     * 设备接入平台的协议类型，默认为MQTT无需修改。 
     */
    @Schema(description = "设备接入平台的协议类型，默认为MQTT无需修改。")
    @NotEmpty(message = "请填写设备接入平台的协议类型，默认为MQTT无需修改。")
    @Size(max = 255, message = "设备接入平台的协议类型，默认为MQTT无需修改。长度不能超过{max}")
    private String protocolType;
    /**
     * 状态(字典值：0启用  1停用)
     */
    @Schema(description = "状态(字典值：0启用  1停用)")
    @NotNull(message = "请填写状态(字典值：0启用  1停用)")
    private Integer productStatus;
    /**
     * 版本序号(系统在发布时自动生成,创建产品时无需手填;若客户端误传,服务端会忽略并以
     * publish 流程生成的雪花值为准)。
     */
    @Schema(description = "版本序号(系统发布时生成,创建产品时无需手填,客户端传入会被忽略)")
    @Size(max = 255, message = "版本序号长度不能超过{max}")
    private String activeVersionNo;
    /**
     * 图标
     */
    @Schema(description = "图标")
    @Size(max = 100, message = "图标长度不能超过{max}")
    private String icon;
    /**
     * 产品描述
     */
    @Schema(description = "产品描述")
    @Size(max = 500, message = "产品描述长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
