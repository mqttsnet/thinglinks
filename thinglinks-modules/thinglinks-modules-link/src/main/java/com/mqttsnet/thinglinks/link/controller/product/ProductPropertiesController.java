package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.service.product.ProductPropertiesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_properties)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product_properties")
public class ProductPropertiesController {
/**
* 服务对象
*/
@Resource
private ProductPropertiesService productPropertiesService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductProperties selectOne(Integer id) {
return productPropertiesService.selectByPrimaryKey(id);
}

}
