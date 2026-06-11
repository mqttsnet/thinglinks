package com.mqttsnet.thinglinks.anyuser.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品相关开放接口（anyUser）
 * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
 *
 * @author mqttsnet
 * @date 2025-06-22
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/anyUser/productOpen")
@Tag(name = "anyUser-产品相关API")
public class ProductOpenAnyUserController {

    @Resource
    private ProductService productService;

    @Resource
    private EchoService echoService;

    /**
     * 北向API-根据产品标识查询产品详情
     *
     * @param productIdentification 产品标识
     * @return 产品详情信息
     */
    @Operation(summary = "北向API查询产品详情", description = "北向API-根据产品标识查询产品详情")
    @GetMapping("/getProductDetailByNorthbound/{productIdentification}")
    @WebLog("北向API查询产品详情")
    public R<ProductResultVO> getProductDetailByNorthbound(@Parameter(description = "产品标识", required = true) @PathVariable("productIdentification") String productIdentification) {
        try {
            log.info("getProductDetailByNorthbound productIdentification:{}", productIdentification);
            ProductResultVO result = productService.findOneByProductIdentification(productIdentification);
            echoService.action(result);
            return R.success(result);
        } catch (Exception e) {
            log.error("查询产品详情失败,productIdentification:{}", productIdentification, e);
            return R.fail("查询产品详情失败: " + e.getMessage());
        }
    }

    /**
     * 北向API-根据产品标识查询产品物模型（包含已启用的服务、属性、命令）
     *
     * @param productIdentification 产品标识
     * @return 产品物模型完整信息
     */
    @Operation(summary = "北向API查询产品物模型", description = "北向API-根据产品标识查询产品物模型（包含已启用的服务、属性、命令）")
    @GetMapping("/getProductThingModelByNorthbound/{productIdentification}")
    @WebLog("北向API查询产品物模型")
    public R<ProductParamVO> getProductThingModelByNorthbound(@Parameter(description = "产品标识", required = true) @PathVariable("productIdentification") String productIdentification) {
        try {
            log.info("getProductThingModelByNorthbound productIdentification:{}", productIdentification);
            ProductParamVO result = productService.selectFullProductByProductIdentification(productIdentification);
            return R.success(result);
        } catch (Exception e) {
            log.error("查询产品物模型失败,productIdentification:{}", productIdentification, e);
            return R.fail("查询产品物模型失败: " + e.getMessage());
        }
    }

}
