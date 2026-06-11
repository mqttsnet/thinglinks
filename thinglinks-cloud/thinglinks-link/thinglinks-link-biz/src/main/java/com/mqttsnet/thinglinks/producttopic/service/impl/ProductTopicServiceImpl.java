package com.mqttsnet.thinglinks.producttopic.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.producttopic.config.ProductTopicTemplate;
import com.mqttsnet.thinglinks.producttopic.config.ProductTopicTemplateConfig;
import com.mqttsnet.thinglinks.producttopic.entity.ProductTopic;
import com.mqttsnet.thinglinks.producttopic.enumeration.ProductTopicTypeEnum;
import com.mqttsnet.thinglinks.producttopic.manager.ProductTopicManager;
import com.mqttsnet.thinglinks.producttopic.service.ProductTopicService;
import com.mqttsnet.thinglinks.producttopic.vo.save.ProductTopicSaveVO;
import com.mqttsnet.thinglinks.producttopic.vo.update.ProductTopicUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * 产品Topic信息表
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
public class ProductTopicServiceImpl extends SuperServiceImpl<ProductTopicManager, Long, ProductTopic> implements ProductTopicService {

    private final ProductTopicTemplateConfig topicTemplateConfig;

    @Override
    protected <UpdateVO> ProductTopic updateBefore(UpdateVO vo) {
        ProductTopicUpdateVO updateVO = (ProductTopicUpdateVO) vo;

        if (superManager.count(Wraps.<ProductTopic>lbQ()
                .eq(ProductTopic::getProductIdentification, updateVO.getProductIdentification())
                .eq(ProductTopic::getTopic, updateVO.getTopic())
                .ne(ProductTopic::getId, updateVO.getId())) > 0) {
            throw BizException.wrap("Topic已存在");
        }

        return super.updateBefore(updateVO);
    }

