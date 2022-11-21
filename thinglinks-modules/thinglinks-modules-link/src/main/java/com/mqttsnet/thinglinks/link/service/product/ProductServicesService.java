package com.mqttsnet.thinglinks.link.service.product;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;

import java.util.List;

/**
 * @Description: 产品服务
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/25$ 23:52$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/25$ 23:52$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public interface ProductServicesService {


    int deleteByPrimaryKey(Long id);

    int insert(ProductServices record);

    int insertOrUpdate(ProductServices record);

    int insertOrUpdateSelective(ProductServices record);

    int insertSelective(ProductServices record);

    ProductServices selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductServices record);

    int updateByPrimaryKey(ProductServices record);

    int updateBatch(List<ProductServices> list);

    int updateBatchSelective(List<ProductServices> list);

    int batchInsert(List<ProductServices> list);

    List<ProductServices> findByProductIdentifications(List<String> productIdentifications);


    List<ProductServices> findAllByProductIdentificationIdAndStatus(String productIdentification, String status);


    List<ProductServices> findAllByProductIdentificationAndServiceNameAndStatus(String productIdentification, String serviceName, String status);


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
     * @param ids 需要删除的产品模型服务主键集合
     * @return 结果
     */
    int deleteProductServicesByIds(Long[] ids);


    /**
     * 根据产品标识和状态获取产品所有服务
     * @param productIdentification
     * @param status
     * @return
     */
    List<ProductServices> selectAllByProductIdentificationAndStatus(String productIdentification, String status);
}

