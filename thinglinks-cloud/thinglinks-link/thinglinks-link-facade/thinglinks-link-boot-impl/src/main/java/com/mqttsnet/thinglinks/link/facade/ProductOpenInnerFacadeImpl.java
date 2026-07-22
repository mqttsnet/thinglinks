package com.mqttsnet.thinglinks.link.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2026/1/29
 */
@Slf4j
@Service
public class ProductOpenInnerFacadeImpl implements ProductOpenInnerFacade {

    @Autowired
    private ProductService productService;


    @Override
    public R<ProductResultVO> getProductDetailByNorthbound(String productIdentification) {
        try {
            return R.success(productService.findOneByProductIdentification(productIdentification));
        } catch (Exception e) {
            return R.fail("Error processing get product detail by northbound event: " + e.getMessage());
        }
    }

    @Override
    public R<ProductParamVO> getProductThingModelByNorthbound(String productIdentification) {
        try {
            return R.success(productService.selectFullProductByProductIdentification(productIdentification));
        } catch (Exception e) {
            return R.fail("Error processing get product thing model by northbound event: " + e.getMessage());
        }
    }

}
