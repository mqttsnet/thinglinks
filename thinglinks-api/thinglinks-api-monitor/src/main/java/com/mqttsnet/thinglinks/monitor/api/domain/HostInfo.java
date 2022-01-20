package com.mqttsnet.thinglinks.monitor.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:HostInfo.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: host的IP密码等信息
 */
@ApiModel(value = "host的IP密码等信息")
@Data
public class HostInfo extends BaseEntity {

    private static final long serialVersionUID = 3875927332935900938L;

    @ApiModelProperty(value = "host名称")
    private String ip;

    @ApiModelProperty(value = "用户")
    private String root;

    @ApiModelProperty(value = "ssh端口")
    private String port;

    @ApiModelProperty(value = "密码")
    private String passwd;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}