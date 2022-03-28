package com.mqttsnet.thinglinks.link.service.product.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.common.core.text.CharsetKit;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Commands;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Properties;
import com.mqttsnet.thinglinks.link.api.domain.product.model.Services;
import com.mqttsnet.thinglinks.link.mapper.product.ProductMapper;
import com.mqttsnet.thinglinks.link.mapper.product.ProductPropertiesMapper;
import com.mqttsnet.thinglinks.link.mapper.product.ProductServicesMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cn.hutool.json.XMLTokener.entity;

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
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService{

    @Resource
    private ProductMapper productMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProductServicesMapper productServicesMapper;
    @Autowired
    private ProductPropertiesMapper productPropertiesMapper;

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
                    return productJsonDataAnalysis(JSONObject.parseObject(sb.toString()),appId,templateId,status);
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
            //验证datatype数据格式（int、decimal、string、bool、dateTime、jsonObject）
            readDatatype.forEach(item -> {
                boolean flag = "int".equals(item) || "decimal".equals(item) || "string".equals(item)
                        || "bool".equals(item) || "dateTime".equals(item) || "jsonObject".equals(item);
                list.add(flag);
                if (list.contains(false)) {
                    parsingErrorMessages.put("datatype:"+item,"Invalid product: Invalid dataType,must be one of [int、decimal、string、bool、dateTime、jsonObject]");
                }
            });
            if (!parsingErrorMessages.isEmpty()){
                return AjaxResult.error(JSONObject.parseObject(parsingErrorMessages.toString()).toJSONString());
            }
            //服务属性解析处理
            Product product = new Product();
            product.setAppId(appId);
            product.setTemplateId(Long.valueOf(templateId));
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
                final int insertSelective = productServicesMapper.insertSelective(productServices);
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
                    final int batchInsert = productPropertiesMapper.insertSelective(productProperties);
                }

            }
        }catch (Exception e){
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
	public ProductServices findOneByProductId(Long productId){
		 return productServicesMapper.findOneByProductId(productId);
	}










}
