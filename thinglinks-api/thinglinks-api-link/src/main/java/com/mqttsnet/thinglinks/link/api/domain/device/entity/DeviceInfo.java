package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* @Description: 子设备档案表
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/4/25$ 12:48$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/4/25$ 12:48$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@ApiModel(value="子设备档案表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfo implements Serializable {
    /**
    * 主键
    */
    @ApiModelProperty(value="主键")
    private Long id;

    /**
    * 边设备主键
    */
    @ApiModelProperty(value="边设备主键")
    private Long dId;

    /**
     * 边设备唯一标识
     */
    @ApiModelProperty(value="边设备唯一标识")
    private String edgeDevicesIdentification;

    /**
    * 应用ID
    */
    @ApiModelProperty(value="应用ID")
    private String appId;

    /**
    * 设备自身的唯一标识
    */
    @ApiModelProperty(value="设备自身的唯一标识")
    private String nodeId;

    /**
    * 设备名称
    */
    @ApiModelProperty(value="设备名称")
    private String nodeName;

    /**
    * 平台生成的设备唯一标识
    */
    @ApiModelProperty(value="平台生成的设备唯一标识")
    private String deviceId;

    /**
    * 设备描述
    */
    @ApiModelProperty(value="设备描述")
    private String description;

    /**
    * 厂商ID
    */
    @ApiModelProperty(value="厂商ID")
    private String manufacturerId;

    /**
    * 设备型号
    */
    @ApiModelProperty(value="设备型号")
    private String model;

    /**
    * 子设备连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT
    */
    @ApiModelProperty(value="子设备连接状态 : 在线：ONLINE || 离线：OFFLINE || 未连接：INIT")
    private String connectStatus;

    /**
    * 是否支持设备影子TRUE:1、FALSE :0
    */
    @ApiModelProperty(value="是否支持设备影子TRUE:1、FALSE :0")
    private Boolean shadowEnable;

    /**
    * 设备影子数据表名
    */
    @ApiModelProperty(value="设备影子数据表名")
    private String shadowTableName;

    /**
    * 状态(字典值：0启用  1停用)
    */
    @ApiModelProperty(value="状态(字典值：0启用  1停用)")
    private String status;

    /**
    * 创建者
    */
    @ApiModelProperty(value="创建者")
    private String createBy;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime createTime;

    /**
    * 更新者
    */
    @ApiModelProperty(value="更新者")
    private String updateBy;

    /**
    * 更新时间
    */
    @ApiModelProperty(value="更新时间")
    private LocalDateTime updateTime;

    /**
    * 备注
    */
    @ApiModelProperty(value="备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}