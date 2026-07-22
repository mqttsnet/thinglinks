package com.mqttsnet.thinglinks.productservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import java.util.Optional;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.constant.ThingModelCodeRule;
import com.mqttsnet.thinglinks.product.event.publisher.ProductEventPublisher;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productservice.entity.ProductServices;
import com.mqttsnet.thinglinks.productservice.enumeration.ProductServiceStatusEnum;
import com.mqttsnet.thinglinks.productservice.manager.ProductServiceManager;
import com.mqttsnet.thinglinks.productservice.service.ProductServiceService;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import com.mqttsnet.thinglinks.productservice.vo.save.ProductServiceSaveVO;
import com.mqttsnet.thinglinks.productservice.vo.update.ProductServiceUpdateVO;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductChangeTargetTypeEnum;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 产品模型服务表
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
public class ProductServiceServiceImpl extends SuperServiceImpl<ProductServiceManager, Long, ProductServices> implements ProductServiceService {

    /**
     * 注入只读 {@link ProductQueryService}(独立 bean,零下游 Service 依赖),
     * 切库经过 Service AOP 边界,且类图天然为 DAG,从根本规避反向依赖循环。
     */
    private final ProductQueryService productQueryService;
    private final ProductEventPublisher productEventPublisher;

    /**
     * 保存产品模型服务
     *
     * @param saveVO
     * @return
     */
    @Override
    public ProductServices saveProductService(ProductServiceSaveVO saveVO) {
        log.info("saveProductService saveVO:{}", saveVO);
        //校验参数
        checkedProductServiceSaveVO(saveVO);
        //构建参数
        ProductServices productService = builderProductServiceSaveVO(saveVO);
        //更新
        superManager.save(productService);
        publishChange(ProductVersionChangeTypeEnum.CREATE, null, productService, "新增服务「" + productService.getServiceName() + "」");
        return productService;
    }

    /**
     * 修改产品模型服务
     *
     * @param updateVO
     * @return
     */
    @Override
    public ProductServices updateProductService(ProductServiceUpdateVO updateVO) {
        log.info("updateProductService updateVO:{}", updateVO);
        //校验参数
        checkedProductServiceUpdateVO(updateVO);
        ProductServices before = superManager.getById(updateVO.getId());
        //构建参数
        ProductServices productServices = BeanPlusUtil.toBeanIgnoreError(updateVO, ProductServices.class);
        //更新
        superManager.updateById(productServices);
        ProductServices after = superManager.getById(updateVO.getId());
        publishChange(ProductVersionChangeTypeEnum.UPDATE, before, after, "编辑服务「" + (after != null ? after.getServiceName() : updateVO.getServiceName()) + "」");
        return productServices;
    }

    /**
     * 删除产品模型服务
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteProductService(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ProductServices productService = superManager.getById(id);
        if (null == productService) {
            throw BizException.wrap("The productService does not exist");
        }
        boolean result = superManager.removeById(id);
        publishChange(ProductVersionChangeTypeEnum.DELETE, productService, null, "删除服务「" + productService.getServiceName() + "」");
        return result;
    }

    @Override
    public ProductServices findOneByProductServiceId(Long serviceId) {
        return superManager.findOneByProductServiceId(serviceId);
    }

    @Override
    public List<ProductServices> selectProductServicesList(ProductServices find) {
        return superManager.selectProductServicesList(find);
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedProductServiceSaveVO(ProductServiceSaveVO saveVO) {

        ArgumentAssert.notNull(saveVO.getProductId(), "productId Cannot be null");
        //校验产品模型是否存在
        ArgumentAssert.notNull(productQueryService.findOneByProductId(saveVO.getProductId()), "product not found");
        ArgumentAssert.notBlank(saveVO.getServiceCode(), "serviceCode Cannot be null");
        //校验编码命名规范
        if (!ReUtil.isMatch(ThingModelCodeRule.PATTERN, saveVO.getServiceCode())) {
            throw BizException.wrap(ThingModelCodeRule.PATTERN_MSG);
        }
        //校验CODE
        if (CollUtil.isNotEmpty(superManager.checkCode(saveVO.getProductId(), saveVO.getServiceCode()))) {
            throw BizException.wrap("serviceCode already exists");
        }
        ArgumentAssert.notBlank(saveVO.getServiceName(), "serviceName Cannot be null");
        ArgumentAssert.notBlank(saveVO.getServiceType(), "serviceType Cannot be null");
        //产品模型服务状态
        ArgumentAssert.notNull(saveVO.getServiceStatus(), "serviceStatus Cannot be null");
        ProductServiceStatusEnum.fromValue(saveVO.getServiceStatus()).orElseThrow(() -> BizException.wrap("serviceStatus is not exist"));

    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private ProductServices builderProductServiceSaveVO(ProductServiceSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, ProductServices.class);
    }

    private void publishChange(ProductVersionChangeTypeEnum changeType, ProductServices before, ProductServices after, String summary) {
        ProductServices ref = after != null ? after : before;
        if (ref == null) {
            return;
        }
        Optional.ofNullable(productQueryService.findOneByProductId(ref.getProductId()))
                .map(ProductResultVO::getProductIdentification)
                .ifPresent(pid -> productEventPublisher.publishProductModelChangedEvent(
                        ProductModelChangedSource.builder()
                                .productIdentification(pid)
                                .changeType(changeType)
                                .targetType(ProductChangeTargetTypeEnum.SERVICE)
                                .before(before == null ? null : BeanPlusUtil.toBeanIgnoreError(before, ProductServiceResultVO.class))
                                .after(after == null ? null : BeanPlusUtil.toBeanIgnoreError(after, ProductServiceResultVO.class))
                                .changeSummary(summary)
                                .build()));
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedProductServiceUpdateVO(ProductServiceUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        ArgumentAssert.notNull(updateVO.getProductId(), "productId Cannot be null");
        //校验产品模型是否存在
        ArgumentAssert.notNull(productQueryService.findOneByProductId(updateVO.getProductId()), "product not found");
        ArgumentAssert.notBlank(updateVO.getServiceCode(), "serviceCode Cannot be null");
        //校验编码命名规范
        if (!ReUtil.isMatch(ThingModelCodeRule.PATTERN, updateVO.getServiceCode())) {
            throw BizException.wrap(ThingModelCodeRule.PATTERN_MSG);
        }
        ArgumentAssert.notBlank(updateVO.getServiceName(), "serviceName Cannot be null");
        ArgumentAssert.notBlank(updateVO.getServiceType(), "serviceType Cannot be null");
        //产品模型状态
        ArgumentAssert.notNull(updateVO.getServiceStatus(), "serviceStatus Cannot be null");
        ProductServiceStatusEnum.fromValue(updateVO.getServiceStatus()).orElseThrow(() -> BizException.wrap("serviceStatus is not exist"));
        //校验CODE
        List<ProductServices> productServicesList = superManager.checkCode(updateVO.getProductId(), updateVO.getServiceCode());
        productServicesList.stream()
                .filter(productServices -> !productServices.getId().equals(updateVO.getId()))
                .findAny()
                .ifPresent(productProperty -> {
                    throw BizException.wrap("serviceCode already exists");
                });

    }

}


