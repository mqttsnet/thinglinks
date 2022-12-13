package com.mqttsnet.thinglinks.rule.service;

import java.util.List;
import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;
import com.mqttsnet.thinglinks.rule.api.domain.model.ActionCommandsModel;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.service.impl
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-12-04 21:39
**/
public interface ActionCommandsService{


    int deleteByPrimaryKey(Integer id);

    int insert(ActionCommands record);

    int insertOrUpdate(ActionCommands record);

    int insertOrUpdateSelective(ActionCommands record);

    int insertSelective(ActionCommands record);

    ActionCommands selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ActionCommands record);

    int updateByPrimaryKey(ActionCommands record);

    int updateBatch(List<ActionCommands> list);

    int updateBatchSelective(List<ActionCommands> list);

    int batchInsert(List<ActionCommands> list);


    List<ActionCommands>   selectByActionCommandsSelective(ActionCommands actionCommands);

    int deleteBatchByIds(Long[] ids);

    List<ActionCommandsModel> actionCommandsToActionCommandsModelList(List<ActionCommands> actionCommandsList);
}
