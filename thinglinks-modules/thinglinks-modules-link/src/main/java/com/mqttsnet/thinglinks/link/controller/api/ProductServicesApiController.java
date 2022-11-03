package com.mqttsnet.thinglinks.link.controller.api;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.security.annotation.PreAuthorize;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 产品服务Api接口
 *
 * @author thinglinks
 */
@RestController
@RequestMapping("/product-services/api")
public class ProductServicesApiController {

    /**
     * 服务对象
     */
    @Resource
    private ProductServicesService productServicesService;

    /**
     * 获取产品模型服务详细信息
     */
    @GetMapping(value = "/{id}")
    public R<ProductServices> selectById(@PathVariable("id") Long id) {
        return R.ok(productServicesService.selectProductServicesById(id));
    }
}
