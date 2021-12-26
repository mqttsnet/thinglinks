package com.mqttsnet.thinglinks.collection.entity;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * @ClassName:SysLoadState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: uptime查看系统负载状态
 */
@ApiModel(value = "uptime查看系统负载状态")
@Data
public class SysLoadState extends BaseEntity {

    private static final long serialVersionUID = -4863071148000213553L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "1分钟之前到现在的负载")
    private Double oneLoad;

    @ApiModelProperty(value = "5分钟之前到现在的负载")
    private Double fiveLoad;

    @ApiModelProperty(value = "15分钟之前到现在的负载")
    private Double fifteenLoad;

    @ApiModelProperty(value = "登录用户数量 废弃")
    private String users;

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