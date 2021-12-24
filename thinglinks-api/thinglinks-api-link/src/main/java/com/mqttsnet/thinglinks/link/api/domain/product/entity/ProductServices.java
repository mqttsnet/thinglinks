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
* @CreateDate:     2021/12/23$ 18:40$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/23$ 18:40$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
/**
    * 产品模型服务表
    */
@ApiModel(value="产品模型服务表")
@Data
public class ProductServices implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Long id;

    /**
    * 服务名称:支持英文大小写、数字、下划线和中划线

    */
    @ApiModelProperty(value="服务名称:支持英文大小写、数字、下划线和中划线,")
    private String service_id;

    /**
    * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串""。

    */
    @ApiModelProperty(value="服务的描述信息:文本描述，不影响实际功能，可配置为空字符串''。,")
    private String description;

    /**
    * 产品模型设备服务指令表ID：指示设备可以执行的命令，如果本服务无命令则配置为[]。
    */
    @ApiModelProperty(value="产品模型设备服务指令表ID：指示设备可以执行的命令，如果本服务无命令则配置为[]。")
    private Long commands_id;

    /**
    * 产品模型服务属性表ID
    */
    @ApiModelProperty(value="产品模型服务属性表ID")
    private Long properties_id;

    /**
    * 创建者
    */
    @ApiModelProperty(value="创建者")
    private String create_by;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private LocalDateTime create_time;

    /**
    * 更新者
    */
    @ApiModelProperty(value="更新者")
    private String update_by;

    /**
    * 更新时间
    */
    @ApiModelProperty(value="更新时间")
    private LocalDateTime update_time;

    private static final long serialVersionUID = 1L;
}