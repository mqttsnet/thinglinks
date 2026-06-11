package com.mqttsnet.thinglinks.device.vo.save;

import java.io.Serial;
import java.io.Serializable;

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

/**
 * <p>
 * 表单保存方法VO
 * 设备档案信息表
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
@Schema(title = "DeviceSaveVO", description = "设备档案信息表")
public class DeviceSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端标识
     */
    @Schema(description = "客户端标识")
    @NotEmpty(message = "请填写客户端标识")
    @Size(max = 255, message = "客户端标识长度不能超过{max}")
    private String clientId;
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @NotEmpty(message = "请填写用户名")
    @Size(max = 255, message = "用户名长度不能超过{max}")
    private String userName;
    /**
     * 密码
     */
    @Schema(description = "密码")
    @NotEmpty(message = "请填写密码")
    @Size(max = 255, message = "密码长度不能超过{max}")
    private String password;
    /**
     * 证书序列号
     */
    @Schema(description = "证书序列号")
    @Size(max = 100, message = "证书序列号长度不能超过{max}")
    private String certSerialNumber;
    /**
     * 应用ID
     */
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
    private String appId;
    /**
     * 认证方式0-用户名密码，1-ssl证书
     */
    @Schema(description = "认证方式0-用户名密码，1-ssl证书")
    @NotNull(message = "请填写认证方式0-用户名密码，1-ssl证书")
    private Integer authMode;
    /**
     * 加密密钥
     */
    @Schema(description = "加密密钥")
    @Size(max = 255, message = "加密密钥长度不能超过{max}")
    private String encryptKey;
    /**
     * 加密向量
     */
    @Schema(description = "加密向量")
    @Size(max = 255, message = "加密向量长度不能超过{max}")
    private String encryptVector;
    /**
     * 签名密钥
     */
    @Schema(description = "签名密钥")
    @Size(max = 255, message = "签名密钥长度不能超过{max}")
    private String signKey;
    /**
     * 传输协议的加密方式：0-明文传输、1-SM4、2-AES
     */
    @Schema(description = "传输协议的加密方式：0-明文传输、1-SM4、2-AES")
    @NotNull(message = "请填写传输协议的加密方式：0-明文传输、1-SM4、2-AES")
    private Integer encryptMethod;
    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    @NotEmpty(message = "请填写设备标识")
    @Size(max = 255, message = "设备标识长度不能超过{max}")
    private String deviceIdentification;
    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    @NotEmpty(message = "请填写设备名称")
    @Size(max = 255, message = "设备名称长度不能超过{max}")
    private String deviceName;
    /**
     * 连接实例
     */
    @Schema(description = "连接实例")
    @Size(max = 255, message = "连接实例长度不能超过{max}")
    private String connector;
    /**
     * 设备描述
     */
    @Schema(description = "设备描述")
    @Size(max = 255, message = "设备描述长度不能超过{max}")
    private String description;
    /**
     * 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
     */
    @Schema(description = "设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0")
    @NotNull(message = "请填写设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0")
    private Integer deviceStatus;
    /**
     * 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
     */
    @Schema(description = "连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0")
    @NotNull(message = "请填写连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0")
    private Integer connectStatus;
    /**
     * 设备标签
     */
    @Schema(description = "设备标签")
    @Size(max = 255, message = "设备标签长度不能超过{max}")
    private String deviceTags;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请填写产品标识")
    private String productIdentification;
    /**
     * 软件版本
     */
    @Schema(description = "软件版本")
    @Size(max = 255, message = "软件版本长度不能超过{max}")
    private String swVersion;
    /**
     * 固件版本
     */
    @Schema(description = "固件版本")
    @Size(max = 255, message = "固件版本长度不能超过{max}")
    private String fwVersion;
    /**
     * sdk版本
     */
    @Schema(description = "sdk版本")
    @Size(max = 255, message = "sdk版本长度不能超过{max}")
    private String deviceSdkVersion;
    /**
     * 子设备所属网关的 deviceIdentification（业务唯一标识，String；非主键 id）。
     * 仅 nodeType=SUBDEVICE 时有意义。
     */
    @Schema(description = "网关设备的 deviceIdentification（业务唯一标识，String 类型；非主键 id）")
    private String gatewayId;
    /**
     * 设备类型:0普通设备 || 1网关设备 || 2子设备
     */
    @Schema(description = "设备类型:0普通设备 || 1网关设备 || 2子设备 ", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备类型不能为空")
    private Integer nodeType;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;

    @Schema(description = "设备位置信息")
    private DeviceLocationSaveVO deviceLocationSaveVO;

}
