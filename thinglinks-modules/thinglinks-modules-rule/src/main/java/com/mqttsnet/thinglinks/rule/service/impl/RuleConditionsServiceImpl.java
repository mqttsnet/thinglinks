package com.mqttsnet.thinglinks.rule.service.impl;

import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;
import com.mqttsnet.thinglinks.rule.mapper.RuleConditionsMapper;
import com.mqttsnet.thinglinks.rule.service.RuleConditionsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.service.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:47
 **/
@Service
public class RuleConditionsServiceImpl implements RuleConditionsService {

    @Resource
    private RuleConditionsMapper ruleConditionsMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return ruleConditionsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(RuleConditions record) {
        return ruleConditionsMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(RuleConditions record) {
        return ruleConditionsMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RuleConditions record) {
        return ruleConditionsMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(RuleConditions record) {
        return ruleConditionsMapper.insertSelective(record);
    }

    @Override
    public RuleConditions selectByPrimaryKey(Long id) {
        return ruleConditionsMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<RuleConditions> selectByRuleId(Long ruleId) {
        return ruleConditionsMapper.selectByRuleId(ruleId);
    }

    @Override
    public int updateByPrimaryKeySelective(RuleConditions record) {
        return ruleConditionsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(RuleConditions record) {
        return ruleConditionsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<RuleConditions> list) {
        return ruleConditionsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<RuleConditions> list) {
        return ruleConditionsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<RuleConditions> list) {
        return ruleConditionsMapper.batchInsert(list);
    }

    public int deleteBatchByIds(Long[] ids) {
        return ruleConditionsMapper.deleteBatchByIds(ids);
    }
}

