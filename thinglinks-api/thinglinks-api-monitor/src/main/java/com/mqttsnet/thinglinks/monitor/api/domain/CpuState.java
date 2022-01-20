package com.mqttsnet.thinglinks.monitor.api.domain;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:CpuState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看CPU使用情况
 */
@ApiModel(value = "查看CPU使用情况")
@Data
public class CpuState extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "用户态的CPU时间（%）废弃")
    private String user;

    @ApiModelProperty(value = "cpu使用率")
    private Double sys;

    @ApiModelProperty(value = "当前空闲率")
    private Double idle;

    @ApiModelProperty(value = "cpu当前等待率")
    private Double iowait;

    @ApiModelProperty(value = "硬中断时间（%） 废弃")
    private String irq;

    @ApiModelProperty(value = "软中断时间（%） 废弃")
    private String soft;

    @ApiModelProperty(value = "添加时间 MM-dd hh:mm:ss")
    private String dateStr;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    public String getDateStr() {
        if (!StringUtils.isEmpty(dateStr) && dateStr.length() > 16) {
            return dateStr.substring(5);
        }
        return dateStr;
    }
}