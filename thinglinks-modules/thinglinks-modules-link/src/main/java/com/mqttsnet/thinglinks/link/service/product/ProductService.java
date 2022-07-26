package com.mqttsnet.thinglinks.link.service.product;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.model.ProductModel;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**

* @Description:    产品服务接口
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/25$ 23:52$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/25$ 23:52$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
public interface ProductService{


    int deleteByPrimaryKey(Long id);

    int insert(Product record);

    int insertOrUpdate(Product record);

    int insertOrUpdateSelective(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    int updateBatch(List<Product> list);

    int updateBatchSelective(List<Product> list);

    int batchInsert(List<Product> list);

    /**
     * 产品模型导入
     * @param file json文件
     * @param updateSupport 是否更新已经存在的产品模型数据
     * @param appId 应用ID
     * @param templateId  产品模型模板ID
     * @param status 状态(字典值：启用  停用)
     * @return AjaxResult
     * @throws Exception
     */
    AjaxResult importProductJson(MultipartFile file,Boolean updateSupport,String appId,String templateId,String status) throws Exception;


    /**
     *
     * 解析产品模型数据
     * @param content 产品模型数据
     * @param appId 应用ID
     * @param templateId  产品模型模板ID
     * @param status 状态(字典值：启用  停用)
     * @return 解析结果
     * @throws Exception
     */
    public AjaxResult productJsonDataAnalysis(JSONObject content, String appId, String templateId, String status) throws Exception;

    /**
     * 查询产品管理
     *
     * @param id 产品管理主键
     * @return 产品管理
     */
    public Product selectProductById(Long id);

    /**
     * 查询产品管理 带服务、属性
     *
     * @param id 产品管理主键
     * @return 产品管理
     */
    public ProductModel selectFullProductById(Long id);

    /**
     * 查询产品管理列表
     *
     * @param product 产品管理
     * @return 产品管理集合
     */
    public List<Product> selectProductList(Product product);

    /**
     * 新增产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    public int insertProduct(Product product);

    /**
     * 修改产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    public int updateProduct(Product product);

    /**
     * 批量删除产品管理
     *
     * @param ids 需要删除的产品管理主键集合
     * @return 结果
     */
    public int deleteProductByIds(Long[] ids);

    /**
     * 删除产品管理信息
     *
     * @param id 产品管理主键
     * @return 结果
     */
    public int deleteProductById(Long id);



	Product findOneByProductName(String productName);



	List<Product> selectByManufacturerIdAndModelAndDeviceType(String manufacturerId,String model,String deviceType);



	Product findOneByManufacturerIdAndModelAndDeviceType(String manufacturerId,String model,String deviceType);


    /**
     * 根据产品模型创建超级表
     * @param product
     * @param services
     * @return
     * @throws Exception
     */
    AjaxResult createSuperTable(Product product, JSONArray services) throws Exception;



	List<Product> findAllByStatus(String status);

    /**
     * 初始化生成超级表模型
     * @param productIds 产品ID集合  productIds==null 初始化所有产品:productIds!=null 初始化指定产品
     * @param InitializeOrNot  是否初始化
     * @return
     * @throws Exception
     */
    List<SuperTableDto> createSuperTableDataModel(Long[] productIds,Boolean InitializeOrNot);



	Product findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(String manufacturerId,String model,String protocolType,String status);



	Product findOneByIdAndStatus(Long id,String status);



	Product findOneByProductIdentificationAndProtocolType(String productIdentification,String protocolType);



	List<Product> findAllByIdInAndStatus(Collection<Long> idCollection,String status);
}
