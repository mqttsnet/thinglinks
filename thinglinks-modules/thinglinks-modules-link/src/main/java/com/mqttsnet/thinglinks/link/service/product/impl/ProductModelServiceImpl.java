package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductModel;
import com.mqttsnet.thinglinks.link.mapper.product.ProductModelMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductModelService;
/**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/23$ 18:40$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/23$ 18:40$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
@Service
public class ProductModelServiceImpl implements ProductModelService{

    @Resource
    private ProductModelMapper productModelMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productModelMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductModel record) {
        return productModelMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductModel record) {
        return productModelMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductModel record) {
        return productModelMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductModel record) {
        return productModelMapper.insertSelective(record);
    }

    @Override
    public ProductModel selectByPrimaryKey(Long id) {
        return productModelMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductModel record) {
        return productModelMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductModel record) {
        return productModelMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductModel> list) {
        return productModelMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductModel> list) {
        return productModelMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductModel> list) {
        return productModelMapper.batchInsert(list);
    }

}
