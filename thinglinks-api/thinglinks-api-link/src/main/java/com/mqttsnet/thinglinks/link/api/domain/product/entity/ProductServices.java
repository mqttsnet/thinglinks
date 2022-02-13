package com.mqttsnet.thinglinks.link.api.domain.product.entity;

import com.mqttsnet.thinglinks.common.core.web.domain.BaseEntity;
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
    * 产品模型服务表
    */
@ApiModel(value="产品模型服务表")
@Data
public class ProductServices extends BaseEntity implements Serializable {
    /**
    * 服务id
    */
    @ApiModelProperty(value="服务id")
    private Long id;

    /**
    * 服务名称:支持英文大小写、数字、下划线和中划线

    */
    @ApiModelProperty(value="服务名称:支持英文大小写、数字、下划线和中划线,")
    private String serviceName;

    /**
    * 产品ID
    */
    @ApiModelProperty(value="产品ID")
    private Long productId;

    /**
    * 产品模型模板ID
    */
    @ApiModelProperty(value="产品模型模板ID")
    private Long templateId;

    /**
    * 状态(字典值：启用  停用)
    */
    @ApiModelProperty(value="状态(字典值：启用  停用)")
    private String status;

    /**
    * 服务的描述信息:文本描述，不影响实际功能，可配置为空字符串""。

    */
    @ApiModelProperty(value="服务的描述信息:文本描述，不影响实际功能，可配置为空字符串''。,")
    private String description;


    private static final long serialVersionUID = 1L;
}