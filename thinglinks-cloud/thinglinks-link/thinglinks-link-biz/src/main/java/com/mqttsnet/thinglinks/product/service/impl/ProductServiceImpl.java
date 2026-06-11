package com.mqttsnet.thinglinks.product.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.result.ProductOverviewResultVO;
import com.mqttsnet.thinglinks.product.entity.Product;
import com.mqttsnet.thinglinks.product.enumeration.ProductStatusEnum;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.product.event.publisher.ProductEventPublisher;
import com.mqttsnet.thinglinks.product.event.source.ProductCacheEvictSource;
import com.mqttsnet.thinglinks.product.event.source.ProductModelChangedSource;
import com.mqttsnet.thinglinks.product.manager.ProductManager;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.product.vo.save.ProductSaveVO;
import com.mqttsnet.thinglinks.product.vo.update.ProductUpdateVO;
import com.mqttsnet.thinglinks.productcommand.entity.ProductCommand;
import com.mqttsnet.thinglinks.productcommand.service.ProductCommandService;
import com.mqttsnet.thinglinks.productcommand.vo.param.ProductCommandParamVO;
import com.mqttsnet.thinglinks.productcommand.vo.save.ProductCommandSaveVO;
import com.mqttsnet.thinglinks.productcommandrequest.entity.ProductCommandRequest;
import com.mqttsnet.thinglinks.productcommandrequest.service.ProductCommandRequestService;
import com.mqttsnet.thinglinks.productcommandrequest.vo.param.ProductCommandRequestParamVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.save.ProductCommandRequestSaveVO;
import com.mqttsnet.thinglinks.productcommandresponse.entity.ProductCommandResponse;
import com.mqttsnet.thinglinks.productcommandresponse.service.ProductCommandResponseService;
import com.mqttsnet.thinglinks.productcommandresponse.vo.param.ProductCommandResponseParamVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.save.ProductCommandResponseSaveVO;
import com.mqttsnet.thinglinks.productproperty.entity.ProductProperty;
import com.mqttsnet.thinglinks.productproperty.service.ProductPropertyService;
import com.mqttsnet.thinglinks.productproperty.vo.param.ProductPropertyParamVO;
import com.mqttsnet.thinglinks.productproperty.vo.save.ProductPropertySaveVO;
import com.mqttsnet.thinglinks.productservice.entity.ProductServices;
import com.mqttsnet.thinglinks.productservice.enumeration.ProductServiceStatusEnum;
import com.mqttsnet.thinglinks.productservice.service.ProductServiceService;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import com.mqttsnet.thinglinks.productservice.vo.save.ProductServiceSaveVO;
import com.mqttsnet.thinglinks.producttopic.service.ProductTopicService;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductChangeTargetTypeEnum;
import com.mqttsnet.thinglinks.productversionchangelog.enumeration.ProductVersionChangeTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 业务实现类
 * 产品模型
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
public class ProductServiceImpl extends SuperServiceImpl<ProductManager, Long, Product> implements ProductService {

    private final ProductServiceService productServiceService;

    private final ProductPropertyService productPropertyService;

    private final ProductCommandService productCommandService;

    private final ProductCommandRequestService productCommandRequestService;

    private final ProductCommandResponseService productCommandResponseService;

    private final DeviceService deviceService;

    private final ProductTopicService productTopicService;

    private final LinkCacheDataHelper linkCacheDataHelper;

    private final ProductEventPublisher productEventPublisher;

    /**
     * 产品 CRUD 时同步刷新草稿快照 + 产品删除时级联软删 product_version 行。
     * ProductVersionServiceImpl 依赖 ProductQueryService(不是 ProductService),无循环依赖。
     */
    private final ProductVersionService productVersionService;

    @Override
    public IPage<ProductResultVO> getPage(PageParams<ProductPageQuery> params) {
        IPage<Product> page = superManager.getPage(params);
        return BeanPlusUtil.toBeanPage(page, ProductResultVO.class);
    }

    /**
     * 获取产品模型总量
     *
     * @return {@link Long} 产品模型数据总量
     */
    @Override
    public Long findProductTotal() {
        return superManager.findProductTotal();
    }

