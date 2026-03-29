package com.mqttsnet.thinglinks.productcommandresponse.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.event.publisher.ProductEventPublisher;
import com.mqttsnet.thinglinks.product.event.source.ProductModelUpdatedEventSource;
import com.mqttsnet.thinglinks.product.manager.ProductManager;
import com.mqttsnet.thinglinks.productcommandresponse.entity.ProductCommandResponse;

import java.util.Collections;
import java.util.Optional;
import com.mqttsnet.thinglinks.productcommandresponse.manager.ProductCommandResponseManager;
import com.mqttsnet.thinglinks.productcommandresponse.service.ProductCommandResponseService;
import com.mqttsnet.thinglinks.productcommandresponse.vo.result.ProductCommandResponseResultVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.save.ProductCommandResponseSaveVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.update.ProductCommandResponseUpdateVO;
import com.mqttsnet.thinglinks.productservice.manager.ProductServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 产品模型服务命令属性响应参数
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductCommandResponseServiceImpl extends SuperServiceImpl<ProductCommandResponseManager, Long, ProductCommandResponse> implements ProductCommandResponseService {

    private final ProductServiceManager productServiceManager;
    private final ProductManager productManager;
    private final ProductEventPublisher productEventPublisher;

    /**
     * 保存产品模型设备响应服务命令属性
     *
     * @param saveVO
     * @return
     */
    @Override
    public ProductCommandResponse saveProductCommandResponse(ProductCommandResponseSaveVO saveVO) {
        log.info("saveProductCommandResponse saveVO:{}", saveVO);
        //校验参数
        checkedProductCommandResponseSaveVO(saveVO);
        //构建参数
        ProductCommandResponse productCommandResponse = builderProductCommandResponseSaveVO(saveVO);
        //更新
        superManager.save(productCommandResponse);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(saveVO.getServiceId());
        return productCommandResponse;
    }

    /**
     * 修改产品模型设备响应服务命令属性
     *
     * @param updateVO
     * @return
     */
    @Override
    public ProductCommandResponse updateProductCommandResponse(ProductCommandResponseUpdateVO updateVO) {
        log.info("updateProductCommandResponse updateVO:{}", updateVO);
        //校验参数
        checkedProductCommandResponseUpdateVO(updateVO);
        //构建参数
        ProductCommandResponse commandResponse = BeanPlusUtil.toBeanIgnoreError(updateVO, ProductCommandResponse.class);
        //更新
        superManager.updateById(commandResponse);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(updateVO.getServiceId());
        return commandResponse;
    }

    @Override
    public Boolean deleteProductCommandResponse(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ProductCommandResponse productCommandResponse = superManager.getById(id);
        if (null == productCommandResponse) {
            throw BizException.wrap("The ProductCommandResponse does not exist");
        }
        boolean result = superManager.removeById(id);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(productCommandResponse.getServiceId());
        return result;
    }

    @Override
    public List<ProductCommandResponseResultVO> selectCommandResponses(List<Long> commandIds) {
        return BeanPlusUtil.toBeanList(superManager.selectCommandResponses(commandIds), ProductCommandResponseResultVO.class);
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    /**
     * 发布产品物模型缓存更新事件
     *
     * @param serviceId 服务ID
     */
    private void publishProductModelCacheEvent(Long serviceId) {
        Optional.ofNullable(productServiceManager.findOneByProductServiceId(serviceId))
                .map(ps -> productManager.findOneByProductId(ps.getProductId()))
                .map(Product::getProductIdentification)
                .ifPresent(identification ->
                        productEventPublisher.publishProductModelUpdatedEvent(ProductModelUpdatedEventSource.builder()
                                .productIdentificationList(Collections.singletonList(identification))
                                .build()));
    }

    private void checkedProductCommandResponseSaveVO(ProductCommandResponseSaveVO saveVO) {
        ArgumentAssert.notNull(saveVO.getServiceId(), "serviceId Cannot be null");
        //校验产品模型服务是否存在
        ArgumentAssert.notNull(productServiceManager.findOneByProductServiceId(saveVO.getServiceId()), "productService not found");
        ArgumentAssert.notNull(saveVO.getCommandId(), "commandId Cannot be null");
        ArgumentAssert.notBlank(saveVO.getDatatype(), "datatype Cannot be null");
        ArgumentAssert.notBlank(saveVO.getRequired(), "required Cannot be null");
        ArgumentAssert.notBlank(saveVO.getParameterCode(), "parameterCode Cannot be null");
        //校验CODE
        if (CollUtil.isNotEmpty(superManager.checkCode(saveVO.getServiceId(), saveVO.getCommandId(), saveVO.getParameterCode()))) {
            throw BizException.wrap("parameterCode already exists");
        }
        ArgumentAssert.notBlank(saveVO.getParameterName(), "parameterName Cannot be null");
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private ProductCommandResponse builderProductCommandResponseSaveVO(ProductCommandResponseSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, ProductCommandResponse.class);
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedProductCommandResponseUpdateVO(ProductCommandResponseUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        ArgumentAssert.notNull(updateVO.getServiceId(), "serviceId Cannot be null");
        //校验产品模型是否存在
        ArgumentAssert.notNull(productServiceManager.findOneByProductServiceId(updateVO.getServiceId()), "productService not found");
        ArgumentAssert.notNull(updateVO.getCommandId(), "commandId Cannot be null");
        ArgumentAssert.notBlank(updateVO.getDatatype(), "datatype Cannot be null");
        ArgumentAssert.notBlank(updateVO.getRequired(), "required Cannot be null");
        ArgumentAssert.notBlank(updateVO.getParameterCode(), "parameterCode Cannot be null");
        ArgumentAssert.notBlank(updateVO.getParameterName(), "parameterName Cannot be null");
        //校验CODE
        List<ProductCommandResponse> productCommandResponses = superManager.checkCode(updateVO.getServiceId(), updateVO.getCommandId(), updateVO.getParameterCode());
        productCommandResponses.stream()
                .filter(productCommandResponse -> !productCommandResponse.getId().equals(updateVO.getId()))
                .findAny()
                .ifPresent(productProperty -> {
                    throw BizException.wrap("parameterCode already exists");
                });
    }

}


