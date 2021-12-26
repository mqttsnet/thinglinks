package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.product.ProductPropertiesMapper;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductProperties;
import com.mqttsnet.thinglinks.link.service.product.ProductPropertiesService;
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
public class ProductPropertiesServiceImpl implements ProductPropertiesService{

    @Resource
    private ProductPropertiesMapper productPropertiesMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productPropertiesMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductProperties record) {
        return productPropertiesMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductProperties record) {
        return productPropertiesMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductProperties record) {
        return productPropertiesMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductProperties record) {
        return productPropertiesMapper.insertSelective(record);
    }

    @Override
    public ProductProperties selectByPrimaryKey(Long id) {
        return productPropertiesMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductProperties record) {
        return productPropertiesMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductProperties record) {
        return productPropertiesMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductProperties> list) {
        return productPropertiesMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductProperties> list) {
        return productPropertiesMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductProperties> list) {
        return productPropertiesMapper.batchInsert(list);
    }

}
