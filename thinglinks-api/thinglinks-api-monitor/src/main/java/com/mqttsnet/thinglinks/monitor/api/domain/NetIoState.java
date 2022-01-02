package com.mqttsnet.thinglinks.monitor.api.domain;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:NetIoState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 网络设备的吞吐率
 */
@ApiModel(value = "网络设备的吞吐率")
@Data
public class NetIoState extends BaseEntity {

    private static final long serialVersionUID = -8314012397341825158L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "每秒钟接收的数据包,rxpck/s")
    private String rxpck;

    @ApiModelProperty(value = "每秒钟发送的数据包,txpck/s")
    private String txpck;

    @ApiModelProperty(value = "每秒钟接收的KB数,txpck/s")
    private String rxbyt;

    @ApiModelProperty(value = "每秒钟发送的KB数,txkB/s")
    private String txbyt;

    @ApiModelProperty(value = "每秒钟接收的压缩数据包,rxcmp/s")
    private String rxcmp;

    @ApiModelProperty(value = "每秒钟发送的压缩数据包,txcmp/s")
    private String txcmp;

    @ApiModelProperty(value = "每秒钟接收的多播数据包,rxmcst/s")
    private String rxmcst;

    @ApiModelProperty(value = "添加时间 yyyy-MM-dd hh:mm:ss")
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