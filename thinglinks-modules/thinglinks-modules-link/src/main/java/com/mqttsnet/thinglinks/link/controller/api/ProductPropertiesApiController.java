package com.mqttsnet.thinglinks.link.controller.api;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.service.product.ProductPropertiesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 属性Api接口
 * @author thinglinks
 */
@RestController
@RequestMapping("/product-properties/api")
public class ProductPropertiesApiController {

    /**
     * 服务对象
     */
    @Resource
    private ProductPropertiesService productPropertiesService;

    /**
     * 根据属性ID查询属性详情
     */
    @GetMapping(value = "/{id}")
    public R<ProductProperties> getInfo(@PathVariable("id") Long id) {
        return R.ok(productPropertiesService.selectProductPropertiesById(id));
    }

}
