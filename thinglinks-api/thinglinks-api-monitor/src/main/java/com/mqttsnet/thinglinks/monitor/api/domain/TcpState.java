package com.mqttsnet.thinglinks.monitor.api.domain;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:TcpState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看TCP连接状态
 */
@ApiModel(value = "查看系统信息")
@Data
public class TcpState extends BaseEntity {

    private static final long serialVersionUID = -299667815095138020L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "每秒本地发起的TCP连接数，既通过connect调用创建的TCP连接；,active/s")
    private String active;

    @ApiModelProperty(value = "每秒远程发起的TCP连接数，即通过accept调用创建的TCP连接,passive/s")
    private String passive;

    @ApiModelProperty(value = "每秒TCP重传数量,retrans/s")
    private String retrans;

    @ApiModelProperty(value = "添加时间")
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