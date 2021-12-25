package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsResponseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_commands_response)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product_commands_response")
public class ProductCommandsResponseController {
/**
* 服务对象
*/
@Resource
private ProductCommandsResponseService productCommandsResponseService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductCommandsResponse selectOne(Integer id) {
return productCommandsResponseService.selectByPrimaryKey(id);
}

}
