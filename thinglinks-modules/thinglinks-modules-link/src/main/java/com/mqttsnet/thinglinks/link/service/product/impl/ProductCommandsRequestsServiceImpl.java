package com.mqttsnet.thinglinks.link.service.product.impl;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import com.mqttsnet.thinglinks.link.mapper.product.ProductCommandsRequestsMapper;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsRequestsService;

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
public class ProductCommandsRequestsServiceImpl implements ProductCommandsRequestsService {

    @Resource
    private ProductCommandsRequestsMapper productCommandsRequestsMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productCommandsRequestsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.insertSelective(record);
    }

    @Override
    public ProductCommandsRequests selectByPrimaryKey(Long id) {
        return productCommandsRequestsMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductCommandsRequests record) {
        return productCommandsRequestsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductCommandsRequests> list) {
        return productCommandsRequestsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductCommandsRequests> list) {
        return productCommandsRequestsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductCommandsRequests> list) {
        return productCommandsRequestsMapper.batchInsert(list);
    }

    /**
     * 查询产品模型设备下发服务命令属性
     *
     * @param id 产品模型设备下发服务命令属性主键
     * @return 产品模型设备下发服务命令属性
     */
    @Override
    public ProductCommandsRequests selectProductCommandsRequestsById(Long id) {
        return productCommandsRequestsMapper.selectProductCommandsRequestsById(id);
    }

    /**
     * 查询产品模型设备下发服务命令属性列表
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 产品模型设备下发服务命令属性
     */
    @Override
    public List<ProductCommandsRequests> selectProductCommandsRequestsList(ProductCommandsRequests productCommandsRequests) {
        return productCommandsRequestsMapper.selectProductCommandsRequestsList(productCommandsRequests);
    }

    /**
     * 新增产品模型设备下发服务命令属性
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 结果
     */
    @Override
    public int insertProductCommandsRequests(ProductCommandsRequests productCommandsRequests) {
        productCommandsRequests.setCreateTime(DateUtils.getNowDate());
        return productCommandsRequestsMapper.insertProductCommandsRequests(productCommandsRequests);
    }

    /**
     * 修改产品模型设备下发服务命令属性
     *
     * @param productCommandsRequests 产品模型设备下发服务命令属性
     * @return 结果
     */
    @Override
    public int updateProductCommandsRequests(ProductCommandsRequests productCommandsRequests) {
        productCommandsRequests.setUpdateTime(DateUtils.getNowDate());
        return productCommandsRequestsMapper.updateProductCommandsRequests(productCommandsRequests);
    }

    /**
     * 批量删除产品模型设备下发服务命令属性
     *
     * @param ids 需要删除的产品模型设备下发服务命令属性主键
     * @return 结果
     */
    @Override
    public int deleteProductCommandsRequestsByIds(Long[] ids) {
        return productCommandsRequestsMapper.deleteProductCommandsRequestsByIds(ids);
    }

}
