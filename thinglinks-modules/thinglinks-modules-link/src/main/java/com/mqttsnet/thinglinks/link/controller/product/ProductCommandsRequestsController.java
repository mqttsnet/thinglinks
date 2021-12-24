package com.mqttsnet.thinglinks.link.controller.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsRequestsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_commands_requests)表控制层
*
* @author thinglinks
*/
@RestController
@RequestMapping("product_commands_requests")
public class ProductCommandsRequestsController {
/**
* 服务对象
*/
@Resource
private ProductCommandsRequestsService productCommandsRequestsService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductCommandsRequests selectOne(Long id) {
return productCommandsRequestsService.selectByPrimaryKey(id);
}

}
