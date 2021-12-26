package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/26$ 13:20$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/26$ 13:20$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
/**
    * 设备动作数据
    */
@ApiModel(value="设备动作数据")
@Data
public class DeviceAction implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Long id;

    /**
    * 设备标识
    */
    @ApiModelProperty(value="设备标识")
    private String deviceIdentification;

    /**
    * 动作类型
    */
    @ApiModelProperty(value="动作类型")
    private String actionType;

    /**
    * 状态
    */
    @ApiModelProperty(value="状态")
    private String status;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime createTime;

    /**
    * 内容信息
    */
    @ApiModelProperty(value="内容信息")
    private String message;

    private static final long serialVersionUID = 1L;
}