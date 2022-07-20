/**
  * Copyright 2021 json.cn
  */
package com.mqttsnet.thinglinks.link.api.domain.product.model;

import lombok.Data;

/**
 * 产品模型服务属性详情对象 Properties
 *
 * @author thinglinks
 * @date 2021-12-23
 */
@Data
public class Properties{
    private static final long serialVersionUID = 1L;
    private Long id;
    private String datatype;
    private String description;
    private String enumlist;
    private Integer max;
    private Integer maxlength;
    private String method;
    private Integer min;
    private String name;
    private Integer required;
    private Integer step;
    private String unit;
}
