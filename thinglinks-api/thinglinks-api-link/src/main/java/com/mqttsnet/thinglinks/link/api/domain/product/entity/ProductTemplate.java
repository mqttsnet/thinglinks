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
    * 产品模型
    */
@ApiModel(value="产品模板")
@Data
public class ProductTemplate extends BaseEntity implements Serializable {
    /**
    * id
    */
    @ApiModelProperty(value="id")
    private Long id;

    /**
    * 应用ID
    */
    @ApiModelProperty(value="应用ID")
    private String appId;

    /**
    * 产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线
    */
    @ApiModelProperty(value="产品名称:自定义，支持中文、英文大小写、数字、下划线和中划线")
    private String templateName;

    /**
    * 状态(字典值：启用  停用)
    */
    @ApiModelProperty(value="状态(字典值：启用  停用)")
    private String status;

    /**
    * 产品模型模板描述
    */
    @ApiModelProperty(value="产品模型模板描述")
    private String remark;

    private static final long serialVersionUID = 1L;
}