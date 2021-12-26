package com.mqttsnet.thinglinks.link.api.domain.product.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/25$ 23:52$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/25$ 23:52$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
/**
    * 产品模型设备服务命令表
    */
@ApiModel(value="产品模型设备服务命令表")
@Data
public class ProductCommands implements Serializable {
    /**
    * 命令id
    */
    @ApiModelProperty(value="命令id")
    private Long id;

    /**
    * 服务ID
    */
    @ApiModelProperty(value="服务ID")
    private Long serviceId;

    /**
    * 指示命令的名字，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。
支持英文大小写、数字及下划线，长度[2,50]。

    */
    @ApiModelProperty(value="指示命令的名字，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。,支持英文大小写、数字及下划线，长度[2,50]。,")
    private String name;

    /**
    * 命令描述。
    */
    @ApiModelProperty(value="命令描述。")
    private String description;

    /**
    * 创建者
    */
    @ApiModelProperty(value="创建者")
    private String createBy;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime createTime;

    /**
    * 更新者
    */
    @ApiModelProperty(value="更新者")
    private String updateBy;

    /**
    * 更新时间
    */
    @ApiModelProperty(value="更新时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}