package com.mqttsnet.thinglinks.link.api.domain.device.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mqttsnet.thinglinks.common.core.annotation.Excel;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Device Entity class model
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/5/4$ 18:57$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/5/4$ 18:57$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class DeviceParams implements Serializable {
    private static final long serialVersionUID = 1L;

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
    @ApiModelProperty(value = "认证方式", example = "0", notes = "0-用户名密码，1-ssl证书")
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
    @ApiModelProperty(value = "设备状态", example = "ENABLE", notes = "启用 || 禁用")
    private String deviceStatus;

    /**
     * 连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
     */
    @ApiModelProperty(value = "连接状态", example = "ONLINE", notes = "在线：ONLINE || 离线：OFFLINE || 未连接：INIT")
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
     * 设备位置信息
     */
    @ApiModelProperty(value = "设备位置信息")
    private DeviceLocation deviceLocation;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;

}
