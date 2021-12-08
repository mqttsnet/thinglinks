package com.mqttsnet.thinglinks.link.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**

* @Description:    java类作用描述

* @Author:         ShiHuan Sun

* @E-mail:          13733918655@163.com

* @Website:         http://thinglinks.mqttsnet.com

* @CreateDate:     2021/11/18$ 9:41$

* @UpdateUser:     ShiHuan Sun

* @UpdateDate:     2021/11/18$ 9:41$

* @UpdateRemark:   修改内容

* @Version:        1.0

*/

/**
    * 设备消息
    */
@ApiModel(value="设备消息")
@Data
public class MqttsDeviceDatas implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Long id;

    /**
    * 设备标识
    */
    @ApiModelProperty(value="设备标识")
    private String device_id;

    /**
    * 消息ID
    */
    @ApiModelProperty(value="消息ID")
    private String message_id;

    /**
    * topic
    */
    @ApiModelProperty(value="topic")
    private String topic;

    /**
    * 状态
    */
    @ApiModelProperty(value="状态")
    private String status;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime create_time;

    /**
    * 内容信息
    */
    @ApiModelProperty(value="内容信息")
    private String message;

    private static final long serialVersionUID = 1L;
}