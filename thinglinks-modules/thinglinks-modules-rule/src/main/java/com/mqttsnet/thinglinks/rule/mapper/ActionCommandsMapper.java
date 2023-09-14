package com.mqttsnet.thinglinks.rule.mapper;

import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.mapper
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-12-04 21:39
**/
@Mapper
public interface ActionCommandsMapper {
    /**
     * delete by primary key
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * insert record to table
     * @param record the record
     * @return insert count
     */
    int insert(ActionCommands record);

    int insertOrUpdate(ActionCommands record);

    int insertOrUpdateSelective(ActionCommands record);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(ActionCommands record);

    /**
     * select by primary key
     * @param id primary key
     * @return object by primary key
     */
    ActionCommands selectByPrimaryKey(Integer id);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(ActionCommands record);

    /**
     * update record
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(ActionCommands record);

    int updateBatch(List<ActionCommands> list);

    int updateBatchSelective(List<ActionCommands> list);

    int batchInsert(@Param("list") List<ActionCommands> list);

    List<ActionCommands> selectByActionCommandsSelective(ActionCommands actionCommands);

    List<ActionCommands> actionCommandsByRuleIdentification(String ruleIdentification);

    int deleteBatchByIds(Long[] ids);
}