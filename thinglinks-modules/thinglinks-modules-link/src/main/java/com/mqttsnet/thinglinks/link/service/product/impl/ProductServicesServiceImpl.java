package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.product.ProductServicesMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
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
public class ProductServicesServiceImpl implements ProductServicesService{

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

}
