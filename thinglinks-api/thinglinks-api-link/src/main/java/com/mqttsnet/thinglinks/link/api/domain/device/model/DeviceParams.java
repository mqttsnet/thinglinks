package com.mqttsnet.thinglinks.link.api.domain.device.model;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import lombok.Data;

import java.io.Serializable;

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
    private String clientId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 认证方式
     */
    private String authMode;

    /**
     * 设备标识
     */
    private String deviceIdentification;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 连接实例
     */
    private String connector;

    /**
     * 设备描述
     */
    private String deviceDescription;

    /**
     * 设备状态： 启用 || 禁用
     */
    private String deviceStatus;

    /**
     * 连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
     */
    private String connectStatus;

    /**
     * 是否遗言
     */
    private String isWill;

    /**
     * 设备标签
     */
    private String deviceTags;

    /**
     * 产品标识
     */
    private String productIdentification;


    /**
     * 协议类型 ：mqtt || coap || modbus || http
     */
    private String protocolType;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备位置信息
     */
    private DeviceLocation deviceLocation;

}
