package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.link.mapper.product
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-18 20:37
 **/
@Mapper
public interface ProductMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(Product record);

    int insertOrUpdate(Product record);

    int insertOrUpdateSelective(Product record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(Product record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    Product selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Product record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Product record);

    int updateBatch(List<Product> list);

    int updateBatchSelective(List<Product> list);

    int batchInsert(@Param("list") List<Product> list);

    /**
     * 查询产品管理
     *
     * @param id 产品管理主键
     * @return 产品管理
     */
    Product selectProductById(Long id);

    /**
     * 查询产品管理
     *
     * @param productIdentification 产品标识
     * @return 产品管理
     */
    Product selectByProductIdentification(String productIdentification);

    /**
     * 查询产品管理列表
     *
     * @param product 产品管理
     * @return 产品管理集合
     */
    List<Product> selectProductList(Product product);

    /**
     * 新增产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    int insertProduct(Product product);

    /**
     * 修改产品管理
     *
     * @param product 产品管理
     * @return 结果
     */
    int updateProduct(Product product);

    /**
     * 删除产品管理
     *
     * @param id 产品管理主键
     * @return 结果
     */
    int deleteProductById(Long id);

    /**
     * 批量删除产品管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProductByIds(Long[] ids);

    Product findOneByProductName(@Param("productName") String productName);

    List<Product> selectByManufacturerIdAndModelAndDeviceType(@Param("manufacturerId") String manufacturerId, @Param("model") String model, @Param("deviceType") String deviceType);

    Product findOneByManufacturerIdAndModelAndDeviceType(@Param("manufacturerId") String manufacturerId, @Param("model") String model, @Param("deviceType") String deviceType);

    List<Product> findAllByStatus(@Param("status") String status);

    Product findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(@Param("manufacturerId") String manufacturerId, @Param("model") String model, @Param("protocolType") String protocolType, @Param("status") String status);

    Product findOneByIdAndStatus(@Param("id") Long id, @Param("status") String status);

    Product findOneByProductIdentificationAndProtocolType(@Param("productIdentification") String productIdentification, @Param("protocolType") String protocolType);

    List<Product> findAllByIdInAndStatus(@Param("idCollection") Collection<Long> idCollection, @Param("status") String status);

    List<Product> selectAllProductByStatus(@Param("status") String status);
    List<Product> selectProductByIdentificationList(List<String> identificationList);
}