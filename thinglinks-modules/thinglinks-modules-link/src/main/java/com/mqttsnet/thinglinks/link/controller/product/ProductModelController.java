package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductModel;
import com.mqttsnet.thinglinks.link.service.product.ProductModelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_model)表控制层
*
* @author thinglinks
*/
@RestController
@RequestMapping("product_model")
public class ProductModelController {
/**
* 服务对象
*/
@Resource
private ProductModelService productModelService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductModel selectOne(Long id) {
return productModelService.selectByPrimaryKey(id);
}

}
