package com.mqttsnet.thinglinks.monitor.api.domain;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName:MemState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看内存使用情况
 */
@ApiModel(value = "查看磁盘IO使用情况")
@Data
public class MemState extends BaseEntity {

    private static final long serialVersionUID = -1412473355088780549L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "总计内存，M")
    private String total;

    @ApiModelProperty(value = "已使用多少，M")
    private String used;

    @ApiModelProperty(value = "未使用，M")
    private String free;

    @ApiModelProperty(value = "已使用百分比%")
    private Double usePer;

    @ApiModelProperty(value = "添加时间 yyyy-MM-dd hh:mm:ss")
    private String dateStr;

    @ApiModelProperty(value = "创建时间 ")
    private Date createTime;

    public String getDateStr() {
        if (!StringUtils.isEmpty(dateStr) && dateStr.length() > 16) {
            return dateStr.substring(5);
        }
        return dateStr;
    }

}