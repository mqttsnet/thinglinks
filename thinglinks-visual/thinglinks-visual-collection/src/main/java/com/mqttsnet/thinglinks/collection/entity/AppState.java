package com.mqttsnet.thinglinks.collection.entity;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * @ClassName:AppState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: app状态监控
 */
@ApiModel(value = "app状态监控")
@Data
public class AppState extends BaseEntity {

    private static final long serialVersionUID = -2913111613773445949L;

    @ApiModelProperty(value = "应用信息ID")
    private String appInfoId;

    @ApiModelProperty(value = "%CPU")
    private Double cpuPer;

    @ApiModelProperty(value = "%MEM")
    private Double memPer;

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