package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:AppInfo.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: app端口信息
 */
@Data
@ApiModel(value = "app端口信息")
public class AppInfo extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    @ApiModelProperty(value = "主机名")
    private String hostname;

    @ApiModelProperty(value = "应用进程ID")
    private String appPid;

    @ApiModelProperty(value = "应用进程名称")
    private String appName;

    @ApiModelProperty(value = "内存使用M")
    private Double memPer;

    @ApiModelProperty(value = "cpu使用率")
    private Double cpuPer;

    @ApiModelProperty(value = "进程获取途径，1进程id号，2进程pid文件")
    private String appType;

    @ApiModelProperty(value = "进程状态，1正常，2下线")
    private String state;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}