package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:HeathMonitor.java
 * @author: shisen
 * @date: 2021年12月26日
 * @Description: app端口信息
 */
@ApiModel(value = "app端口信息")
@Data
public class HeathMonitor extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    @ApiModelProperty(value = "应用服务名称")
    private String appName;

    @ApiModelProperty(value = "心跳检测Url")
    private String heathUrl;

    @ApiModelProperty(value = "状态")
    private String heathStatus;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}