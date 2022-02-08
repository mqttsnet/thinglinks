package com.mqttsnet.thinglinks.link.service.product.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.common.core.text.CharsetKit;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mqttsnet.thinglinks.link.mapper.product.ProductMapper;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**

* @Description:    java类作用描述
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
public class ProductServiceImpl implements ProductService{

    @Resource
    private ProductMapper productMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Product record) {
        return productMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Product record) {
        return productMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Product record) {
        return productMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Product record) {
        return productMapper.insertSelective(record);
    }

    @Override
    public Product selectByPrimaryKey(Long id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Product record) {
        return productMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Product record) {
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
     * 产品模型导入
     *
     * @param file
     * @return AjaxResult
     * @throws Exception
     */
    @Override
    public AjaxResult importProductJson(MultipartFile file) throws Exception {
        // 首先校验json格式
        List<String> imageType = Lists.newArrayList("json");
        List<String> zip = Lists.newArrayList("zip", "rar", "7z");
        // 获取文件名，带后缀
        String originalFilename = file.getOriginalFilename();
        // 获取文件的后缀格式
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        StringBuilder sb = new StringBuilder();
        if (imageType.contains(fileSuffix)) {
            try (InputStream inputStream = file.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CharsetKit.getFilecharset(file.getInputStream())));
                sb = new StringBuilder();
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return insertProduct(JSONObject.parseObject(sb.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (zip.contains(fileSuffix)) {
            // 非法文件
            InputStream inputStream = file.getInputStream();
            return readZip(inputStream);
            //   log.error("the picture's suffix is illegal");
        } else {
            //非法文件
            return AjaxResult.error("the picture's suffix is illegal");
        }
        return null;
    }

    public AjaxResult readZip(InputStream inputStream) throws Exception {
        //获取ZIP输入流(一定要指定字符集Charset.forName("GBK")否则会报java.lang.IllegalArgumentException: MALFORMED)
        ZipInputStream zipInputStream = new ZipInputStream(inputStream, Charset.forName("UTF-8"));
        //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
        ZipEntry ze = null;
        AjaxResult ajaxResult = null;
        //循环遍历
        while ((ze = zipInputStream.getNextEntry()) != null) {
            log.info("文件名：" + ze.getName() + " 文件大小：" + ze.getSize() + " bytes");
            log.info("文件内容：");
            //读取
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream, Charset.forName("UTF-8")));
            String line;
            //内容不为空，输出
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            ajaxResult = insertProduct(JSONObject.parseObject(sb.toString()));
        }
        //一定记得关闭流
        zipInputStream.closeEntry();
        inputStream.close();
        return ajaxResult;
    }

    /**
     * 新增导入产品模型
     *
     * @param content 产品模型
     * @return 结果
     */
    @Override
    @Transactional
    public AjaxResult insertProduct(JSONObject content) throws Exception{
        //分析json取值
        String manufacturerId = content.getString("manufacturerId");
        String manufacturerName = content.getString("manufacturerName");
        String model = content.getString("model");
        String productName = content.getString("productName");
        String productType = content.getString("productType");
        String status = content.getString("status");
        String productSerial = content.getString("productSerial");
        String version = content.getString("version");
        //效验数据是否存在
        Product query = new Product();
        query.setUserName(loginUser.getUsername());
        query.setModel(model);
        query.setManufacturerId(manufacturerId);
        List<Product> getList = productMapper.selectListByProduct(query);
        if (getList != null && getList.size()>0){
            return AjaxResult.error("产品模型已上传");
        }
        //验证properties数据格式（int、decimal、string、bool、dateTime、jsonObject）
        List read1 = JsonPath.read(content.toJSONString(), "$..properties[*].datatype");
        List read2 = JsonPath.read(content.toJSONString(), "$..properties[*].description");
        List read3 = JsonPath.read(content.toJSONString(), "$..properties[*].maxlength");
        List read4 = JsonPath.read(content.toJSONString(), "$..properties[*].method");
        List read5 = JsonPath.read(content.toJSONString(), "$..properties[*].name");
        List read6 = JsonPath.read(content.toJSONString(), "$..properties[*].required");
        List read7 = JsonPath.read(content.toJSONString(), "$..properties[*].step");
        List read8 = JsonPath.read(content.toJSONString(), "$..properties[*].unit");
        List booleans = new ArrayList<>();
        booleans.add(read1.size());
        booleans.add(read2.size());
        booleans.add(read3.size());
        booleans.add(read4.size());
        booleans.add(read5.size());
        booleans.add(read6.size());
        booleans.add(read7.size());
        booleans.add(read8.size());
        ArrayList<Object> list = new ArrayList<>();
        read1.stream().forEach(p -> {
            boolean b = "int".equals(p) || "decimal".equals(p) || "string".equals(p)
                    || "bool".equals(p) || "dateTime".equals(p) || "jsonObject".equals(p);
            list.add(b);
        });
        if (list.contains(false)) {
            return AjaxResult.error("Invalid product: Invalid dataType,must be one of [int、decimal、string、bool、dateTime、jsonObject]");
        }
        //添加物模型数据
        Product product = new Product();
        product.setProductName(productName);
        product.setManufacturerId(manufacturerId);
        product.setManufacturerName(manufacturerName);
        product.setProdoctType(productType);
        product.setModel(model);
        product.setProductSerial(productSerial);
        product.setVersion(version);
        product.setStatus(status);
        product.setContent(content.toJSONString().replaceAll("\\\\",""));
        product.setCreateDate(DateUtil.now());
        product.setUpdateDate(new Date());
        product.setUserName(loginUser.getUsername());
        if ("1".equals(productType)) {
            product.setLineType(ClientConstant.LINETYPE_WG);
        } else if ("0".equals(productType)) {
            product.setLineType(ClientConstant.LINETYPE_ZL);
        }
        productMapper.insertProduct(product);
        //添加服务数据
        JSONArray jsonArray = content.getJSONArray("services");
        for (int i = 0; i < jsonArray.size(); i++) {
            ProductServices services = new ProductServices();
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            services.setServiceid(jsonObject1.getString("serviceId"));
            services.setDescription(jsonObject1.getString("description"));
            services.setProductid(product.getId());
            services.setCreatedate(DateUtil.now());
            services.setAccount(product.getUserName());
            productServicesMapper.insertProductServices(services);
            //添加属性数据
            List<ProductPropertis> propertiesList = new ArrayList<>();
            JSONArray jsonArray1 = jsonObject1.getJSONArray("properties");
            for (int j = 0; j < jsonArray1.size(); j++) {
                ProductPropertis properties = new ProductPropertis();
                JSONObject jsonObject2 = jsonArray1.getJSONObject(j);
                properties.setServicesId(String.valueOf(services.getId()));
                properties.setUnit(jsonObject2.getString("unit"));
                properties.setDescription(jsonObject2.getString("description"));
                properties.setName(jsonObject2.getString("name"));
                properties.setAccount(services.getAccount());
                properties.setCreatedate(DateUtil.now());
                propertiesList.add(properties);
            }
            productServicesMapper.batchProductPropertis(propertiesList);
        }
        return AjaxResult.success("操作成功");
    }

}
