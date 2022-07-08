package com.mqttsnet.thinglinks.link.mapper.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/25$ 23:52$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/25$ 23:52$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Mapper
public interface ProductServicesMapper {
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
    int insert(ProductServices record);

    int insertOrUpdate(ProductServices record);

    int insertOrUpdateSelective(ProductServices record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(ProductServices record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    ProductServices selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(ProductServices record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(ProductServices record);

    int updateBatch(List<ProductServices> list);

    int updateBatchSelective(List<ProductServices> list);

    int batchInsert(@Param("list") List<ProductServices> list);

    List<ProductServices> findByProductIds(@Param("productIds") List<Long> productIds);

    List<ProductServices> findAllByProductIdAndStatus(@Param("productId") Long productId, @Param("status") String status);

    List<ProductServices> findAllByProductIdAndServiceNameAndStatus(@Param("productId") Long productId, @Param("serviceName") String serviceName, @Param("status") String status);


    /**
     * 查询产品模型服务
     *
     * @param id 产品模型服务主键
     * @return 产品模型服务
     */
    ProductServices selectProductServicesById(Long id);

    /**
     * 查询产品模型服务列表
     *
     * @param productServices 产品模型服务
     * @return 产品模型服务集合
     */
    List<ProductServices> selectProductServicesList(ProductServices productServices);

    /**
     * 新增产品模型服务
     *
     * @param productServices 产品模型服务
     * @return 结果
     */
    int insertProductServices(ProductServices productServices);

    /**
     * 修改产品模型服务
     *
     * @param productServices 产品模型服务
     * @return 结果
     */
    int updateProductServices(ProductServices productServices);

    /**
     * 批量删除产品模型服务
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProductServicesByIds(Long[] ids);
}
