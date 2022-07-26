package com.mqttsnet.thinglinks.rule.service;

import java.util.List;

import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.service
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:47
 **/
public interface RuleConditionsService {


    int deleteByPrimaryKey(Long id);

    int insert(RuleConditions record);

    int insertOrUpdate(RuleConditions record);

    int insertOrUpdateSelective(RuleConditions record);

    int insertSelective(RuleConditions record);

    RuleConditions selectByPrimaryKey(Long id);

    List<RuleConditions> selectByRuleId(Long ruleId);

    int updateByPrimaryKeySelective(RuleConditions record);

    int updateByPrimaryKey(RuleConditions record);

    int updateBatch(List<RuleConditions> list);

    int updateBatchSelective(List<RuleConditions> list);

    int batchInsert(List<RuleConditions> list);

}

