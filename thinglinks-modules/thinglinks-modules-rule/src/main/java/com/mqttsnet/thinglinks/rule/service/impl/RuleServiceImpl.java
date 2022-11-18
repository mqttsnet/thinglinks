package com.mqttsnet.thinglinks.rule.service.impl;

import com.mqttsnet.thinglinks.rule.api.domain.Rule;
import com.mqttsnet.thinglinks.rule.mapper.RuleMapper;
import com.mqttsnet.thinglinks.rule.service.RuleService;
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
public class RuleServiceImpl implements RuleService {

    @Resource
    private RuleMapper ruleMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return ruleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Rule selectByRuleIdentification(String ruleIdentification) {
        return ruleMapper.selectByRuleIdentification(ruleIdentification);
    }

    @Override
    public int insert(Rule record) {
        return ruleMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Rule record) {
        return ruleMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Rule record) {
        return ruleMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Rule record) {
        return ruleMapper.insertSelective(record);
    }

    @Override
    public Rule selectByPrimaryKey(Long id) {
        return ruleMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Rule record) {
        return ruleMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Rule record) {
        return ruleMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Rule> list) {
        return ruleMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Rule> list) {
        return ruleMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Rule> list) {
        return ruleMapper.batchInsert(list);
    }

}


