package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName:DashboardView.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 主面板概要信息
 */
@ApiModel(value = "主面板概要信息")
@Data
public class DashboardView extends BaseEntity {

    private static final long serialVersionUID = -1262528746414406709L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "系统版本信息")
    private String version;

    @ApiModelProperty(value = "系统已经运行了多少天")
    private String yxDays;

    @ApiModelProperty(value = "内存已使用百分比")
    private double memPer;

    @ApiModelProperty(value = "更新时间")
    private String dateStr;

}