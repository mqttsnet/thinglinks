package com.mqttsnet.thinglinks.mqs.transform.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 脚本绑定 · 设备上下文 DTO。
 *
 * <p>从 {@code DeviceCacheVO} 映射而来,作为前置转换 Groovy 脚本里 {@code device} 绑定变量的承载体。
 * 刻意做成独立 DTO 而非直接传缓存 VO:① 绝不在原 {@code DeviceCacheVO} 上抠字段污染缓存;
 * ② 不含 {@code password}(敏感且转换脚本用不到);③ 不暴露 {@code echoMap} 等缓存内部字段。
 *
 * <p>脚本里用法:{@code device.signKey} / {@code device.encryptMethod} / {@code device.deviceSdkVersion} …
 *
 * @author mqttsnet
 */
@Data
public class ScriptDeviceContextDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String clientId;
    private String userName;
    private String certSerialNumber;
    private String appId;
    /** 认证方式 0-用户名密码 1-ssl证书 */
    private Integer authMode;
    /** 加密密钥 */
    private String encryptKey;
    /** 加密向量 */
    private String encryptVector;
    /** 签名密钥 */
    private String signKey;
    /** 传输协议加密方式:0-明文 1-SM4 2-AES */
    private Integer encryptMethod;
    private String deviceIdentification;
    private String deviceName;
    private String connector;
    private String description;
    private Integer deviceStatus;
    private Integer connectStatus;
    private String deviceTags;
    private String productIdentification;
    private String swVersion;
    private String fwVersion;
    private String deviceSdkVersion;
    private String gatewayId;
    /** 设备类型:0普通 1网关 2子设备 */
    private Integer nodeType;
    private String remark;
    private Long createdOrgId;
    /** 绑定的产品发布版本号 */
    private String boundProductVersionNo;

    // 刻意不含 password —— 敏感字段不进脚本上下文
}
