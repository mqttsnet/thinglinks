package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @ClassName:IntrusionInfo.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 检查系统入侵信息
 */
@ApiModel(value = "检查系统入侵信息")
@Data
public class IntrusionInfo extends BaseEntity {

    private static final long serialVersionUID = 879979812204191283L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "系统内核模块")
    private String lsmod;

    @ApiModelProperty(value = "查看passwd文件修改时间")
    private String passwdInfo;

    @ApiModelProperty(value = "查看系统计划任务")
    private String crontab;

    @ApiModelProperty(value = "检查网络，正常网卡不该在promisc模式，可能存在sniffer")
    private String promisc;

    @ApiModelProperty(value = "系统rpc服务")
    private String rpcinfo;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

}