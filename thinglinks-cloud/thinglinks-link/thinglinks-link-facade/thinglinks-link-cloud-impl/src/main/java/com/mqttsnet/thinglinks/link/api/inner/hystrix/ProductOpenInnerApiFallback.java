package com.mqttsnet.thinglinks.link.api.inner.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.api.inner.ProductOpenInnerApi;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;

/**
 * 产品开放接口降级处理
 *
 * @author mqttsnet
 * @date 2025-06-22
 */
public class ProductOpenInnerApiFallback implements ProductOpenInnerApi {
    @Override
    public R<ProductResultVO> getProductDetailByNorthbound(String productIdentification) {
        return R.timeout();
    }

    @Override
    public R<ProductParamVO> getProductThingModelByNorthbound(String productIdentification) {
        return R.timeout();
    }
}
