package com.mqttsnet.thinglinks.link.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;

/**
 * 产品-开放接口API
 *
 * @author mqttsnet
 * @date 2025-06-22
 */
public interface ProductOpenInnerFacade {

    /**
     * 北向API-根据产品标识查询产品详情
     *
     * @param productIdentification 产品标识
     * @return {@link R<ProductResultVO>} 产品详情信息
     */
    R<ProductResultVO> getProductDetailByNorthbound(String productIdentification);

    /**
     * 北向API-根据产品标识查询产品物模型（包含服务、属性、命令）
     *
     * @param productIdentification 产品标识
     * @return {@link R<ProductParamVO>} 产品物模型完整信息
     */
    R<ProductParamVO> getProductThingModelByNorthbound(String productIdentification);
}
