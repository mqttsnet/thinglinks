package com.mqttsnet.thinglinks.link.controller.api;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 产品Api接口
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product/api")
public class ProductApiController {

    /**
     * 服务对象
     */
    @Resource
    private ProductService productService;


    /**
     * 通过标识查询产品
     * @param productIdentification
     * @return
     */
    @GetMapping("/select-by-product-identification/{productIdentification}")
    public R<Product> selectByProductIdentification(@PathVariable("productIdentification") String productIdentification) {
        return R.ok(productService.selectByProductIdentification(productIdentification));
    }

}
