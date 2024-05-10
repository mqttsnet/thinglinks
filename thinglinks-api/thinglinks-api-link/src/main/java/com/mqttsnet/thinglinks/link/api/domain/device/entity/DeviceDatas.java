package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    * 设备消息
    */
@ApiModel(value="设备消息")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class DeviceDatas implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Long id;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;

    /**
     * 协议类型 ：mqtt || coap || modbus || http
     */
    @ApiModelProperty(value = "协议类型 ：mqtt || coap || modbus || http")
    private String protocolType;

    /**
     * 消息ID
     */
    @ApiModelProperty(value = "消息ID")
    private String messageId;

    /**
     * topic
     */
    @ApiModelProperty(value = "topic")
    private String topic;

    /**
     * 内容信息
     */
    @ApiModelProperty(value = "内容信息")
    private String message;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 上报时间
     */
    @ApiModelProperty(value = "上报时间")
    private LocalDateTime reportTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}