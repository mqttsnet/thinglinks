package com.mqttsnet.thinglinks.link.service.product.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DataTypeEnum;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.text.CharsetKit;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanUtils;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.empowerment.enumeration.EmpowermentStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.empowerment.vo.result.EmpowermentRecordResultVO;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommands;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.link.api.domain.product.model.ProductModel;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Properties;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Services;
import com.mqttsnet.thinglinks.link.api.domain.product.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import com.mqttsnet.thinglinks.link.mapper.product.ProductMapper;
import com.mqttsnet.thinglinks.link.service.product.*;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.constant.TdsConstants;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.FieldsVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.utils.TdsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 产品模型业务层
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/25$ 23:52$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/25$ 23:52$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@RefreshScope
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProductServicesService productServicesService;
    @Autowired
    private ProductPropertiesService productPropertiesService;

    @Autowired
    private ProductCommandsService productCommandsService;

    @Autowired
    private ProductCommandsRequestsService productCommandsRequestsService;

    @Autowired
    private ProductCommandsResponseService productCommandsResponseService;

    @Resource
    private RemoteTdEngineService remoteTdEngineService;
    @Autowired
    private RedisService redisService;

    @Autowired
    private CacheDataHelper cacheDataHelper;

    /**
     * 数据库名称
     */
    @Value("${spring.datasource.dynamic.datasource.master.dbName:thinglinks}")
    private String dataBaseName;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        return productMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setUpdateBy(sysUser.getUserName());
        return productMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setUpdateBy(sysUser.getUserName());
        return productMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        return productMapper.insertSelective(record);
    }

    @Override
    public Product selectByPrimaryKey(Long id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateBy(sysUser.getUserName());
        return productMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateBy(sysUser.getUserName());
        return productMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Product> list) {
        return productMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Product> list) {
        return productMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Product> list) {
        return productMapper.batchInsert(list);
    }

    /**
     * 产品模型文件导入
     *
     * @param file                   json文件
     * @param updateSupport          是否更新已经存在的产品模型数据
     * @param appId                  应用ID
     * @param templateIdentification 产品模型模板标识
     * @param status                 状态(字典值：启用  停用)
     * @return AjaxResult
     * @throws Exception
     */
    @Override
    public AjaxResult importProductJson(MultipartFile file, Boolean updateSupport, String appId, String templateIdentification, String status) throws Exception {
        // 首先校验json格式
        List<String> jsonType = Lists.newArrayList("json");
        // 获取文件名，带后缀
        String originalFilename = file.getOriginalFilename();
        // 获取文件的后缀格式
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        StringBuilder sb = null;
        if (jsonType.contains(fileSuffix)) {
            //JSON格式
            try (InputStream inputStream = file.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CharsetKit.getFilecharset(file.getInputStream())));
                sb = new StringBuilder();
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    //解析产品模型数据
                    return this.productJsonDataAnalysis(JSONObject.parseObject(sb.toString()), appId, templateIdentification, status);
                } catch (IOException e) {
                    log.error(e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        //非法文件
        return AjaxResult.error("the picture's suffix is illegal");
    }

    /**
     * 解析产品模型数据
     *
     * @param content                产品模型数据
     * @param appId                  应用ID
     * @param templateIdentification 产品模型模标识
     * @param status                 状态(字典值：启用  停用)
     * @return 解析结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public AjaxResult productJsonDataAnalysis(JSONObject content, String appId, String templateIdentification, String status) throws Exception {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        try {
            //厂商ID
            String manufacturerId = JsonPath.read(content, "$.manufacturerId");
            //厂商名称
            String manufacturerName = JsonPath.read(content, "$.manufacturerName");
            //产品型号
            String model = JsonPath.read(content, "$.model");
            //产品名称
            String productName = JsonPath.read(content, "$.productName");
            //产品类型
            String productType = JsonPath.read(content, "$.productType");
            //数据格式
            String dataFormat = JsonPath.read(content, "$.dataFormat");
            //设备类型
            String deviceType = JsonPath.read(content, "$.deviceType");
            //设备接入平台的协议类型
            String protocolType = JsonPath.read(content, "$.protocolType");
            //产品描述
            String remark = JsonPath.read(content, "$.remark");
            //验证产品模型是否存在
            Product oneByManufacturerIdAndModelAndDeviceType = productMapper.findOneByManufacturerIdAndModelAndDeviceType(manufacturerId, model, deviceType);
            if (StringUtils.isNotNull(oneByManufacturerIdAndModelAndDeviceType)) {
                return AjaxResult.error("产品模型已存在,无需上传");
            }
            //验证properties数据格式
            List readDatatype = JsonPath.read(content.toJSONString(), "$..properties[*].datatype");
            List readDescription = JsonPath.read(content.toJSONString(), "$..properties[*].description");
            List readEnumlist = JsonPath.read(content.toJSONString(), "$..properties[*].enumlist");
            List readMax = JsonPath.read(content.toJSONString(), "$..properties[*].max");
            List readMaxlength = JsonPath.read(content.toJSONString(), "$..properties[*].maxlength");
            List readMethod = JsonPath.read(content.toJSONString(), "$..properties[*].method");
            List readMin = JsonPath.read(content.toJSONString(), "$..properties[*].min");
            List readName = JsonPath.read(content.toJSONString(), "$..properties[*].name");
            List readRequired = JsonPath.read(content.toJSONString(), "$..properties[*].required");
            List readStep = JsonPath.read(content.toJSONString(), "$..properties[*].step");
            List readUnit = JsonPath.read(content.toJSONString(), "$..properties[*].unit");
            Map<String, Object> parsingErrorMessages = new HashMap<>();
            List<Object> list = new ArrayList<>();
            //验证datatype数据格式（int、decimal、string、binary、bool、timestamp、json）
            readDatatype.forEach(item -> {
                boolean flag = "int".equals(item) || "decimal".equals(item) || "string".equals(item) || "binary".equals(item)
                        || "bool".equals(item) || "timestamp".equals(item) || "json".equals(item);
                list.add(flag);
                if (list.contains(false)) {
                    log.error("datatype:" + item, "Invalid product: Invalid dataType,must be one of [int、decimal、string、binary、bool、timestamp、json]");
                    parsingErrorMessages.put("datatype:" + item, "Invalid product: Invalid dataType,must be one of [int、decimal、string、binary、bool、timestamp、json]");
                }
            });
            if (!parsingErrorMessages.isEmpty()) {
                return AjaxResult.error(parsingErrorMessages.toString());
            }
            //服务属性解析处理
            Product product = new Product();
            product.setAppId(appId);
            if (StringUtils.isNotEmpty(templateIdentification)) {
                product.setTemplateIdentification(templateIdentification);
            }
            product.setProductName(productName);
            product.setProductIdentification(UUID.getUUID());
            product.setProductType(String.valueOf(productType));
            product.setManufacturerId(manufacturerId);
            product.setManufacturerName(manufacturerName);
            product.setModel(model);
            product.setDataFormat(dataFormat);
            product.setDeviceType(deviceType);
            product.setProtocolType(protocolType);
            product.setStatus(status);
            product.setRemark(remark);
            product.setCreateBy(sysUser.getUserName());
            final int insertProduct = productMapper.insertProduct(product);
            if (insertProduct == 0) {
                return AjaxResult.error("Product model storage error");
            }
            //添加服务数据
            JSONArray services = content.getJSONArray("services");
            for (int i = 0; i < services.size(); i++) {
                JSONObject service = services.getJSONObject(i);
                ProductServices productServices = new ProductServices();
                productServices.setServiceName(service.getString("serviceId"));
                productServices.setProductIdentification(product.getProductIdentification());
                productServices.setStatus(product.getStatus());
                productServices.setDescription(service.getString("description"));
                productServices.setCreateBy(sysUser.getUserName());
                final int insertSelective = productServicesService.insertSelective(productServices);
                if (insertSelective == 0) {
                    throw new RuntimeException("Service capability Data storage fails");
                }
                //添加属性数据
                JSONArray properties = service.getJSONArray("properties");
                for (int j = 0; j < properties.size(); j++) {
                    JSONObject propertie = properties.getJSONObject(j);
                    ProductProperties productProperties = new ProductProperties();
                    BeanUtils.copyProperties(propertie.toJavaObject(Properties.class), productProperties);
                    productProperties.setServiceId(productServices.getId());
                    productProperties.setCreateBy(sysUser.getUserName());
                    final int batchInsert = productPropertiesService.insertSelective(productProperties);
                    if (batchInsert == 0) {
                        log.error("Property capability Data storage fails");
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return AjaxResult.error("操作失败");
        }
        return AjaxResult.success("操作成功");
    }

    /**
     * 查询产品管理
     *
     * @param id 产品管理主键
     * @return 产品管理
     */
    @Override
    public Product selectProductById(Long id) {
        return productMapper.selectProductById(id);
    }

    @Override
    public Product selectByProductIdentification(String productIdentification) {
        return productMapper.selectByProductIdentification(productIdentification);
    }

    /**
     * 查询产品管理 带服务、属性
     *
     * @param id 产品管理主键
     * @return 产品管理
     */
    @Deprecated
    @Override
    public ProductModel selectFullProductById(Long id) {
        Product product = selectProductById(id);
        ProductModel productModel = new ProductModel();
        if (product != null) {
            BeanUtils.copyBeanProp(productModel, product);
            ProductServices find = new ProductServices();
            find.setProductIdentification(product.getProductIdentification());
            find.setStatus(Constants.ENABLE);
            // 查询服务列表
            List<ProductServices> productServicesList = productServicesService.selectProductServicesList(find);
            if (!productServicesList.isEmpty()) {
                List<Services> services = new ArrayList<>();
                productServicesList.forEach(ps -> {
                    Services service = new Services();
                    BeanUtils.copyBeanProp(service, ps);
                    service.setServiceId(String.valueOf(ps.getId()));
                    // 查询服务属性列表
                    List<ProductProperties> productPropertiesList = productPropertiesService.findAllByServiceId(ps.getId());
                    if (!productPropertiesList.isEmpty()) {
                        List<Properties> properties = new ArrayList<>();
                        productPropertiesList.forEach(pp -> {
                            Properties p = new Properties();
                            BeanUtils.copyBeanProp(p, pp);
                            properties.add(p);
                        });
                        service.setProperties(properties);
                    }
                    services.add(service);
                });
                productModel.setServices(services);
            }
        }
        return productModel;
    }

    /**
     * 查询产品管理列表
     *
     * @param product 产品管理
     * @return 产品管理
     */
    @Override
    public List<Product> selectProductList(Product product) {
        return productMapper.selectProductList(product);
    }

    /**
     * 新增产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    @Override
    public int insertProduct(Product product) {
        Product oneByProductName = productMapper.findOneByProductName(product.getProductName());
        if (StringUtils.isNotNull(oneByProductName)) {
            return -1;
        }
        product.setProductIdentification(SnowflakeIdUtil.nextId());
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        product.setCreateBy(sysUser.getUserName());
        return productMapper.insertProduct(product);
    }

    /**
     * 修改产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    @Override
    public int updateProduct(Product product) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        product.setUpdateBy(sysUser.getUserName());
        return productMapper.updateProduct(product);
    }

    /**
     * 批量删除产品管理
     *
     * @param ids 需要删除的产品管理主键
     * @return 结果
     */
    @Override
    public int deleteProductByIds(Long[] ids) {
        return productMapper.deleteProductByIds(ids);
    }

    /**
     * 删除产品管理信息
     *
     * @param id 产品管理主键
     * @return 结果
     */
    @Override
    public int deleteProductById(Long id) {
        return productMapper.deleteProductById(id);
    }

    @Override
    public Product findOneByProductName(String productName) {
        return productMapper.findOneByProductName(productName);
    }

    @Override
    public List<Product> selectByManufacturerIdAndModelAndDeviceType(String manufacturerId, String model, String deviceType) {
        return productMapper.selectByManufacturerIdAndModelAndDeviceType(manufacturerId, model, deviceType);
    }

    @Override
    public Product findOneByManufacturerIdAndModelAndDeviceType(String manufacturerId, String model, String deviceType) {
        return productMapper.findOneByManufacturerIdAndModelAndDeviceType(manufacturerId, model, deviceType);
    }

    @Override
    public List<Product> findAllByStatus(String status) {
        return productMapper.findAllByStatus(status);
    }

    @Override
    public Product findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(String manufacturerId, String model, String protocolType, String status) {
        return productMapper.findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(manufacturerId, model, protocolType, status);
    }

    @Override
    public Product findOneByIdAndStatus(Long id, String status) {
        return productMapper.findOneByIdAndStatus(id, status);
    }

    @Override
    public Product findOneByProductIdentificationAndProtocolType(String productIdentification, String protocolType) {
        return productMapper.findOneByProductIdentificationAndProtocolType(productIdentification, protocolType);
    }

    @Override
    public List<Product> findAllByIdInAndStatus(List<Long> ids, String status) {
        return productMapper.findAllByIdInAndStatus(ids, status);
    }


    /**
     * 根据状态获取所有的产品列表
     *
     * @param status
     * @return
     */
    @Override
    public List<Product> selectAllProductByStatus(String status) {
        return productMapper.selectAllProductByStatus(status);
    }

    public List<Product> selectProductByProductIdentificationList(List<String> productIdentificationList) {
        return productMapper.selectProductByProductIdentificationList(productIdentificationList);
    }

    @Override
    public ProductResultVO findOneByProductIdentification(String productIdentification) {
        return BeanPlusUtil.toBeanIgnoreError(productMapper.selectByProductIdentification(productIdentification), ProductResultVO.class);
    }

    @Override
    public Long findProductTotal() {
        return productMapper.findProductTotal();
    }

    @Override
    public List<Product> findProductsByPage(int offset, int pageSize) {
        return productMapper.findProductsByPage(offset, pageSize);
    }

    /**
     * 查询产品管理 带服务、属性、命令
     *
     * @param productIdentification 产品标识
     * @return
     */
    @Override
    public ProductParamVO selectFullProductByProductIdentification(String productIdentification) {
        return Optional.ofNullable(this.findOneByProductIdentification(productIdentification))
                .map(product -> {
                    ProductParamVO productDetails = BeanPlusUtil.toBeanIgnoreError(product, ProductParamVO.class);
                    ProductServices find = new ProductServices();
                    find.setProductIdentification(product.getProductIdentification());
                    find.setStatus(Constants.ENABLE);
                    List<ProductServices> productServicesList = productServicesService.selectProductServicesList(find);

                    List<Long> serviceIds = productServicesList.stream()
                            .map(ProductServices::getId)
                            .collect(Collectors.toList());

                    List<ProductCommands> productCommandList = Optional.ofNullable(
                                    productCommandsService.selectProductCommandsByServiceIdList(serviceIds))
                            .orElse(Collections.emptyList());

                    List<ProductProperties> productPropertiesList = Optional.ofNullable(
                                    productPropertiesService.selectPropertiesByServiceIdList(serviceIds))
                            .orElse(Collections.emptyList());

                    List<ProductServiceParamVO> services = productServicesList.stream()
                            .map(ps -> {
                                ProductServiceParamVO service = BeanPlusUtil.toBeanIgnoreError(ps, ProductServiceParamVO.class);

                                List<ProductCommandParamVO> commands = Optional.ofNullable(productCommandList)
                                        .orElse(Collections.emptyList())
                                        .stream()
                                        .filter(command -> command.getServiceId().equals(ps.getId()))
                                        .map(command -> {
                                            ProductCommandParamVO commandParamVO = BeanPlusUtil.toBeanIgnoreError(command, ProductCommandParamVO.class);

                                            // Simplifying the stream operations and request/response mapping
                                            List<ProductCommandRequestParamVO> filteredRequests = Optional.ofNullable(
                                                            productCommandsRequestsService.selectProductCommandsRequestsByCommandIdList(Arrays.asList(command.getId())))
                                                    .orElse(Collections.emptyList())
                                                    .stream()
                                                    .map(request -> BeanPlusUtil.toBeanIgnoreError(request, ProductCommandRequestParamVO.class))
                                                    .collect(Collectors.toList());

                                            commandParamVO.setRequests(filteredRequests);

                                            List<ProductCommandResponseParamVO> filteredResponses = Optional.ofNullable(
                                                            productCommandsResponseService.selectProductCommandsResponseByCommandIdList(Arrays.asList(command.getId())))
                                                    .orElse(Collections.emptyList())
                                                    .stream()
                                                    .map(response -> BeanPlusUtil.toBeanIgnoreError(response, ProductCommandResponseParamVO.class))
                                                    .collect(Collectors.toList());

                                            commandParamVO.setResponses(filteredResponses);

                                            return commandParamVO;
                                        })
                                        .collect(Collectors.toList());
                                service.setCommands(commands);

                                List<ProductPropertyParamVO> properties = Optional.ofNullable(productPropertiesList)
                                        .orElse(Collections.emptyList())
                                        .stream()
                                        .filter(property -> property.getServiceId().equals(ps.getId()))
                                        .map(pp -> BeanPlusUtil.toBeanIgnoreError(pp, ProductPropertyParamVO.class))
                                        .collect(Collectors.toList());
                                service.setProperties(properties);

                                return service;
                            })
                            .collect(Collectors.toList());

                    productDetails.setServices(services);
                    return productDetails;
                })
                .orElse(new ProductParamVO()); // Return an empty ProductParamVO object if the initial product is null
    }

    @Override
    public String productEmpowerment(Long[] productIds) {
        List<Product> productList;
        if (null == productIds) {
            productList = productMapper.findAllByStatus(Constants.ENABLE);
        } else {
            productList = productMapper.findAllByIdInAndStatus(Arrays.asList(productIds), Constants.ENABLE);
        }
        if (productList.isEmpty()) {
            return "No product data";
        }
        List<EmpowermentRecordResultVO> empowermentRecordResultVOS = new ArrayList<>();

        productList.forEach(product -> {
            log.info("Processing product with identification: {}", product.getProductIdentification());
            EmpowermentRecordResultVO record = new EmpowermentRecordResultVO();
            LocalDateTime startTime = LocalDateTime.now();
            record.setStartTime(startTime);
            List<String> feedbackList = new ArrayList<>();

            String productIdentification = "";
            String productName = "";

            Optional<ProductParamVO> productOpt = Optional.ofNullable(this.selectFullProductByProductIdentification(product.getProductIdentification()));
            if (productOpt.isPresent()) {
                ProductParamVO productParamVO = productOpt.get();
                ProductTypeEnum productTypeEnum = ProductTypeEnum.fromValue(productParamVO.getProductType()).get();
                record.setAppId(productParamVO.getAppId());
                record.setEmpowermentIdentification(productParamVO.getProductIdentification());
                productIdentification = productParamVO.getProductIdentification();
                productName = productParamVO.getProductName();

                productParamVO.getServices().forEach(service -> {
                    String superTableName = TdsUtils.superTableName(String.valueOf(productTypeEnum.getDesc()), productParamVO.getProductIdentification(), service.getServiceCode());
                    R<List<SuperTableDescribeVO>> superTableDescribeVOListR = remoteTdEngineService.describeSuperOrSubTable(superTableName);

                    List<SuperTableDescribeVO> existingFields = Optional.ofNullable(superTableDescribeVOListR.getData()).orElse(Collections.emptyList());

                    if (existingFields.isEmpty()) {
                        String feedback = createNewSuperTableStructure(service, superTableName);
                        feedbackList.add(feedback);
                    } else {
                        String feedback = updateSuperTableStructure(service, superTableName, existingFields);
                        feedbackList.add(feedback);
                    }

                    //save to redis
                    Optional<List<SuperTableDescribeVO>> superTableDescribeOpt = Optional.ofNullable(
                            remoteTdEngineService.describeSuperOrSubTable(superTableName).getData()
                    );
                    if (superTableDescribeOpt.isPresent() && !superTableDescribeOpt.get().isEmpty()) {
                        cacheDataHelper.setProductModelSuperTableCacheVO(productParamVO.getProductIdentification(), service.getServiceCode(), superTableDescribeOpt.get());
                    }

                });

                log.info("Product processed: {}", product.getProductIdentification());
            } else {
                log.warn("Product not found for product ID: {}", product.getProductIdentification());
            }

            LocalDateTime endTime = LocalDateTime.now();
            record.setEndTime(endTime);

            List<Map<String, Object>> feedbacks = feedbackList.stream()
                    .map(feedback -> {
                        Map<String, Object> map = new HashMap<>();
                        // Using epoch time in milliseconds as the timestamp
                        map.put("timestamp", System.currentTimeMillis());
                        map.put("message", feedback);
                        return map;
                    })
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();

            String jsonFeedback = "";
            try {
                jsonFeedback = mapper.writeValueAsString(feedbacks);
            } catch (JsonProcessingException e) {
                log.error("Error converting feedback to JSON. Using default empty value.", e);
            }

            record.setFeedback(jsonFeedback);


            record.setStatus(EmpowermentStatusEnum.COMPLETED.getValue());

            Duration duration = Duration.between(startTime, endTime);
            record.setOutcome(String.format("Processed product with identification: %s, name: %s. Total time taken: %s seconds.",
                    productIdentification, productName, duration.getSeconds()));


            log.info("Empowerment record: {}", record);
            empowermentRecordResultVOS.add(record);
        });

        // TODO save empowerment records to database
        log.info("Empowerment records: {}, count: {}", empowermentRecordResultVOS, empowermentRecordResultVOS.size());
        return "Empowerment process completed. Total records: " + empowermentRecordResultVOS.size();

    }


    private String createNewSuperTableStructure(ProductServiceParamVO service, String superTableName) {
        StringBuilder feedback = new StringBuilder("Creating new super table: ").append(superTableName).append(". ");

        SuperTableDTO superTableDTO = new SuperTableDTO();
        superTableDTO.setSuperTableName(superTableName);

        List<FieldsVO> schemaFields = new ArrayList<>(Arrays.asList(
                new FieldsVO(TdsConstants.TS, DataTypeEnum.TIMESTAMP.getDataType(), null),
                new FieldsVO(TdsConstants.EVENT_TIME, DataTypeEnum.TIMESTAMP.getDataType(), null)
        ));

        service.getProperties().forEach(property -> {
            String fieldName = property.getPropertyCode();
            Integer size = Optional.ofNullable(property.getMaxlength()).map(Integer::parseInt).orElse(null);
            schemaFields.add(new FieldsVO(fieldName, DataTypeEnum.valueOfByDataType(property.getDatatype()).getDataType(), size));
        });

        List<FieldsVO> tagsFields = Collections.singletonList(new FieldsVO(TdsConstants.DEVICE_IDENTIFICATION, DataTypeEnum.BINARY.getDataType(), 64));

        superTableDTO.setSchemaFields(FieldsVO.toFieldsList(schemaFields));
        superTableDTO.setTagsFields(FieldsVO.toFieldsList(tagsFields));

        log.info("catch superTableDTO:{}", JSONUtil.toJsonStr(superTableDTO));
        R superTableAndColumn = remoteTdEngineService.createSuperTableAndColumn(superTableDTO);
        if (ResultEnum.SUCCESS.getCode() != superTableAndColumn.getCode()) {
            feedback.append("Creation of field(s) failed with message: ").append(superTableAndColumn.getMsg()).append(Constants.SEMICOLON);
        } else {
            feedback.append("Successfully created fields: ").append(FieldsVO.toFieldsList(schemaFields).stream().map(Fields::getFieldName).collect(Collectors.joining(Constants.SEPARATOR))).append(Constants.SEMICOLON);
        }
        return feedback.toString();
    }

    private String updateSuperTableStructure(ProductServiceParamVO service, String superTableName, List<SuperTableDescribeVO> existingFields) {
        StringBuilder feedback = new StringBuilder("Updating super table: ").append(superTableName).append(". ");
        SuperTableDTO superTableDTO = new SuperTableDTO();
        superTableDTO.setDataBaseName("");
        superTableDTO.setSuperTableName(superTableName);

        List<String> existingFieldNames = existingFields.stream()
                .filter(describeVO -> !Objects.equals(TdsConstants.TAG, describeVO.getNote()))
                .map(SuperTableDescribeVO::getField)
                .collect(Collectors.toList());

        service.getProperties().forEach(property -> {
            String fieldName = property.getPropertyCode();
            DataTypeEnum dataTypeEnum = DataTypeEnum.valueOfByDataType(property.getDatatype());
            Integer size = Optional.ofNullable(property.getMaxlength())
                    .map(Integer::parseInt)
                    .orElse(null);

            Optional<SuperTableDescribeVO> matchedFieldOpt = existingFields.stream()
                    .filter(f -> Objects.equals(f.getField(), fieldName))
                    .findFirst();

            boolean isTypeMatch = matchedFieldOpt.map(field -> DataTypeEnum.valueOfByDataType(field.getType()))
                    .map(fieldTypeEnum -> dataTypeEnum.isTypeEqual(fieldTypeEnum.getDataType()))
                    .orElse(false);

            boolean isSizeMatch = matchedFieldOpt.map(SuperTableDescribeVO::getLength)
                    .map(length -> Objects.equals(length, size))
                    .orElse(false);

            // Check if type or size mismatch
            if (matchedFieldOpt.isPresent() && (!isTypeMatch || !isSizeMatch)) {
                // Delete the existing field first if type or size doesn't match
                superTableDTO.setFields(FieldsVO.toFields(new FieldsVO(fieldName, null, null)));
                remoteTdEngineService.dropSuperTableColumn(superTableDTO);
            }

            // Check if field is absent or type or size mismatch
            if (!matchedFieldOpt.isPresent() || !isTypeMatch || !isSizeMatch) {
                // Add or alter field if not matched or not matching in type or size
                FieldsVO fieldsVO = new FieldsVO(fieldName, dataTypeEnum.getDataType(), size);
                superTableDTO.setFields(FieldsVO.toFields(fieldsVO));
                R alterSuperTableColumn = remoteTdEngineService.alterSuperTableColumn(superTableDTO);
                if (ResultEnum.SUCCESS.getCode() != alterSuperTableColumn.getCode()) {
                    feedback.append("Alteration of field(s) failed with message: ").append(alterSuperTableColumn.getMsg()).append(Constants.SEMICOLON);
                } else {
                    feedback.append("Successfully altered fields: ").append(fieldName).append(Constants.SEMICOLON);
                }
            }
        });


        // Check for fields that should be deleted
        existingFieldNames.stream()
                .filter(existingFieldName -> !Arrays.asList(TdsConstants.TS, TdsConstants.EVENT_TIME).contains(existingFieldName))
                .filter(existingFieldName -> service.getProperties().stream().noneMatch(p -> Objects.equals(p.getPropertyCode(), existingFieldName)))
                .forEach(existingFieldName -> {
                    FieldsVO fieldsVO = new FieldsVO();
                    fieldsVO.setFieldName(existingFieldName);
                    superTableDTO.setFields(FieldsVO.toFields(fieldsVO));
                    R alterSuperTableColumn = remoteTdEngineService.dropSuperTableColumn(superTableDTO);
                    if (ResultEnum.SUCCESS.getCode() != alterSuperTableColumn.getCode()) {
                        feedback.append("Deletion of field: ").append(existingFieldName).append(" failed with message: ").append(alterSuperTableColumn.getMsg()).append(Constants.SEMICOLON);
                    } else {
                        feedback.append("Successfully deleted field: ").append(existingFieldName).append(Constants.SEMICOLON);
                    }
                });

        return feedback.toString();
    }


}

