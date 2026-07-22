package com.mqttsnet.thinglinks.link.api.inner;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.link.api.inner.hystrix.ProductOpenInnerApiFallback;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 产品-开放接口API
 *
 * @author mqttsnet
 * @date 2025-06-22
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-link-server}",
        fallback = ProductOpenInnerApiFallback.class, path = "/inner/productOpen")
public interface ProductOpenInnerApi {

    /**
     * 北向API-根据产品标识查询产品详情
     *
     * @param productIdentification 产品标识
     * @return 产品详情信息
     */
    @Operation(summary = "北向API查询产品详情", description = "北向API-根据产品标识查询产品详情")
    @GetMapping(path = "/getProductDetailByNorthbound/{productIdentification}")
    R<ProductResultVO> getProductDetailByNorthbound(@Parameter(description = "产品标识", required = true) @PathVariable("productIdentification") String productIdentification);

    /**
     * 北向API-根据产品标识查询产品物模型（包含服务、属性、命令）
     *
     * @param productIdentification 产品标识
     * @return 产品物模型完整信息
     */
    @Operation(summary = "北向API查询产品物模型", description = "北向API-根据产品标识查询产品物模型（包含服务、属性、命令）")
    @GetMapping(path = "/getProductThingModelByNorthbound/{productIdentification}")
    R<ProductParamVO> getProductThingModelByNorthbound(@Parameter(description = "产品标识", required = true) @PathVariable("productIdentification") String productIdentification);
}