    /**
     * 保存产品模型
     *
     * @param saveVO
     * @return
     */
    @Override
    public ProductSaveVO saveProduct(ProductSaveVO saveVO) {
        log.info("saveProduct saveVO:{}", saveVO);
        //校验参数
        checkedProductSaveVO(saveVO);
        //构建参数
        Product product = builderProductSaveVO(saveVO);
        //保存产品
        superManager.save(product);
        // 初始化产品Topic
        initProductBaseTopics(product.getProductIdentification(), Boolean.FALSE);

        // 发布产品物模型变更事件
        productEventPublisher.publishProductModelChangedEvent(ProductModelChangedSource.builder()
                .productIdentification(product.getProductIdentification())
                .changeType(ProductVersionChangeTypeEnum.CREATE)
                .targetType(ProductChangeTargetTypeEnum.PRODUCT_INFO)
                .after(BeanPlusUtil.toBeanIgnoreError(product, ProductResultVO.class))
                .changeSummary("新增产品「" + product.getProductName() + "」")
                .build());

        return saveVO;
    }


    /**
     * 修改产品模型
     *
     * @param updateVO
     * @return
     */
    @Override
    public ProductUpdateVO updateProduct(ProductUpdateVO updateVO) {
        log.info("updateProduct updateVO:{}", updateVO);
        //校验参数
        checkedProductUpdateVO(updateVO);
        Product before = superManager.getById(updateVO.getId());
        //构建参数
        Product product = BeanPlusUtil.toBeanIgnoreError(updateVO, Product.class);
        //更新
        superManager.updateById(BeanPlusUtil.toBeanIgnoreError(updateVO, Product.class));
        Product after = superManager.getById(updateVO.getId());
        // 初始化产品Topic
        initProductBaseTopics(product.getProductIdentification(), Boolean.TRUE);

        // 发布产品物模型变更事件
        productEventPublisher.publishProductModelChangedEvent(ProductModelChangedSource.builder()
                .productIdentification(product.getProductIdentification())
                .changeType(ProductVersionChangeTypeEnum.UPDATE)
                .targetType(ProductChangeTargetTypeEnum.PRODUCT_INFO)
                .before(BeanPlusUtil.toBeanIgnoreError(before, ProductResultVO.class))
                .after(BeanPlusUtil.toBeanIgnoreError(after, ProductResultVO.class))
                .changeSummary("编辑产品「" + (after != null ? after.getProductName() : updateVO.getProductName()) + "」")
                .build());

        return updateVO;
    }

    /**
     * 删除产品模型
     *
     * @param id 产品ID
     * @return {@link Boolean} 是否删除成功
     */
    @Override
    public Boolean deleteProduct(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        Product product = superManager.getById(id);
        if (null == product) {
            throw BizException.wrap("The product does not exist");
        }
        if (deviceService.isProductInUseByDevices(product.getProductIdentification())) {
            throw BizException.wrap("The product is bound to the device and cannot be deleted");
        }
        String productIdentification = product.getProductIdentification();
        // 级联软删 product_version 所有版本行(DRAFT + 历史 PUBLISHED/CANARY/SHADOW),
        // TD 历史资源由 purgeHistory 独立流程处理,本方法仅清基础表关系。
        int affectedVersions = productVersionService.softDeleteAllByProductIdentification(productIdentification);
        if (affectedVersions > 0) {
            log.info("[deleteProduct] cascade softDelete product_version productIdentification={} affected={}", productIdentification, affectedVersions);
        }
        Boolean removed = superManager.removeById(id);
        // 发缓存失效事件:产品已删,监听器 AFTER_COMMIT 失效产品基础缓存。
        // 物模型缓存按 (productIdentification, versionNo) 切分,版本快照不可变 + 7d TTL 自动过期,
        // 产品被删后老缓存不会再被命中(因为没人能再查到该 product),不需要主动清。
        productEventPublisher.publishProductCacheEvictEvent(
                ProductCacheEvictSource.builder().productIdentification(productIdentification)
                        .contextMap(ContextUtil.getLocalMap()).build());
        return removed;
    }

