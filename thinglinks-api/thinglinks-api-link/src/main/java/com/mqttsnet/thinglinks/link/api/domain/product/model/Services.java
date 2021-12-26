/**
  * Copyright 2021 json.cn 
  */
package com.mqttsnet.thinglinks.link.api.domain.product.model;
import lombok.Data;

import java.util.List;

/**
 * 产品模型对象 Services
 *
 * @author thinglinks
 * @date 2021-12-23
 */
@Data
public class Services{
    private static final long serialVersionUID = 1L;
    private String serviceId;
    private String description;
    private List<String> commands;
    private List<Properties> properties;

}