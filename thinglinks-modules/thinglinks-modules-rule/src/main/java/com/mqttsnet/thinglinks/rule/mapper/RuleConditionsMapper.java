package com.mqttsnet.thinglinks.rule.mapper;

import com.mqttsnet.thinglinks.rule.api.domain.RuleConditions;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.rule.mapper
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-21 18:50
 **/
@Mapper
public interface RuleConditionsMapper {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(RuleConditions record);

    int insertOrUpdate(RuleConditions record);

    int insertOrUpdateSelective(RuleConditions record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(RuleConditions record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    RuleConditions selectByPrimaryKey(Long id);

    /**
     * 根据规则id查询规则条件集合
     *
     * @param ruleId
     * @return
     */
    List<RuleConditions> selectByRuleId(Long ruleId);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(RuleConditions record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(RuleConditions record);

    int updateBatch(List<RuleConditions> list);

    int updateBatchSelective(List<RuleConditions> list);

    int batchInsert(@Param("list") List<RuleConditions> list);

    int deleteBatchByIds(Long[] ids);
}
