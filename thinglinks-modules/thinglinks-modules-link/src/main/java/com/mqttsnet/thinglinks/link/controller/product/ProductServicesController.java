package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_services)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product_services")
public class ProductServicesController {
/**
* 服务对象
*/
@Resource
private ProductServicesService productServicesService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductServices selectOne(Integer id) {
return productServicesService.selectByPrimaryKey(id);
}

}
