package com.mqttsnet.thinglinks.productproperty.service.impl;

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
import com.mqttsnet.thinglinks.productproperty.entity.ProductProperty;

import java.util.Collections;
import java.util.Optional;
import com.mqttsnet.thinglinks.productproperty.enumeration.DataTypeEnum;
import com.mqttsnet.thinglinks.productproperty.manager.ProductPropertyManager;
import com.mqttsnet.thinglinks.productproperty.service.ProductPropertyService;
import com.mqttsnet.thinglinks.productproperty.vo.save.ProductPropertySaveVO;
import com.mqttsnet.thinglinks.productproperty.vo.update.ProductPropertyUpdateVO;
import com.mqttsnet.thinglinks.productservice.manager.ProductServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 产品模型服务属性表
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
public class ProductPropertyServiceImpl extends SuperServiceImpl<ProductPropertyManager, Long, ProductProperty> implements ProductPropertyService {

    private final ProductServiceManager productServiceManager;
    private final ProductManager productManager;
    private final ProductEventPublisher productEventPublisher;

    /**
     * 保存产品模型服务属性
     *
     * @param saveVO
     * @return
     */
    @Override
    public ProductProperty saveProductProperty(ProductPropertySaveVO saveVO) {
        log.info("saveProductProperty saveVO:{}", saveVO);
        //校验参数
        checkedProductPropertySaveVO(saveVO);
        //构建参数
        ProductProperty productProperty = builderProductPropertySaveVO(saveVO);
        //更新
        superManager.save(productProperty);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(saveVO.getServiceId());
        return productProperty;
    }

    /**
     * 修改产品模型服务属性
     *
     * @param updateVO
     * @return
     */
    @Override
    public ProductProperty updateProductProperty(ProductPropertyUpdateVO updateVO) {
        log.info("updateProductProperty updateVO:{}", updateVO);
        //校验参数
        checkedProductPropertyUpdateVO(updateVO);
        //构建参数
        ProductProperty productProperty = BeanPlusUtil.toBeanIgnoreError(updateVO, ProductProperty.class);
        //更新
        superManager.updateById(productProperty);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(updateVO.getServiceId());
        return productProperty;
    }

    @Override
    public Boolean deleteProductProperty(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ProductProperty productProperty = superManager.getById(id);
        if (null == productProperty) {
            throw BizException.wrap("The productProperty does not exist");
        }
        boolean result = superManager.removeById(id);
        // 发布产品物模型更新事件
        publishProductModelCacheEvent(productProperty.getServiceId());
        return result;
    }

    @Override
    public List<ProductProperty> findAllByServiceId(Long serviceId) {
        return superManager.findAllByServiceId(serviceId);
    }

    @Override
    public List<ProductProperty> findAllByServiceIds(List<Long> serviceIds) {
        return superManager.findAllByServiceIds(serviceIds);
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

    private void checkedProductPropertySaveVO(ProductPropertySaveVO saveVO) {
        ArgumentAssert.notNull(saveVO.getServiceId(), "serviceId Cannot be null");
        //校验产品模型服务是否存在
        ArgumentAssert.notNull(productServiceManager.findOneByProductServiceId(saveVO.getServiceId()), "productService not found");
        ArgumentAssert.notBlank(saveVO.getPropertyCode(), "propertyCode Cannot be null");
        //校验CODE
        if (CollUtil.isNotEmpty(superManager.checkCode(saveVO.getServiceId(), saveVO.getPropertyCode()))) {
            throw BizException.wrap("propertyCode already exists");
        }
        ArgumentAssert.notBlank(saveVO.getPropertyName(), "propertyName Cannot be null");
        ArgumentAssert.notBlank(saveVO.getDatatype(), "datatype Cannot be null");
        if (!DataTypeEnum.TYPE_COLLECTION.contains(saveVO.getDatatype())) {
            throw BizException.wrap("datatype does not exist");
        }
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private ProductProperty builderProductPropertySaveVO(ProductPropertySaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, ProductProperty.class);
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedProductPropertyUpdateVO(ProductPropertyUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        ArgumentAssert.notNull(updateVO.getServiceId(), "serviceId Cannot be null");
        ArgumentAssert.notBlank(updateVO.getPropertyCode(), "propertyCode Cannot be null");
        ArgumentAssert.notBlank(updateVO.getPropertyName(), "propertyName Cannot be null");
        ArgumentAssert.notBlank(updateVO.getDatatype(), "datatype Cannot be null");
        if (!DataTypeEnum.TYPE_COLLECTION.contains(updateVO.getDatatype())) {
            throw BizException.wrap("datatype does not exist");
        }
        //校验CODE
        List<ProductProperty> productProperties = superManager.checkCode(updateVO.getServiceId(), updateVO.getPropertyCode());
        productProperties.stream()
                .filter(productProperty -> !productProperty.getId().equals(updateVO.getId()))
                .findAny()
                .ifPresent(productProperty -> {
                    throw BizException.wrap("propertyCode already exists");
                });
    }

}


