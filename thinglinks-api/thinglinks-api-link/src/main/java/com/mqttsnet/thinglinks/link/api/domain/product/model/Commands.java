/**
  * Copyright 2021 json.cn 
  */
package com.mqttsnet.thinglinks.link.api.domain.product.model;
import lombok.Data;

import java.util.List;

/**
 * 产品模型服务命令对象 Commands
 *
 * @author thinglinks
 * @date 2021-12-23
 */
@Data
public class Commands {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private List<Properties> requests;
    private List<Properties> responses;

}