package com.mqttsnet.thinglinks.collection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:DiskIoState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看磁盘IO使用情况
 */
@ApiModel(value = "查看磁盘IO使用情况")
@Data
public class MailSet extends BaseEntity {

    private static final long serialVersionUID = -8284741180883299533L;

    @ApiModelProperty(value = "是否发送邮件告警,1发送0不发送")
    private String sendMail;

    @ApiModelProperty(value = "发送邮箱的帐号")
    private String fromMailName;

    @ApiModelProperty(value = "发送邮箱的密码")
    private String fromPwd;

    @ApiModelProperty(value = "发送邮箱的SMTP服务器")
    private String smtpHost;

    @ApiModelProperty(value = "发送邮箱的SMTP端口,25或465")
    private String smtpPort;

    @ApiModelProperty(value = "发送邮箱是否启用安全链接(SSL),1启用,0不启用")
    private String smtpSSL;

    @ApiModelProperty(value = "接受告警信息的邮件")
    private String toMail;

    @ApiModelProperty(value = "cpu使用率告警值")
    private Integer cpuPer;

    @ApiModelProperty(value = "mem使用率告警值")
    private Integer memPer;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}