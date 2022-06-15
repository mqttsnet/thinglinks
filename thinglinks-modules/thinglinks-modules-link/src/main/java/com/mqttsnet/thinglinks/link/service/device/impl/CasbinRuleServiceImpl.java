package com.mqttsnet.thinglinks.link.service.device.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.device.CasbinRuleMapper;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import com.mqttsnet.thinglinks.link.service.device.CasbinRuleService;
/**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:22$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:22$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
@Service
public class CasbinRuleServiceImpl implements CasbinRuleService{

    @Resource
    private CasbinRuleMapper casbinRuleMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return casbinRuleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CasbinRule record) {
        return casbinRuleMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(CasbinRule record) {
        return casbinRuleMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(CasbinRule record) {
        return casbinRuleMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(CasbinRule record) {
        return casbinRuleMapper.insertSelective(record);
    }

    @Override
    public CasbinRule selectByPrimaryKey(Integer id) {
        return casbinRuleMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CasbinRule record) {
        return casbinRuleMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CasbinRule record) {
        return casbinRuleMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<CasbinRule> list) {
        return casbinRuleMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<CasbinRule> list) {
        return casbinRuleMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<CasbinRule> list) {
        return casbinRuleMapper.batchInsert(list);
    }

}
