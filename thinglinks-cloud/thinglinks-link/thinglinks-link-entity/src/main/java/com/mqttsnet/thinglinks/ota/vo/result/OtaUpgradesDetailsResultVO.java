package com.mqttsnet.thinglinks.ota.vo.result;

import java.io.Serial;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * OTA升级包详情返回值VO
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:36:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "OtaUpgradesDetailsResultVO", description = "OTA升级包详情")
public class OtaUpgradesDetailsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 包名称
     */
    @Schema(description = "包名称")
    private String packageName;
    /**
     * 升级包类型(0:软件包、1:固件包)
     */
    @Schema(description = "升级包类型(0:软件包、1:固件包)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_PACKAGES_TYPE)
    private Integer packageType;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;
    /**
     * 升级包版本号
     */
    @Schema(description = "升级包版本号")
    private String version;
    /**
     * 升级包的位置
     */
    @Schema(description = "升级包的位置")
    private String fileLocation;

    /**
     * 升级包的签名方法
     */
    @Schema(description = "升级包的签名方法")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_PACKAGES_SIGN_METHOD)
    private Integer signMethod;

    /**
     * 状态
     */
    @Schema(description = "状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_PACKAGES_STATUS)
    private Integer status;
    /**
     * 升级包功能描述
     */
    @Schema(description = "升级包功能描述")
    private String description;
    /**
     * 自定义信息
     */
    @Schema(description = "自定义信息")
    private String customInfo;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;

    /**
     * 产品信息
     */
    @Schema(description = "产品信息")
    private ProductResultVO productResult;

}