package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.product.ProductCommandsRequestsMapper;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductCommandsRequests;
import com.mqttsnet.thinglinks.link.service.product.ProductCommandsRequestsService;
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
public class ProductCommandsRequestsServiceImpl implements ProductCommandsRequestsService{

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

}
