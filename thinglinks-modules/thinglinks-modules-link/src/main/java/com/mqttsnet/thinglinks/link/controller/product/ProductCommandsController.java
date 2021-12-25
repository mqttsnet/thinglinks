package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommands;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_commands)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product_commands")
public class ProductCommandsController {
/**
* 服务对象
*/
@Resource
private ProductCommandsService productCommandsService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductCommands selectOne(Integer id) {
return productCommandsService.selectByPrimaryKey(id);
}

}
