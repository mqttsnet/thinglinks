package com.mqttsnet.thinglinks.link.api.domain.device.entity;

import com.mqttsnet.thinglinks.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:23$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:23$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
/**
    * CAS规则策略配置表
    */
@ApiModel(value="CAS规则策略配置表")
@Data
public class CasbinRule extends BaseEntity implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Integer id;

    /**
    * 类型
    */
    @ApiModelProperty(value="类型")
    private String ptype;

    /**
    * 规则名称
    */
    @ApiModelProperty(value="规则名称")
    private String v0;

    /**
    * 资源
    */
    @ApiModelProperty(value="资源")
    private String v1;

    /**
    * 动作
    */
    @ApiModelProperty(value="动作")
    private String v2;

    /**
    * 策略
    */
    @ApiModelProperty(value="策略")
    private String v3;

    @ApiModelProperty(value="")
    private String v4;

    @ApiModelProperty(value="")
    private String v5;

    private static final long serialVersionUID = 1L;
}