package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product")
public class ProductController {
/**
* 服务对象
*/
@Resource
private ProductService productService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public Product selectOne(Long id) {
return productService.selectByPrimaryKey(id);
}

}
