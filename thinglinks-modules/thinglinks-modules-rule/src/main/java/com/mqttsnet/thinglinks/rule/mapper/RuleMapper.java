package com.mqttsnet.thinglinks.rule.mapper;

import com.mqttsnet.thinglinks.rule.api.domain.Rule;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.mapper
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:49
 **/
@Mapper
public interface RuleMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 根据规则标识查询规则信息
     *
     * @param ruleIdentification
     * @return
     */
    Rule selectByRuleIdentification(String ruleIdentification);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(Rule record);

    int insertOrUpdate(Rule record);

    int insertOrUpdateSelective(Rule record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(Rule record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    Rule selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Rule record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(Rule record);

    int updateBatch(List<Rule> list);

    int updateBatchSelective(List<Rule> list);

    int batchInsert(@Param("list") List<Rule> list);
}
