package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:SystemInfo.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看系统信息
 */
@ApiModel(value = "查看系统信息")
@Data
public class SystemInfo extends BaseEntity {

    private static final long serialVersionUID = 879979812204191283L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "系统版本信息")
    private String version;

    @ApiModelProperty(value = "系统版本详细信息")
    private String versionDetail;

    @ApiModelProperty(value = "内存使用率")
    private Double memPer;

    @ApiModelProperty(value = "core的个数(即核数)")
    private String cpuCoreNum;

    @ApiModelProperty(value = "cpu使用率")
    private Double cpuPer;

    @ApiModelProperty(value = "CPU型号信息")
    private String cpuXh;

    @ApiModelProperty(value = "主机状态，1正常，2下线")
    private String state;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "主机备注")
    private String remark;

    @ApiModelProperty(value = "磁盘总使用率")
    private Double diskPer;

    private Integer memPerLe;

    private Integer  cpuPerLe;
}