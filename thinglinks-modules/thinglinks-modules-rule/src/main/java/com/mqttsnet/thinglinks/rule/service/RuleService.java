package com.mqttsnet.thinglinks.rule.service;

import com.mqttsnet.thinglinks.rule.api.domain.Rule;

import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.service
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:47
 **/
public interface RuleService {


    int deleteByPrimaryKey(Long id);

    int insert(Rule record);

    int insertOrUpdate(Rule record);

    int insertOrUpdateSelective(Rule record);

    int insertSelective(Rule record);

    Rule selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Rule record);

    int updateByPrimaryKey(Rule record);

    int updateBatch(List<Rule> list);

    int updateBatchSelective(List<Rule> list);

    int batchInsert(List<Rule> list);

}

