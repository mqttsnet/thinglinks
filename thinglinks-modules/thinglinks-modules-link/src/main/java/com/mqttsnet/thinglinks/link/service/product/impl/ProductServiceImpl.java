package com.mqttsnet.thinglinks.link.service.product.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DataTypeEnum;
import com.mqttsnet.thinglinks.common.core.text.CharsetKit;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.common.rocketmq.domain.MQMessage;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Properties;
import com.mqttsnet.thinglinks.link.mapper.product.ProductMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductPropertiesService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
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
import java.util.*;

/**

* @Description:    产品模型业务层
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/25$ 23:52$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/25$ 23:52$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
@Service
@Slf4j
@RefreshScope
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProductServicesService productServicesService;
    @Autowired
    private ProductPropertiesService productPropertiesService;
    @Resource
    private RemoteTdEngineService remoteTdEngineService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

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
        record.setCreateTime(DateUtils.getNowDate());
        return productMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        if (StringUtils.isEmpty(String.valueOf(record.getId()))){
            record.setCreateBy(sysUser.getUserName());
            record.setCreateTime(DateUtils.getNowDate());
        }else {
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(sysUser.getUserName());
        }
        return productMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        if (StringUtils.isEmpty(String.valueOf(record.getId()))){
            record.setCreateBy(sysUser.getUserName());
            record.setCreateTime(DateUtils.getNowDate());
        }else {
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(sysUser.getUserName());
        }
        return productMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setCreateTime(DateUtils.getNowDate());
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
        record.setUpdateTime(DateUtils.getNowDate());
        record.setUpdateBy(sysUser.getUserName());
        return productMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Product record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateTime(DateUtils.getNowDate());
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
     * @param file json文件
     * @param updateSupport 是否更新已经存在的产品模型数据
     * @param appId 应用ID
     * @param templateId  产品模型模板ID
     * @param status 状态(字典值：启用  停用)
     * @return AjaxResult
     * @throws Exception
     */
    @Override
    public AjaxResult importProductJson(MultipartFile file,Boolean updateSupport,String appId,String templateId,String status) throws Exception {
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
                    return this.productJsonDataAnalysis(JSONObject.parseObject(sb.toString()), appId, templateId, status);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } else {
            //非法文件
            return AjaxResult.error("the picture's suffix is illegal");
        }
        return null;
    }

    /**
     * 解析产品模型数据
     *
     * @param content 产品模型数据
     * @param appId 应用ID
     * @param templateId  产品模型模板ID
     * @param status 状态(字典值：启用  停用)
     * @return 解析结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public AjaxResult productJsonDataAnalysis(JSONObject content,String appId,String templateId,String status) throws Exception{
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
            if (StringUtils.isNotNull(oneByManufacturerIdAndModelAndDeviceType)){
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
                    parsingErrorMessages.put("datatype:"+item,"Invalid product: Invalid dataType,must be one of [int、decimal、string、binary、bool、timestamp、json]");
                }
            });
            if (!parsingErrorMessages.isEmpty()){
                return AjaxResult.error(JSONObject.parseObject(parsingErrorMessages.toString()).toJSONString());
            }
            //服务属性解析处理
            Product product = new Product();
            product.setAppId(appId);
            if (StringUtils.isNotEmpty(templateId)){
                product.setTemplateId(Long.valueOf(templateId));
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
            product.setCreateTime(DateUtils.getNowDate());
            final int insertProduct = productMapper.insertProduct(product);
            if (insertProduct==0){
                return AjaxResult.error("Product model storage error");
            }
            //添加服务数据
            JSONArray services = content.getJSONArray("services");
            for (int i = 0; i < services.size(); i++) {
                JSONObject service = services.getJSONObject(i);
                ProductServices productServices = new ProductServices();
                productServices.setServiceName(service.getString("serviceId"));
                productServices.setProductId(product.getId());
                productServices.setStatus(product.getStatus());
                productServices.setDescription(service.getString("description"));
                productServices.setCreateBy(sysUser.getUserName());
                productServices.setCreateTime(DateUtils.getNowDate());
                final int insertSelective = productServicesService.insertSelective(productServices);
                if (insertSelective==0) {
                    throw new RuntimeException("Service capability Data storage fails");
                }
                //添加属性数据
                JSONArray properties = service.getJSONArray("properties");
                for (int j = 0; j < properties.size(); j++) {
                    JSONObject propertie = properties.getJSONObject(j);
                    ProductProperties productProperties = new ProductProperties();
                    BeanUtils.copyProperties(propertie.toJavaObject(Properties.class),productProperties);
                    productProperties.setServiceId(productServices.getId());
                    productProperties.setCreateBy(sysUser.getUserName());
                    productProperties.setCreateTime(DateUtils.getNowDate());
                    final int batchInsert = productPropertiesService.insertSelective(productProperties);
                }
            }
            //解析入库成功创建TD超级表及子表
            this.createSuperTable(product,services);
        }catch (Exception e){
            log.error(e.getMessage());
            return AjaxResult.error("操作失败");
        }
        return AjaxResult.success("操作成功");
    }

    /**
     * 根据产品模型创建超级表
     * @param product
     * @param services
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public AjaxResult createSuperTable(Product product,JSONArray services) throws Exception{
        //构建超级表入参对象
        SuperTableDto superTableDto = new SuperTableDto();
        try {
            loop:
            for (int i = 0; i < services.size(); i++) {
                JSONObject service = services.getJSONObject(i);
                //超级表名称命名规则:产品类型_产品标识_服务名称
                String superTableName = product.getProductType()+"_"+product.getProductIdentification()+"_"+service.getString("serviceId");
                //设置数据库名称和超级表名称
                superTableDto.setDataBaseName(dataBaseName);
                superTableDto.setSuperTableName(superTableName);
                //构建超级表的表结构字段列表
                JSONArray properties = service.getJSONArray("properties");
                //如果服务下属性值为空，没必要为该服务创建超级表，跳过该循环，进入下个服务
                if (properties.isEmpty()) {
                    continue;
                }
                //构建超级表的表结构字段列表
                List<Fields> schemaFields = new ArrayList<>();
                //超级表第一个字段数据类型必须为时间戳,默认Ts为当前系统时间
                Fields tsColumn = new Fields();
                tsColumn.setFieldName("ts");
                tsColumn.setDataType(DataTypeEnum.TIMESTAMP);
                schemaFields.add(tsColumn);
                //超级表第二个字段为事件发生时间数据类型必须为时间戳
                Fields eventTimeColumn = new Fields();
                eventTimeColumn.setFieldName("event_time");
                eventTimeColumn.setDataType(DataTypeEnum.TIMESTAMP);
                schemaFields.add(eventTimeColumn);
                //根据属性对象列表循环构建超级表表结构
                for (int j = 0; j < properties.size(); j++) {
                    JSONObject propertie = properties.getJSONObject(j);
                    //获取字段名称
                    String filedName = (String) propertie.get("name");
                    //获取该属性数据类型
                    String datatype = (String) propertie.get("datatype");
                    //获取该属性的数据大小
                    Integer size = (Integer) propertie.get("maxlength");
                    //添加超级表表结构字段
                    Fields fields = new Fields(filedName, datatype, size);
                    schemaFields.add(fields);
                }
                //构建超级表标签字段列表
                //根据业务逻辑，将超级表的标签字段定为
                // 1:设备标识：deviceIdentification
                List<Fields> tagsFields = new ArrayList<>();
                Fields tags = new Fields();
                tags.setFieldName("device_identification");
                tags.setDataType(DataTypeEnum.BINARY);
                tags.setSize(64);
                tagsFields.add(tags);

                //设置超级表表结构列表
                superTableDto.setSchemaFields(schemaFields);
                //设置超级表标签字段列表
                superTableDto.setTagsFields(tagsFields);
                R<?> cstResult = remoteTdEngineService.createSuperTable(superTableDto);
                //创建超级表报错，打印报错信息，并跳过该循环，继续为下个服务创建表
                if (cstResult.getCode() != 200) {
                    log.error("Create SuperTable Exception: " + cstResult.getMsg());
                    continue loop;
                }
                log.info("Create SuperTable Result: {}",cstResult.getCode());
                //将之前存在redis里的同样的名称的超级表的表结构信息删除
                if (redisService.hasKey(Constants.TDENGINE_SUPERTABLEFILELDS+superTableName)) {
                    redisService.deleteObject(Constants.TDENGINE_SUPERTABLEFILELDS+superTableName);
                }
                //在redis里存入新的超级表对的表结构信息
                redisService.setCacheObject(Constants.TDENGINE_SUPERTABLEFILELDS + superTableName, superTableDto);
                log.info("缓存超级表数据模型:{}",JSON.toJSONString(superTableDto));
            }
        }catch (Exception e){
         log.error(e.getMessage());
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
    public Product selectProductById(Long id)
    {
        return productMapper.selectProductById(id);
    }

    /**
     * 查询产品管理列表
     *
     * @param product 产品管理
     * @return 产品管理
     */
    @Override
    public List<Product> selectProductList(Product product)
    {
        return productMapper.selectProductList(product);
    }

    /**
     * 新增产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    @Override
    public int insertProduct(Product product)
    {
        Product oneByProductName = productMapper.findOneByProductName(product.getProductName());
        if(StringUtils.isNotNull(oneByProductName)){
            return 0;
        }
        product.setProductIdentification(UUID.getUUID());
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        product.setCreateBy(sysUser.getUserName());
        product.setCreateTime(DateUtils.getNowDate());
        return productMapper.insertProduct(product);
    }

    /**
     * 修改产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    @Override
    public int updateProduct(Product product)
    {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        product.setUpdateTime(DateUtils.getNowDate());
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
    public int deleteProductByIds(Long[] ids)
    {
        return productMapper.deleteProductByIds(ids);
    }

    /**
     * 删除产品管理信息
     *
     * @param id 产品管理主键
     * @return 结果
     */
    @Override
    public int deleteProductById(Long id)
    {
        return productMapper.deleteProductById(id);
    }

	@Override
	public Product findOneByProductName(String productName){
		 return productMapper.findOneByProductName(productName);
	}

	@Override
	public List<Product> selectByManufacturerIdAndModelAndDeviceType(String manufacturerId,String model,String deviceType){
		 return productMapper.selectByManufacturerIdAndModelAndDeviceType(manufacturerId,model,deviceType);
	}

	@Override
	public Product findOneByManufacturerIdAndModelAndDeviceType(String manufacturerId,String model,String deviceType){
		 return productMapper.findOneByManufacturerIdAndModelAndDeviceType(manufacturerId,model,deviceType);
	}

	@Override
	public List<Product> findAllByStatus(String status){
		 return productMapper.findAllByStatus(status);
	}


    /**
     * 初始化生成超级表模型
     * @param productIds 产品ID集合  productIds==null 初始化所有产品:productIds!=null 初始化指定产品
     * @param InitializeOrNot  是否初始化
     * @return
     * @throws Exception
     */
    @Async
    @Override
    public List<SuperTableDto> createSuperTableDataModel(Long[] productIds,Boolean InitializeOrNot)throws Exception{
        List<SuperTableDto> superTableDtoList = new ArrayList<>();
        List<Product>  productList = new ArrayList<>();
        if (null==productIds) {
            productList = this.findAllByStatus("0");
        }else {
            productList = this.findAllByIdInAndStatus(Arrays.asList(productIds),"0");
        }
        SuperTableDto superTableDto;
        loop:
        for (Product product : productList) {
            List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdAndStatus(product.getId(), "0");
            if(StringUtils.isEmpty(allByProductIdAndStatus)){
                continue;
            }
            for (ProductServices productServices : allByProductIdAndStatus) {
                superTableDto = new SuperTableDto();
                if(StringUtils.isNull(productServices)){
                    continue loop;
                }
                //超级表名称命名规则:产品类型_产品标识_服务名称
                String superTableName = product.getProductType()+"_"+product.getProductIdentification()+"_"+productServices.getServiceName();
                //设置数据库名称和超级表名称
                superTableDto.setDataBaseName(dataBaseName);
                superTableDto.setSuperTableName(superTableName);
                //构建超级表的表结构字段列表
                List<ProductProperties> allByServiceId = productPropertiesService.findAllByServiceId(productServices.getId());
                //如果服务下属性值为空，没必要为该服务创建超级表，跳过该循环，进入下个服务
                if(StringUtils.isNull(allByServiceId)){
                    continue loop;
                }
                //构建超级表的表结构字段列表
                List<Fields> schemaFields = new ArrayList<>();
                //超级表第一个字段数据类型必须为时间戳,默认Ts为当前系统时间
                Fields tsColumn = new Fields();
                tsColumn.setFieldName("ts");
                tsColumn.setDataType(DataTypeEnum.TIMESTAMP);
                schemaFields.add(tsColumn);
                //超级表第二个字段为事件发生时间数据类型必须为时间戳
                Fields eventTimeColumn = new Fields();
                eventTimeColumn.setFieldName("event_time");
                eventTimeColumn.setDataType(DataTypeEnum.TIMESTAMP);
                schemaFields.add(eventTimeColumn);
                //根据属性对象列表循环构建超级表表结构
                for (ProductProperties productProperties : allByServiceId) {
                    //获取字段名称
                    String filedName = productProperties.getName();
                    //获取该属性数据类型
                    String datatype = productProperties.getDatatype();
                    //获取该属性的数据大小
                    Integer size = productProperties.getMaxlength();
                    //添加超级表表结构字段
                    Fields fields = new Fields(filedName, datatype, size);
                    schemaFields.add(fields);
                }
                //构建超级表标签字段列表
                //根据业务逻辑，将超级表的标签字段定为
                // 1:设备标识：deviceIdentification
                List<Fields> tagsFields = new ArrayList<>();
                Fields tags = new Fields();
                tags.setFieldName("device_identification");
                tags.setDataType(DataTypeEnum.BINARY);
                tags.setSize(64);
                tagsFields.add(tags);

                //设置超级表表结构列表
                superTableDto.setSchemaFields(schemaFields);
                //设置超级表标签字段列表
                superTableDto.setTagsFields(tagsFields);
                //将之前存在redis里的同样的名称的超级表的表结构信息删除
                if (redisService.hasKey(Constants.TDENGINE_SUPERTABLEFILELDS+superTableName)) {
                    redisService.deleteObject(Constants.TDENGINE_SUPERTABLEFILELDS+superTableName);
                }
                //在redis里存入新的超级表对的表结构信息
                redisService.setCacheObject(Constants.TDENGINE_SUPERTABLEFILELDS + superTableName, superTableDto);
                log.info("缓存超级表数据模型:{}",JSON.toJSONString(superTableDto));
                superTableDtoList.add(superTableDto);
                if (InitializeOrNot){
                    //推送RocketMq消息初始化超级表
                    MQMessage mqMessage = new MQMessage();
                    mqMessage.setTopic(ConsumerTopicConstant.PRODUCTSUPERTABLE_CREATEORUPDATE);
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type","create");
                    jsonObject.put("msg",JSON.toJSONString(superTableDto));
                    mqMessage.setMessage(jsonObject.toJSONString());
                    rocketMQTemplate.convertAndSend(mqMessage.getTopic(), mqMessage.getMessage());
                }
            }
        }
        return superTableDtoList;
    }

	@Override
	public Product findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(String manufacturerId,String model,String protocolType,String status){
		 return productMapper.findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(manufacturerId,model,protocolType,status);
	}

	@Override
	public Product findOneByIdAndStatus(Long id,String status){
		 return productMapper.findOneByIdAndStatus(id,status);
	}

	@Override
	public Product findOneByProductIdentificationAndProtocolType(String productIdentification,String protocolType){
		 return productMapper.findOneByProductIdentificationAndProtocolType(productIdentification,protocolType);
	}

	@Override
	public List<Product> findAllByIdInAndStatus(Collection<Long> idCollection, String status){
		 return productMapper.findAllByIdInAndStatus(idCollection,status);
	}













}