    @Override
    protected <SaveVO> ProductTopic saveBefore(SaveVO vo) {
        ProductTopicSaveVO saveVO = (ProductTopicSaveVO) vo;

        if (superManager.count(Wraps.<ProductTopic>lbQ()
                .eq(ProductTopic::getProductIdentification, saveVO.getProductIdentification())
                .eq(ProductTopic::getTopic, saveVO.getTopic())) > 0) {
            throw BizException.wrap("Topic已存在");
        }

        return super.saveBefore(saveVO);
    }


    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, ProductTopic entity) {
//        superManager.refreshProductTopicCache(Collections.singletonList(entity));
    }

    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, ProductTopic entity) {
//        superManager.refreshProductTopicCache(Collections.singletonList(entity));
    }


    /**
     * 初始化产品基础Topic
     *
     * @param productIdentification 产品标识
     * @param productTypeEnum       产品类型枚举
     * @param reInit                是否重新初始化
     */
    @Override
    public void initProductBaseTopics(String productIdentification, ProductTypeEnum productTypeEnum, Boolean reInit) {
        log.info("开始初始化产品基础Topic - 产品标识: {}, 产品类型: {}, 是否重新初始化: {}", productIdentification, productTypeEnum.getDesc(), reInit);
        List<ProductTopicTemplate> templates = getTopicTemplates(productTypeEnum);
        if (CollectionUtil.isEmpty(templates)) {
            log.warn("未找到产品类型[{}]对应的基础Topic模板", productTypeEnum.getDesc());
            return;
        }
        boolean alreadyInitialized = isAlreadyInitialized(productIdentification);
        if (alreadyInitialized) {
            if (Boolean.TRUE.equals(reInit)) {
                // 重新初始化：删除现有Topic后重新创建
                log.info("产品[{}]已存在基础Topic，执行重新初始化", productIdentification);
                // 删除该产品的所有基础Topic
                boolean deleteSuccess = deleteBaseTopicByProductIdentification(productIdentification);
                if (!deleteSuccess) {
                    throw BizException.wrap("删除现有基础Topic失败，无法重新初始化");
                }
            } else {
                // 不重新初始化，直接跳过
                log.info("产品[{}]的基础Topic已经初始化过，跳过初始化", productIdentification);
                return;
            }
        }
        log.info("产品[{}]没有基础Topic，执行首次初始化", productIdentification);
        List<ProductTopic> productTopics = buildProductTopics(templates, productIdentification);
        superManager.saveBatch(productTopics);
        log.info("成功为产品[{}]初始化了{}个基础Topic", productIdentification, productTopics.size());

    }

    /** 单次查询最大 ID 数,防止用户传超大列表撑爆 SQL IN。 */
    private static final int MAX_IDS_PER_QUERY = 500;

    /**
     * 批量根据 ID 查 topic 模板字符串。fail-soft:任何异常路径都返回空列表。
     *
     * @author mqttsnet
     * @since 2026-05-06
     */
    @Override
    public List<String> findTopicsByIds(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 过滤 null 元素 + 去重 + 截到上限,防御脏入参 / SQL IN 爆量
        List<Long> safeIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(MAX_IDS_PER_QUERY)
                .collect(Collectors.toList());
        if (safeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(superManager.listByIds(safeIds))
                .orElse(Collections.emptyList())
                .stream()
                .map(ProductTopic::getTopic)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }


    /**
     * 根据产品标识删除所有基础Topic
     *
     * @param productIdentification 产品标识
     * @return 删除基础Topic结果
     */
    private boolean deleteBaseTopicByProductIdentification(String productIdentification) {
        log.info("开始删除产品所有Topic - 产品标识: {}", productIdentification);
        // 查询该产品的所有基础Topic
        List<ProductTopic> existingTopics = superManager.list(Wraps.<ProductTopic>lbQ()
                .eq(ProductTopic::getProductIdentification, productIdentification)
                .eq(ProductTopic::getTopicType, ProductTopicTypeEnum.BASIC.getValue()));

        if (CollectionUtil.isEmpty(existingTopics)) {
            log.info("产品[{}]没有基础Topic记录，无需删除", productIdentification);
            return true;
        }

        // 批量删除
        List<Long> topicIds = existingTopics.stream()
                .map(ProductTopic::getId)
                .distinct()
                .collect(Collectors.toList());
        boolean deleteSuccess = superManager.removeByIds(topicIds);
        if (deleteSuccess) {
            log.info("成功删除产品[{}]的{}个基础Topic", productIdentification, existingTopics.size());
        } else {
            log.error("删除产品[{}]基础Topic失败", productIdentification);
            throw BizException.wrap("删除产品基础Topic失败");
        }
        return deleteSuccess;
    }


    /**
     * 获取Topic模板
     *
     * @param productTypeEnum 产品类型枚举
     * @return Topic模板列表
     */
    private List<ProductTopicTemplate> getTopicTemplates(ProductTypeEnum productTypeEnum) {
        Map<String, List<ProductTopicTemplate>> templates = topicTemplateConfig.getProductTopicTemplates();
        if (CollectionUtil.isEmpty(templates)) {
            throw BizException.wrap("Nacos配置中未找到Topic模板配置");
        }

        // 根据产品类型获取对应的模板
        String typeKey = getProductTypeKey(productTypeEnum);
        List<ProductTopicTemplate> templateList = templates.get(typeKey);
        if (CollectionUtil.isEmpty(templateList)) {
            throw BizException.wrap("不支持的产品类型: " + productTypeEnum.getDesc());
        }

        return templateList;
    }

    /**
     * 获取产品类型对应的配置键
     *
     * @param productTypeEnum 产品类型枚举
     * @return 配置键
     */
    private String getProductTypeKey(ProductTypeEnum productTypeEnum) {
        switch (productTypeEnum) {
            case COMMON:
                return "COMMON";
            case GATEWAY:
                return "GATEWAY";
            default:
                return "COMMON";
        }
    }

    /**
     * 检查是否已经初始化过
     * 仅检查基础Topic是否存在
     *
     * @param productIdentification 产品标识
     * @return {@link Boolean} 是否已初始化
     */
    private boolean isAlreadyInitialized(String productIdentification) {
        Long count = superManager.lambdaQuery()
                .eq(ProductTopic::getProductIdentification, productIdentification)
                .eq(ProductTopic::getTopicType, ProductTopicTypeEnum.BASIC.getValue())
                .count();
        return count > 0;
    }

    /**
     * 构建产品Topic列表
     *
     * @param templates             Topic模板列表
     * @param productIdentification 产品标识
     * @return {@link List<ProductTopic>} ProductTopic列表
     */
    private List<ProductTopic> buildProductTopics(List<ProductTopicTemplate> templates, String productIdentification) {
        return templates.stream()
                .map(template -> buildProductTopic(template, productIdentification))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个ProductTopic实体
     *
     * @param template              Topic模板
     * @param productIdentification 产品标识
     * @return {@link ProductTopic} ProductTopic实体
     */
    private ProductTopic buildProductTopic(ProductTopicTemplate template,
                                           String productIdentification) {
        ProductTopic productTopic = new ProductTopic();
        productTopic.setProductIdentification(productIdentification);
        productTopic.setTopic(template.getTopic());
        productTopic.setPublisher(template.getPublisher());
        productTopic.setSubscriber(template.getSubscriber());
        productTopic.setFunctionType(template.getFunctionType());
        productTopic.setRemark(template.getRemark());
        productTopic.setTopicType(template.getTopicType());
        productTopic.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return productTopic;
    }

}


