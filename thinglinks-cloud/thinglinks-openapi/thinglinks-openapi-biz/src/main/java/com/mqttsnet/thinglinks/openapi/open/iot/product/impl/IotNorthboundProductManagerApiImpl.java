package com.mqttsnet.thinglinks.openapi.open.iot.product.impl;

import com.gitee.sop.support.context.OpenContext;
import com.gitee.sop.support.exception.OpenException;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.link.facade.ProductOpenInnerFacade;
import com.mqttsnet.thinglinks.openapi.enumeration.ErrorStoryMessageEnum;
import com.mqttsnet.thinglinks.openapi.open.iot.product.IotNorthboundProductManagerApi;
import com.mqttsnet.thinglinks.openapi.open.iot.product.converter.ProductThingModelConverter;
import com.mqttsnet.thinglinks.openapi.open.iot.product.req.IotNorthboundProductGetDetailRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.product.req.IotNorthboundProductGetThingModelRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.product.resp.IotNorthboundProductGetDetailResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.product.resp.IotNorthboundProductGetThingModelResponse;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Description:
 * 物联网北向API-产品管理实现类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/01/22
 */
@DubboService
@Slf4j
public class IotNorthboundProductManagerApiImpl implements IotNorthboundProductManagerApi {
    @Resource
    private ProductOpenInnerFacade productOpenInnerFacade;

    @Override
    public IotNorthboundProductGetDetailResponse getProductDetail(IotNorthboundProductGetDetailRequest request, OpenContext context) {
        log.info("getProductDetail...params: appId={}, tenantId={}, productIdentification={}",
                context.getAppId(), context.getTenantId(), request.getProductIdentification());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 调用 Facade 查询产品详情
        var result = productOpenInnerFacade.getProductDetailByNorthbound(request.getProductIdentification());
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.PRODUCT_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.PRODUCT_QUERY_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.PRODUCT_QUERY_FAILED.getSolution());
        }

        // 转换返回结果
        ProductResultVO productResultVO = result.getData();
        return BeanPlusUtil.toBean(productResultVO, IotNorthboundProductGetDetailResponse.class);
    }

    @Override
    public IotNorthboundProductGetThingModelResponse getProductThingModel(IotNorthboundProductGetThingModelRequest request, OpenContext context) {
        log.info("getProductThingModel...params: appId={}, tenantId={}, productIdentification={}",
                context.getAppId(), context.getTenantId(), request.getProductIdentification());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 调用 Facade 查询产品物模型
        var result = productOpenInnerFacade.getProductThingModelByNorthbound(request.getProductIdentification());
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.PRODUCT_THING_MODEL_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.PRODUCT_THING_MODEL_QUERY_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.PRODUCT_THING_MODEL_QUERY_FAILED.getSolution());
        }

        // 转换返回结果
        ProductParamVO productParamVO = result.getData();
        return ProductThingModelConverter.convert(productParamVO);
    }

}
