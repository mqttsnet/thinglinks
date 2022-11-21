package com.mqttsnet.thinglinks.link.service.product.impl;

import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.mapper.product.ProductServicesMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
@Service
public class ProductServicesServiceImpl implements ProductServicesService {

    @Resource
    private ProductServicesMapper productServicesMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productServicesMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductServices record) {
        return productServicesMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductServices record) {
        return productServicesMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductServices record) {
        return productServicesMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductServices record) {
        return productServicesMapper.insertSelective(record);
    }

    @Override
    public ProductServices selectByPrimaryKey(Long id) {
        return productServicesMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductServices record) {
        return productServicesMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductServices record) {
        return productServicesMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductServices> list) {
        return productServicesMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductServices> list) {
        return productServicesMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductServices> list) {
        return productServicesMapper.batchInsert(list);
    }

    @Override
    public List<ProductServices> findByProductIdentifications(List<String> productIds) {
        return productServicesMapper.findByProductIdentifications(productIds);
    }

    @Override
    public List<ProductServices> findAllByProductIdentificationIdAndStatus(String productIdentification, String status) {
        return productServicesMapper.findAllByProductIdentificationIdAndStatus(productIdentification, status);
    }

    @Override
    public List<ProductServices> findAllByProductIdentificationAndServiceNameAndStatus(String productIdentification, String serviceName, String status) {
        return productServicesMapper.findAllByProductIdentificationAndServiceNameAndStatus(productIdentification, serviceName, status);
    }

    /**
     * 查询产品模型服务
     *
     * @param id 产品模型服务主键
     * @return 产品模型服务
     */
    @Override
    public ProductServices selectProductServicesById(Long id) {
        return productServicesMapper.selectProductServicesById(id);
    }

    /**
     * 查询产品模型服务列表
     *
     * @param productServices 产品模型服务
     * @return 产品模型服务
     */
    @Override
    public List<ProductServices> selectProductServicesList(ProductServices productServices) {
        return productServicesMapper.selectProductServicesList(productServices);
    }

    /**
     * 新增产品模型服务
     *
     * @param productServices 产品模型服务
     * @return 结果
     */
    @Override
    public int insertProductServices(ProductServices productServices) {
        return productServicesMapper.insertProductServices(productServices);
    }

    /**
     * 修改产品模型服务
     *
     * @param productServices 产品模型服务
     * @return 结果
     */
    @Override
    public int updateProductServices(ProductServices productServices) {
        return productServicesMapper.updateProductServices(productServices);
    }

    /**
     * 批量删除产品模型服务
     *
     * @param ids 需要删除的产品模型服务主键
     * @return 结果
     */
    @Override
    public int deleteProductServicesByIds(Long[] ids) {
        return productServicesMapper.deleteProductServicesByIds(ids);
    }


    /**
     * 根据产品标识和状态获取产品所有服务
     *
     * @param productIdentification
     * @param status
     * @return
     */
    @Override
    public List<ProductServices> selectAllByProductIdentificationAndStatus(String productIdentification, String status) {
        return productServicesMapper.selectAllByProductIdentificationAndStatus(productIdentification, status);
    }
}

