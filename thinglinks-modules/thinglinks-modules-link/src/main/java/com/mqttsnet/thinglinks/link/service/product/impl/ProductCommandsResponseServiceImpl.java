package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsResponse;
import com.mqttsnet.thinglinks.link.mapper.product.ProductCommandsResponseMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsResponseService;
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
public class ProductCommandsResponseServiceImpl implements ProductCommandsResponseService{

    @Resource
    private ProductCommandsResponseMapper productCommandsResponseMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productCommandsResponseMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductCommandsResponse record) {
        return productCommandsResponseMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductCommandsResponse record) {
        return productCommandsResponseMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductCommandsResponse record) {
        return productCommandsResponseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductCommandsResponse record) {
        return productCommandsResponseMapper.insertSelective(record);
    }

    @Override
    public ProductCommandsResponse selectByPrimaryKey(Long id) {
        return productCommandsResponseMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductCommandsResponse record) {
        return productCommandsResponseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductCommandsResponse record) {
        return productCommandsResponseMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductCommandsResponse> list) {
        return productCommandsResponseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductCommandsResponse> list) {
        return productCommandsResponseMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductCommandsResponse> list) {
        return productCommandsResponseMapper.batchInsert(list);
    }

}
