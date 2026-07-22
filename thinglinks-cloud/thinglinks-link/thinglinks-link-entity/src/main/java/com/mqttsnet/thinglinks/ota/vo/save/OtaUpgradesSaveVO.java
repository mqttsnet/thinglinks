package com.mqttsnet.thinglinks.ota.vo.save;

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
 * OTA升级包
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
@EqualsAndHashCode
@Builder
@Schema(title = "OtaUpgradesSaveVO", description = "OTA升级包")
public class OtaUpgradesSaveVO implements Serializable {

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
     * 包名称
     */
    @Schema(description = "包名称")
    @NotEmpty(message = "请填写包名称")
    @Size(max = 100, message = "包名称长度不能超过{max}")
    private String packageName;
    /**
     * 升级包类型(0:软件包、1:固件包)
     */
    @Schema(description = "升级包类型(0:软件包、1:固件包)")
    @NotNull(message = "请填写升级包类型(0:软件包、1:固件包)")
    private Integer packageType;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    @NotEmpty(message = "请填写产品标识")
    @Size(max = 100, message = "产品标识长度不能超过{max}")
    private String productIdentification;
    /**
     * 升级包版本号
     */
    @Schema(description = "升级包版本号")
    @NotEmpty(message = "请填写升级包版本号")
    @Size(max = 255, message = "升级包版本号长度不能超过{max}")
    private String version;
    /**
     * 产品版本序号
     */
    @Schema(description = "产品版本序号")
    @NotEmpty(message = "请选择产品版本序号")
    @Size(max = 64, message = "产品版本序号长度不能超过{max}")
    private String productVersionNo;
    /**
     * 升级包的位置
     */
    @Schema(description = "升级包的位置")
    @NotEmpty(message = "请填写升级包的位置")
    @Size(max = 255, message = "升级包的位置长度不能超过{max}")
    private String fileLocation;


    @Schema(description = "签名方法")
    @NotNull(message = "请填写签名方法")
    private Integer signMethod;
    /**
     * 状态
     */
    @Schema(description = "状态")
    @NotNull(message = "请填写状态")
    private Integer status;
    /**
     * 升级包功能描述
     */
    @Schema(description = "升级包功能描述")
    @Size(max = 255, message = "升级包功能描述长度不能超过{max}")
    private String description;
    /**
     * 自定义信息
     */
    @Schema(description = "自定义信息")
    @Size(max = 2147483647, message = "自定义信息长度不能超过{max}")
    private String customInfo;
    /**
     * 描述
     */
    @Schema(description = "描述")
    @Size(max = 255, message = "描述长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