    /**
     * 查询产品管理完整信息（包含服务、属性、命令）
     *
     * @param productIdentification 产品标识
     * @return {@link ProductParamVO} 产品管理完整参数VO
     * @throws com.mqttsnet.basic.exception.BizException 如果产品不存在
     */
    @Override
    public ProductParamVO selectFullProductByProductIdentification(String productIdentification) {
        // 查询产品，如果不存在则抛出异常
        Product product = Optional.ofNullable(superManager.findOneByProductIdentification(productIdentification))
            .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));

        // 转换基本产品信息
        ProductParamVO productDetails = BeanPlusUtil.toBeanIgnoreError(product, ProductParamVO.class);

        // 查询产品服务列表（只查询已激活的服务）
        List<ProductServices> productServicesList = Optional.of(new ProductServices())
            .map(find -> {
                find.setProductId(product.getId());
                find.setServiceStatus(ProductServiceStatusEnum.ACTIVATED.getValue());
                return productServiceService.selectProductServicesList(find);
            })
            .orElse(Collections.emptyList());

        List<Long> serviceIds = productServicesList.stream().map(ProductServices::getId).collect(Collectors.toList());

        // 查询所有服务的命令和属性
        List<ProductCommand> productCommandList = Optional.ofNullable(productCommandService.findAllByServiceIds(serviceIds))
            .orElse(Collections.emptyList());

        List<ProductProperty> productPropertiesList = Optional.ofNullable(productPropertyService.findAllByServiceIds(serviceIds))
            .orElse(Collections.emptyList());

        // 组装服务信息（包含命令和属性）
        List<ProductServiceParamVO> services = productServicesList.stream().map(ps -> {
            ProductServiceParamVO service = BeanPlusUtil.toBeanIgnoreError(ps, ProductServiceParamVO.class);

            // 组装服务的命令列表
            List<ProductCommandParamVO> commands = productCommandList.stream()
                .filter(command -> Objects.equals(command.getServiceId(), ps.getId()))  // Filter by Service ID
                .map(command -> {
                    ProductCommandParamVO commandParamVO = BeanPlusUtil.toBeanIgnoreError(command,
                        ProductCommandParamVO.class);

                    // 组装命令的请求参数
                    List<ProductCommandRequestParamVO> filteredRequests =
                        productCommandRequestService.selectCommandRequests(Collections.singletonList(command.getId()))
                            .stream()
                            .map(request -> BeanPlusUtil.toBeanIgnoreError(request, ProductCommandRequestParamVO.class))
                            .filter(request -> Objects.equals(request.getCommandId(), command.getId()))
                            .collect(Collectors.toList());
                    commandParamVO.setRequests(filteredRequests);

                    // 组装命令的响应参数
                    List<ProductCommandResponseParamVO> filteredResponses =
                        productCommandResponseService.selectCommandResponses(Collections.singletonList(command.getId()))
                            .stream()
                            .map(response -> BeanPlusUtil.toBeanIgnoreError(response, ProductCommandResponseParamVO.class))
                            .filter(response -> Objects.equals(response.getCommandId(), command.getId()))
                            .collect(Collectors.toList());
                    commandParamVO.setResponses(filteredResponses);

                    return commandParamVO;
                })
                .collect(Collectors.toList());
            service.setCommands(commands);

            // 组装服务的属性列表
            List<ProductPropertyParamVO> properties = productPropertiesList.stream()
                .filter(property -> Objects.equals(property.getServiceId(), ps.getId()))  // Filter by Service ID
                .map(pp -> BeanPlusUtil.toBeanIgnoreError(pp, ProductPropertyParamVO.class))
                .collect(Collectors.toList());
            service.setProperties(properties);

            return service;
        }).collect(Collectors.toList());

        productDetails.setServices(services);
        return productDetails;
    }


    @Override
    public void importProductJson(MultipartFile file, String appId) {
        String originalFilename = file.getOriginalFilename();
        if (!"json".equalsIgnoreCase(FileUtil.getSuffix(originalFilename))) {
            throw BizException.wrap("the file suffix must be json");
        }
        try {
            String jsonContent = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);
            // 解析产品模型数据
            ProductParamVO productParamVO = JSON.parseObject(jsonContent, ProductParamVO.class);
            if (StrUtil.isNotBlank(appId)) {
                productParamVO.setAppId(appId);
            }
            this.productJsonDataAnalysis(productParamVO);
        } catch (IOException e) {
            log.error("import product json error: {}", e.getMessage(), e);
            throw BizException.wrap("import product json failed!");
        }
    }

    @Override
    public ProductResultVO findOneByProductId(Long productId) {
        return BeanPlusUtil.toBeanIgnoreError(superManager.getById(productId), ProductResultVO.class);
    }

    /**
     * 根据产品标识查询产品详情
     *
     * @param productIdentification 产品标识
     * @return {@link ProductResultVO} 产品详情
     */
    @Override
    public ProductResultVO findOneByProductIdentification(String productIdentification) {
        return BeanPlusUtil.toBeanIgnoreError(superManager.findOneByProductIdentification(productIdentification), ProductResultVO.class);
    }

    @Override
    public List<ProductResultVO> findListByProductIdentificationList(List<String> productIdentificationList) {
        if (CollUtil.isEmpty(productIdentificationList)) {
            return Collections.emptyList();
        }
        return BeanPlusUtil.copyToList(superManager.findListByProductIdentificationList(productIdentificationList), ProductResultVO.class);
    }

    @Override
    public void generateProductJson(ProductParamVO paramVO) {
        this.productJsonDataAnalysis(paramVO);
    }

    /**
     * 获取产品概况统计
     *
     * @return {@link ProductOverviewResultVO} 产品概况统计
     */
    @Override
    public ProductOverviewResultVO getProductOverview() {
        List<Product> productList = superManager.list();
        ProductOverviewResultVO resultVO = new ProductOverviewResultVO();

        resultVO.setProductsTotalCount(productList.size());

        AtomicLong ordinaryCount = new AtomicLong();
        AtomicLong gatewayCount = new AtomicLong();
        AtomicLong unknownCount = new AtomicLong();
        AtomicLong enabledCount = new AtomicLong();
        AtomicLong disabledCount = new AtomicLong();

        productList.forEach(product -> {
            // 产品类型统计
            if (Objects.equals(product.getProductType(), ProductTypeEnum.COMMON.getValue())) {
                ordinaryCount.incrementAndGet();
            } else if (Objects.equals(product.getProductType(), ProductTypeEnum.GATEWAY.getValue())) {
                gatewayCount.incrementAndGet();
            } else if (Objects.equals(product.getProductType(), ProductTypeEnum.UNKNOWN.getValue())) {
                unknownCount.incrementAndGet();
            }

            // 产品状态统计
            if (Objects.equals(product.getProductStatus(), ProductStatusEnum.ACTIVATED.getValue())) {
                enabledCount.incrementAndGet();
            } else if (Objects.equals(product.getProductStatus(), ProductStatusEnum.LOCKED.getValue())) {
                disabledCount.incrementAndGet();
            }
        });

        resultVO.setOrdinaryProductsCount(ordinaryCount.intValue());
        resultVO.setGatewayProductsCount(gatewayCount.intValue());
        resultVO.setUnknownProductsCount(unknownCount.intValue());
        resultVO.setEnabledCount(enabledCount.intValue());
        resultVO.setDisabledCount(disabledCount.intValue());

        return resultVO;
    }

    @Override
    public Boolean initProductBaseTopics(String productIdentification, Boolean reInit) {
        log.info("开始初始化产品基础Topic - 产品标识: {}, 是否重新初始化: {}", productIdentification, reInit);
        ProductResultVO productResultVO = findOneByProductIdentification(productIdentification);
        ArgumentAssert.notNull(productResultVO, "产品信息不存在");
        try {
            productTopicService.initProductBaseTopics(productIdentification, ProductTypeEnum.valueOf(productResultVO.getProductType()), reInit);
            log.info("成功初始化产品基础Topic - 产品标识: {}", productIdentification);
            return true;
        } catch (Exception e) {
            log.error("初始化产品基础Topic失败 - 产品标识: {}", productIdentification, e);
            throw BizException.wrap("初始化产品基础Topic失败请重试");
        }
    }


    @Override
    public List<ProductResultVO> getProductResultVOList(ProductPageQuery query) {
        return BeanPlusUtil.toBeanList(superManager.getProductList(query), ProductResultVO.class);
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedProductSaveVO(ProductSaveVO saveVO) {
        ArgumentAssert.notNull(saveVO.getProductType(), "productType Cannot be null");
        if (!ProductTypeEnum.TYPE_COLLECTION.contains(saveVO.getProductType())) {
            throw BizException.wrap("productType is not exist");
        }
        ArgumentAssert.notBlank(saveVO.getAppId(), "appId Cannot be null");
        ArgumentAssert.notBlank(saveVO.getProductName(), "productName Cannot be null");
        ArgumentAssert.notBlank(saveVO.getManufacturerId(), "manufacturerId Cannot be null");
        ArgumentAssert.notBlank(saveVO.getManufacturerName(), "manufacturerName Cannot be null");
        ArgumentAssert.notBlank(saveVO.getModel(), "model Cannot be null");
        ArgumentAssert.notBlank(saveVO.getDataFormat(), "dataFormat Cannot be null");
        ArgumentAssert.notBlank(saveVO.getProtocolType(), "protocolType Cannot be null");
        Optional<ProtocolTypeEnum> protocolTypeOptional = ProtocolTypeEnum.fromValue(saveVO.getProtocolType());

        if (protocolTypeOptional.isEmpty()) {
            throw BizException.wrap("protocolType is not exist");
        }
        ArgumentAssert.notBlank(saveVO.getDeviceType(), "deviceType Cannot be null");
        //验证产品模型是否存在
        Product product = superManager.findOneByManufacturerIdAndModelAndDeviceType(saveVO.getManufacturerId(), saveVO.getModel(), saveVO.getDeviceType());
        if (ObjectUtil.isNotNull(product)) {
            throw BizException.wrap("product model already exists");
        }
        //产品模型状态
        ArgumentAssert.notNull(saveVO.getProductStatus(), "productStatus Cannot be null");
        ProductStatusEnum.fromValue(saveVO.getProductStatus()).orElseThrow(() -> BizException.wrap("productStatus is not exist"));

        // 注:产品版本号(product_version)不再由创建/编辑表单维护。
        // 版本生命周期由 ProductVersionService 的草稿/发布流程接管 ──
        // 创建时 activeVersionNo 为空,发布时雪花生成版本号并回写。
        // 产品唯一性已由上方 manufacturerId+model+deviceType 校验覆盖。
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private Product builderProductSaveVO(ProductSaveVO saveVO) {
        //产品标识生成规则: 雪花算法生成
        saveVO.setProductIdentification(String.valueOf(SnowflakeIdUtil.nextId()));
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, Product.class);
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedProductUpdateVO(ProductUpdateVO updateVO) {

        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        ArgumentAssert.notNull(updateVO.getProductType(), "productType Cannot be null");
        if (!ProductTypeEnum.TYPE_COLLECTION.contains(updateVO.getProductType())) {
            throw BizException.wrap("productType is not exist");
        }
        ArgumentAssert.notBlank(updateVO.getAppId(), "appId Cannot be null");
        ArgumentAssert.notBlank(updateVO.getProductName(), "productName Cannot be null");
        ArgumentAssert.notBlank(updateVO.getManufacturerId(), "manufacturerId Cannot be null");
        ArgumentAssert.notBlank(updateVO.getManufacturerName(), "manufacturerName Cannot be null");
        ArgumentAssert.notBlank(updateVO.getModel(), "model Cannot be null");
        ArgumentAssert.notBlank(updateVO.getDataFormat(), "dataFormat Cannot be null");
        ArgumentAssert.notBlank(updateVO.getProtocolType(), "protocolType Cannot be null");
        Optional<ProtocolTypeEnum> protocolTypeOptional = ProtocolTypeEnum.fromValue(updateVO.getProtocolType());

        if (protocolTypeOptional.isEmpty()) {
            throw BizException.wrap("protocolType is not exist");
        }
        ArgumentAssert.notBlank(updateVO.getDeviceType(), "deviceType Cannot be null");
        //产品模型状态
        ArgumentAssert.notNull(updateVO.getProductStatus(), "productStatus Cannot be null");
        ProductStatusEnum.fromValue(updateVO.getProductStatus()).orElseThrow(() -> BizException.wrap("productStatus is not exist"));

        // 注:产品版本号(product_version)不再由创建/编辑表单维护,
        // 版本生命周期由 ProductVersionService 的草稿/发布流程接管。
    }

    /**
     * 解析产品模型数据
     *
     * @param productVO 产品模型参数
     */
    private void productJsonDataAnalysis(ProductParamVO productVO) {
        log.info("productJsonDataAnalysis...productVO:{}", JSON.toJSONString(productVO));
        //服务属性解析处理
        Product product = BeanPlusUtil.toBeanIgnoreError(productVO, Product.class);
        //产品标识生成规则: 雪花算法生成
        product.setProductIdentification(String.valueOf(SnowflakeIdUtil.nextId()));
        product.setProductStatus(ProductStatusEnum.LOCKED.getValue());
        product.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        //新增 校验参数
        checkedProductSaveVO(BeanPlusUtil.toBeanIgnoreError(product, ProductSaveVO.class));
        boolean saveProductFlag = superManager.save(product);
        if (!saveProductFlag) {
            throw BizException.wrap("Product information storage fails");
        }
        //添加服务数据
        List<ProductServiceParamVO> services = productVO.getServices();
        if (CollUtil.isEmpty(services)) {
            throw BizException.wrap("The product service information is empty. Please check the product model JSON file.");
        }
        ProductServiceParamVO productServiceParamVO;
        for (ProductServiceParamVO service : services) {
            productServiceParamVO = service;
            ProductServiceSaveVO productServices = BeanPlusUtil.toBeanIgnoreError(productServiceParamVO, ProductServiceSaveVO.class);
            productServices.setProductId(product.getId());
            productServices.setServiceStatus(product.getProductStatus());
            productServices.setCreatedOrgId(ContextUtil.getCurrentDeptId());
            ProductServices productService = productServiceService.saveProductService(productServices);
            if (ObjectUtil.isNull(productService)) {
                throw BizException.wrap("Service capability Data storage fails");
            }
            //添加属性数据
            List<ProductPropertyParamVO> properties = productServiceParamVO.getProperties();
            if (!properties.isEmpty()) {
                ProductPropertySaveVO propertySaveVO;
                for (ProductPropertyParamVO property : properties) {
                    propertySaveVO = BeanPlusUtil.toBeanIgnoreError(property, ProductPropertySaveVO.class);
                    propertySaveVO.setServiceId(productService.getId());
                    propertySaveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
                    ProductProperty productProperty = productPropertyService.saveProductProperty(propertySaveVO);
                    if (ObjectUtil.isNull(productProperty)) {
                        throw BizException.wrap("Property capability Data storage fails");
                    }
                }
            }
            //添加命令数据
            List<ProductCommandParamVO> commands = productServiceParamVO.getCommands();
            if (!commands.isEmpty()) {
                ProductCommandParamVO productCommandParamVO;
                for (ProductCommandParamVO command : commands) {
                    productCommandParamVO = command;
                    ProductCommandSaveVO productCommand = BeanPlusUtil.toBeanIgnoreError(productCommandParamVO, ProductCommandSaveVO.class);
                    productCommand.setServiceId(productService.getId());
                    productCommand.setCreatedOrgId(ContextUtil.getCurrentDeptId());
                    ProductCommand saveProductCommand = productCommandService.saveProductCommand(productCommand);
                    if (ObjectUtil.isNull(saveProductCommand)) {
                        throw BizException.wrap("command capability Data storage fails");
                    }
                    //产品请求服务命令
                    List<ProductCommandRequestParamVO> requests = productCommandParamVO.getRequests();
                    if (!requests.isEmpty()) {
                        for (ProductCommandRequestParamVO request : requests) {
                            ProductCommandRequestSaveVO productCommandRequest = BeanPlusUtil.toBeanIgnoreError(request, ProductCommandRequestSaveVO.class);
                            productCommandRequest.setServiceId(productService.getId());
                            productCommandRequest.setCommandId(saveProductCommand.getId());
                            productCommandRequest.setCreatedOrgId(ContextUtil.getCurrentDeptId());
                            ProductCommandRequest saveCommandRequestFlag = productCommandRequestService.saveProductCommandRequest(productCommandRequest);
                            if (ObjectUtil.isNull(saveCommandRequestFlag)) {
                                throw BizException.wrap("productCommandRequest capability Data storage fails");
                            }
                        }
                    }
                    //产品响应服务命令
                    List<ProductCommandResponseParamVO> responses = productCommandParamVO.getResponses();
                    if (!responses.isEmpty()) {
                        for (ProductCommandResponseParamVO respons : responses) {
                            ProductCommandResponseSaveVO commandResponse = BeanPlusUtil.toBeanIgnoreError(respons, ProductCommandResponseSaveVO.class);
                            commandResponse.setServiceId(productService.getId());
                            commandResponse.setCommandId(saveProductCommand.getId());
                            commandResponse.setCreatedOrgId(ContextUtil.getCurrentDeptId());
                            ProductCommandResponse saveCommandResponseFlag = productCommandResponseService.saveProductCommandResponse(commandResponse);
                            if (ObjectUtil.isNull(saveCommandResponseFlag)) {
                                throw BizException.wrap("productCommandResponse capability Data storage fails");
                            }
                        }
                    }
                }
            }
        }

        // 初始化产品Topic
        initProductBaseTopics(product.getProductIdentification(), Boolean.TRUE);

        // 发布产品物模型变更事件
        productEventPublisher.publishProductModelChangedEvent(ProductModelChangedSource.builder()
                .productIdentification(product.getProductIdentification())
                .changeType(ProductVersionChangeTypeEnum.CREATE)
                .targetType(ProductChangeTargetTypeEnum.PRODUCT_INFO)
                .after(BeanPlusUtil.toBeanIgnoreError(product, ProductResultVO.class))
                .changeSummary("新增产品「" + product.getProductName() + "」")
                .build());
    }

    // ────────────── 产品版本指针切换 service 入口 ──────────────
    //
    // 为什么独立两个方法而不是合成一个带 boolean / enum 参数:
    //   * 灰度发布需要"捕获 切换前 activeVersionNo 写入 previousFullVersionNo",外部传不了"之前的值"
    //     的语义,Service 内部读出来最安全
    //   * 回滚的"清空 previousFullVersionNo"动作只属于回滚链路,跟发布完全不同步
    //
    // 跨域调用方(productversion 域)调本 Service 而非 ProductManager:
    //   * 走 @DS(BASE_TENANT) 切租户库;Manager 无 @DS 会 fallback 默认库
    //   * 禁止跨层级调用 ── 类约束已在团队规约里反复明确

    @Override
    public Product switchActiveVersionForPublish(String productIdentification, String newActiveVersion,
                                                 boolean recordCurrentAsPrevious) {
        ArgumentAssert.notBlank(productIdentification, "productIdentification must not be blank");
        ArgumentAssert.notBlank(newActiveVersion, "newActiveVersion must not be blank");

        Product product = Optional.ofNullable(superManager.findOneByProductIdentification(productIdentification))
                .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));
        String previousActive = product.getActiveVersionNo();
        product.setActiveVersionNo(newActiveVersion);
        if (recordCurrentAsPrevious) {
            // 灰度发布:把切换前的版本号记入备忘指针,供后续回滚 / 灰度路由
            product.setPreviousFullVersionNo(previousActive);
        }
        superManager.updateById(product);
        // 发缓存失效事件:activeVersionNo 已变,监听器 AFTER_COMMIT 失效产品基础缓存
        productEventPublisher.publishProductCacheEvictEvent(
                ProductCacheEvictSource.builder().productIdentification(productIdentification)
                        .contextMap(ContextUtil.getLocalMap()).build());
        return product;
    }

    @Override
    public Product rollbackActiveVersion(String productIdentification, String targetVersion) {
        ArgumentAssert.notBlank(productIdentification, "productIdentification must not be blank");
        ArgumentAssert.notBlank(targetVersion, "targetVersion must not be blank");

        Product product = Optional.ofNullable(superManager.findOneByProductIdentification(productIdentification))
                .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));
        product.setActiveVersionNo(targetVersion);
        // 回滚后产品不再处于灰度切换中,清空 previousFullVersionNo 避免 statistics 误统计为"灰度中"
        product.setPreviousFullVersionNo(null);
        superManager.updateById(product);
        // 发缓存失效事件:activeVersionNo 已变,监听器 AFTER_COMMIT 失效产品基础缓存
        productEventPublisher.publishProductCacheEvictEvent(
                ProductCacheEvictSource.builder().productIdentification(productIdentification)
                        .contextMap(ContextUtil.getLocalMap()).build());
        return product;
    }
}
