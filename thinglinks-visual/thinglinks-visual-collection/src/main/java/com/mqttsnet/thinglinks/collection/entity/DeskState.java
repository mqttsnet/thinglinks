package com.mqttsnet.thinglinks.collection.entity;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * @ClassName:DeskState.java
 * @author: shisen
 * @date: 2021年12月24日
 * @Description: 查看磁盘大小使用信息
 */
@ApiModel(value = "查看磁盘大小使用信息")
@Data
public class DeskState extends BaseEntity {

    private static final long serialVersionUID = 879979812204191283L;

    @ApiModelProperty(value = "host名称")
    private String hostname;

    @ApiModelProperty(value = "盘符类型")
    private String fileSystem;

    @ApiModelProperty(value = "分区大小")
    private String size;

    @ApiModelProperty(value = "已使用")
    private String used;

    @ApiModelProperty(value = "可用")
    private String avail;

    @ApiModelProperty(value = "已使用百分比")
    private String usePer;

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