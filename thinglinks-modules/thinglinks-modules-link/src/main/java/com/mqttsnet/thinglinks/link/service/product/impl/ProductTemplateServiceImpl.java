package com.mqttsnet.thinglinks.link.service.product.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductTemplate;
import com.mqttsnet.thinglinks.link.mapper.product.ProductTemplateMapper;
import com.mqttsnet.thinglinks.link.service.product.ProductTemplateService;
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
public class ProductTemplateServiceImpl implements ProductTemplateService{

    @Resource
    private ProductTemplateMapper productTemplateMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return productTemplateMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ProductTemplate record) {
        return productTemplateMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ProductTemplate record) {
        return productTemplateMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ProductTemplate record) {
        return productTemplateMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ProductTemplate record) {
        return productTemplateMapper.insertSelective(record);
    }

    @Override
    public ProductTemplate selectByPrimaryKey(Long id) {
        return productTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ProductTemplate record) {
        return productTemplateMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ProductTemplate record) {
        return productTemplateMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ProductTemplate> list) {
        return productTemplateMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ProductTemplate> list) {
        return productTemplateMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ProductTemplate> list) {
        return productTemplateMapper.batchInsert(list);
    }

}
