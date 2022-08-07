package com.mqttsnet.thinglinks.link.api.domain.device.entity.deviceInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @program: thinglinks
 * @description: 子设备信息
 * @packagename: com.mqttsnet.thinglinks.link.api.domain.device.entity
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-29 17:54
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfoParams {

    /**
     * 主键
     */
    private Long id;

    /**
     * 边设备主键
     */
    @NotNull(message = "边设备主键不能为空")
    private Long did;

    /**
     * 应用ID
     */
    @NotBlank(message = "应用ID不能为空")
    private String appId;

    /**
     * 设备节点ID
     */
    @NotBlank(message = "设备节点ID不能为空")
    private String nodeId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String nodeName;

    /**
     * 子设备唯一标识
     */
    private String deviceId;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 厂商ID
     */
    private String manufacturerId;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 子设备连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
     */
    private String connectStatus;

    /**
     * 是否支持设备影子TRUE:1、FALSE :0
     */
    private Boolean shadowEnable;


    /**
     * 状态(字典值：0启用  1停用)
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

}
