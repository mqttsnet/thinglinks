package com.mqttsnet.thinglinks.cache.vo.device;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
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
 * 设备档案缓存VO
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
@Schema(title = "DeviceCacheVO", description = "设备档案缓存VO")
public class DeviceCacheVO extends Entity<Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "id")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    /**
     * 客户端标识
     */
    @Schema(description = "客户端标识")
    private String clientId;
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;
    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 证书序列号
     */
    @Schema(description = "证书序列号")
    private String certSerialNumber;
    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 认证方式0-用户名密码，1-ssl证书
     */
    @Schema(description = "认证方式0-用户名密码，1-ssl证书")
    private Integer authMode;
    /**
     * 加密密钥
     */
    @Schema(description = "加密密钥")
    private String encryptKey;
    /**
     * 加密向量
     */
    @Schema(description = "加密向量")
    private String encryptVector;
    /**
     * 签名密钥
     */
    @Schema(description = "签名密钥")
    private String signKey;
    /**
     * 传输协议的加密方式：0-明文传输、1-SM4、2-AES
     */
    @Schema(description = "传输协议的加密方式：0-明文传输、1-SM4、2-AES")
    private Integer encryptMethod;
    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;
    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;
    /**
     * 连接实例
     */
    @Schema(description = "连接实例")
    private String connector;
    /**
     * 设备描述
     */
    @Schema(description = "设备描述")
    private String description;
    /**
     * 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
     */
    @Schema(description = "设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0")
    private Integer deviceStatus;
    /**
     * 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
     */
    @Schema(description = "连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0")
    private Integer connectStatus;
    /**
     * 设备标签
     */
    @Schema(description = "设备标签")
    private String deviceTags;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;
    /**
     * 软件版本
     */
    @Schema(description = "软件版本")
    private String swVersion;
    /**
     * 固件版本
     */
    @Schema(description = "固件版本")
    private String fwVersion;
    /**
     * sdk版本
     */
    @Schema(description = "sdk版本")
    private String deviceSdkVersion;
    /**
     * 网关设备id
     */
    @Schema(description = "网关设备id")
    private String gatewayId;
    /**
     * 设备类型:0普通设备 || 1网关设备 || 2子设备
     */
    @Schema(description = "设备类型:0普通设备 || 1网关设备 || 2子设备 ")
    private Integer nodeType;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;

    /**
     * 绑定的产品版本号(对应 product_version.version_no)。
     */
    @Schema(description = "绑定的产品版本号")
    private String boundProductVersionNo;

}
