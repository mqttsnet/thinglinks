package com.mqttsnet.thinglinks.productcommandrequest.service.impl;

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
import com.mqttsnet.thinglinks.productcommandrequest.entity.ProductCommandRequest;
import com.mqttsnet.thinglinks.productcommandrequest.manager.ProductCommandRequestManager;
import com.mqttsnet.thinglinks.productcommandrequest.service.ProductCommandRequestService;
import com.mqttsnet.thinglinks.productcommandrequest.vo.result.ProductCommandRequestResultVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.save.ProductCommandRequestSaveVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.update.ProductCommandRequestUpdateVO;
import com.mqttsnet.thinglinks.productservice.service.ProductServiceService;
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
 * 产品模型服务命令属性请求参数
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
public class ProductCommandRequestServiceImpl extends SuperServiceImpl<ProductCommandRequestManager, Long, ProductCommandRequest> implements ProductCommandRequestService {

    private final ProductServiceService productServiceService;
    /**
     * 注入只读 {@link ProductQueryService}(独立 bean,零下游 Service 依赖),
     * 切库经过 Service AOP 边界,且类图天然为 DAG,从根本规避反向依赖循环。
     */
    private final ProductQueryService productQueryService;
    private final ProductEventPublisher productEventPublisher;

    /**
     * 保存产品模型设备下发服务命令属性
     *
     * @param saveVO
     * @return
     */
    @Override
    public ProductCommandRequest saveProductCommandRequest(ProductCommandRequestSaveVO saveVO) {
        log.info("saveProductCommandRequest saveVO:{}", saveVO);
        //校验参数
        checkedProductCommandRequestSaveVO(saveVO);
        //构建参数
        ProductCommandRequest productCommandRequest = builderProductCommandRequestSaveVO(saveVO);
        //更新
        superManager.save(productCommandRequest);
        publishChange(ProductVersionChangeTypeEnum.CREATE, null, productCommandRequest, "新增命令请求参数「" + productCommandRequest.getParameterName() + "」");
        return productCommandRequest;
    }

    /**
     * 修改产品模型设备下发服务命令属性
     *
     * @param updateVO
     * @return
     */
    @Override
    public ProductCommandRequest updateProductCommandRequest(ProductCommandRequestUpdateVO updateVO) {
        log.info("updateProductCommandRequest updateVO:{}", updateVO);
        //校验参数
        checkedProductCommandRequestUpdateVO(updateVO);
        ProductCommandRequest before = superManager.getById(updateVO.getId());
        //构建参数
        ProductCommandRequest productCommandRequest = BeanPlusUtil.toBeanIgnoreError(updateVO, ProductCommandRequest.class);
        //更新
        superManager.updateById(productCommandRequest);
        ProductCommandRequest after = superManager.getById(updateVO.getId());
        publishChange(ProductVersionChangeTypeEnum.UPDATE, before, after, "编辑命令请求参数「" + (after != null ? after.getParameterName() : updateVO.getParameterName()) + "」");
        return productCommandRequest;
    }

    @Override
    public Boolean deleteProductCommandRequest(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ProductCommandRequest productCommandRequest = superManager.getById(id);
        if (null == productCommandRequest) {
            throw BizException.wrap("The productCommandRequest does not exist");
        }
        boolean result = superManager.removeById(id);
        publishChange(ProductVersionChangeTypeEnum.DELETE, productCommandRequest, null, "删除命令请求参数「" + productCommandRequest.getParameterName() + "」");
        return result;
    }

    @Override
    public List<ProductCommandRequestResultVO> selectCommandRequests(List<Long> commandIds) {
        return BeanPlusUtil.toBeanList(superManager.selectCommandRequests(commandIds), ProductCommandRequestResultVO.class);
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedProductCommandRequestSaveVO(ProductCommandRequestSaveVO saveVO) {
        ArgumentAssert.notNull(saveVO.getServiceId(), "serviceId Cannot be null");
        //校验产品模型服务是否存在
        ArgumentAssert.notNull(productServiceService.findOneByProductServiceId(saveVO.getServiceId()), "productService not found");
        ArgumentAssert.notNull(saveVO.getCommandId(), "commandId Cannot be null");
        ArgumentAssert.notBlank(saveVO.getDatatype(), "datatype Cannot be null");
        ArgumentAssert.notBlank(saveVO.getRequired(), "required Cannot be null");
        ArgumentAssert.notBlank(saveVO.getParameterCode(), "parameterCode Cannot be null");
        //校验编码命名规范
        if (!ReUtil.isMatch(ThingModelCodeRule.PATTERN, saveVO.getParameterCode())) {
            throw BizException.wrap(ThingModelCodeRule.PATTERN_MSG);
        }
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
    private ProductCommandRequest builderProductCommandRequestSaveVO(ProductCommandRequestSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, ProductCommandRequest.class);
    }

    private void publishChange(ProductVersionChangeTypeEnum changeType, ProductCommandRequest before, ProductCommandRequest after, String summary) {
        ProductCommandRequest ref = after != null ? after : before;
        if (ref == null) {
            return;
        }
        Optional.ofNullable(productServiceService.findOneByProductServiceId(ref.getServiceId()))
                .map(ps -> productQueryService.findOneByProductId(ps.getProductId()))
                .map(ProductResultVO::getProductIdentification)
                .ifPresent(pid -> productEventPublisher.publishProductModelChangedEvent(
                        ProductModelChangedSource.builder()
                                .productIdentification(pid)
                                .changeType(changeType)
                                .targetType(ProductChangeTargetTypeEnum.COMMAND)
                                .before(before == null ? null : BeanPlusUtil.toBeanIgnoreError(before, ProductCommandRequestResultVO.class))
                                .after(after == null ? null : BeanPlusUtil.toBeanIgnoreError(after, ProductCommandRequestResultVO.class))
                                .changeSummary(summary)
                                .build()));
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedProductCommandRequestUpdateVO(ProductCommandRequestUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        ArgumentAssert.notNull(updateVO.getServiceId(), "serviceId Cannot be null");
        //校验产品模型是否存在
        ArgumentAssert.notNull(productServiceService.findOneByProductServiceId(updateVO.getServiceId()), "productService not found");
        ArgumentAssert.notNull(updateVO.getCommandId(), "commandId Cannot be null");
        ArgumentAssert.notBlank(updateVO.getDatatype(), "datatype Cannot be null");
        ArgumentAssert.notBlank(updateVO.getRequired(), "required Cannot be null");
        ArgumentAssert.notBlank(updateVO.getParameterCode(), "parameterCode Cannot be null");
        //校验编码命名规范
        if (!ReUtil.isMatch(ThingModelCodeRule.PATTERN, updateVO.getParameterCode())) {
            throw BizException.wrap(ThingModelCodeRule.PATTERN_MSG);
        }
        ArgumentAssert.notBlank(updateVO.getParameterName(), "parameterName Cannot be null");
        //校验CODE
        List<ProductCommandRequest> productCommandRequests = superManager.checkCode(updateVO.getServiceId(), updateVO.getCommandId(), updateVO.getParameterCode());
        productCommandRequests.stream()
                .filter(productCommandRequest -> !productCommandRequest.getId().equals(updateVO.getId()))
                .findAny()
                .ifPresent(productProperty -> {
                    throw BizException.wrap("parameterCode already exists");
                });
    }

}


