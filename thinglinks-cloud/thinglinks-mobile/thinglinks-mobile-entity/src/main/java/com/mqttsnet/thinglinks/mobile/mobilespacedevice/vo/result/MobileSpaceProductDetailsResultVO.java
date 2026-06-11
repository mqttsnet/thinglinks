package com.mqttsnet.thinglinks.mobile.mobilespacedevice.vo.result;

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

/**
 * @program: thinglinks-cloud
 * @description: 移动端-空间产品详情结果VO
 * @packagename: com.mqttsnet.thinglinks.mobilespacedevice.vo.result
 * @author: mqttsnet
 * @e-mainl: 13733918655@163.com
 * @date: 2024-10-12 16:22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "MobileSpaceProductDetailsResultVO", description = "移动端-空间产品详情结果VO")
public class MobileSpaceProductDetailsResultVO extends AuditableResultVO {

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
     * 产品版本
     */
    @Schema(description = "产品版本")
    private String activeVersionNo;
    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;
    /**
     * 产品描述
     */
    @Schema(description = "产品描述")
    private String remark;

    @Schema(description = "空间设备详情信息集合")
    private List<MobileSpaceDeviceDetailsResultVO> spaceDeviceDetailsResultVOList;
}
