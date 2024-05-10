package com.mqttsnet.thinglinks.link.api.domain.cache.device;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductCacheVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

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
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "DeviceCacheVO", description = "设备档案缓存VO")
public class DeviceCacheVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 客户端标识
     */
    @ApiModelProperty(value = "客户端标识")
    private String clientId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    private String appId;

    /**
     * 认证方式
     */
    @ApiModelProperty(value = "认证方式")
    private String authMode;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;

    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 连接实例
     */
    @ApiModelProperty(value = "连接实例")
    private String connector;

    /**
     * 设备描述
     */
    @ApiModelProperty(value = "设备描述")
    private String deviceDescription;

    /**
     * 设备状态： 启用 || 禁用
     */
    @ApiModelProperty(value = "设备状态： 启用 || 禁用")
    private String deviceStatus;

    /**
     * 连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
     */
    @ApiModelProperty(value = "连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT,")
    private String connectStatus;

    /**
     * 是否遗言
     */
    @ApiModelProperty(value = "是否遗言")
    private String isWill;

    /**
     * 设备标签
     */
    @ApiModelProperty(value = "设备标签")
    private String deviceTags;

    /**
     * 产品标识
     */
    @ApiModelProperty(value = "产品标识")
    private String productIdentification;


    /**
     * 协议类型 ：mqtt || coap || modbus || http
     */
    @ApiModelProperty(value = "协议类型 ：mqtt || coap || modbus || http")
    private String protocolType;

    /**
     * 设备类型
     */
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 加密密钥
     */
    @ApiModelProperty(value = "加密密钥")
    private String encryptKey;
    /**
     * 加密向量
     */
    @ApiModelProperty(value = "加密向量")
    private String encryptVector;
    /**
     * 签名密钥
     */
    @ApiModelProperty(value = "签名密钥")
    private String signKey;
    /**
     * 传输协议的加密方式：0-明文传输、1-SM4、2-AES
     */
    @ApiModelProperty(value = "传输协议的加密方式：0-明文传输、1-SM4、2-AES ")
    private String encryptMethod;

    /**
     * 软件版本
     */
    @ApiModelProperty(value = "软件版本")
    private String swVersion;
    /**
     * 固件版本
     */
    @ApiModelProperty(value = "固件版本")
    private String fwVersion;
    /**
     * sdk版本
     */
    @ApiModelProperty(value = "sdk版本")
    private String deviceSdkVersion;

    /**
     * 设备产品基础信息
     */
    @ApiModelProperty(value = "设备产品基础信息")
    private ProductCacheVO productCacheVO;

}
