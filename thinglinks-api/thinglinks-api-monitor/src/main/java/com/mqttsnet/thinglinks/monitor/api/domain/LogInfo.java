package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:LogInfo.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 日志信息
 */
@ApiModel(value = "日志信息")
@Data
public class LogInfo extends BaseEntity {

    private static final long serialVersionUID = 1565538727002722890L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "描述")
    private String infoContent;

    @ApiModelProperty(value = "0成功，1失败")
    private String state;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}