package com.mqttsnet.thinglinks.rule.service.impl;

import com.mqttsnet.thinglinks.rule.service.ActionCommandsService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mqttsnet.thinglinks.rule.mapper.ActionCommandsMapper;
import java.util.List;
import com.mqttsnet.thinglinks.rule.api.domain.ActionCommands;

/**
* @program: thinglinks
* @description: ${description}
* @packagename: com.mqttsnet.thinglinks.rule.service
* @author: ShiHuan Sun
* @e-mainl: 13733918655@163.com
* @date: 2022-12-04 21:39
**/
@Service
public class ActionCommandsServiceImpl implements ActionCommandsService {

    @Resource
    private ActionCommandsMapper actionCommandsMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return actionCommandsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ActionCommands record) {
        return actionCommandsMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(ActionCommands record) {
        return actionCommandsMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(ActionCommands record) {
        return actionCommandsMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(ActionCommands record) {
        return actionCommandsMapper.insertSelective(record);
    }

    @Override
    public ActionCommands selectByPrimaryKey(Integer id) {
        return actionCommandsMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ActionCommands record) {
        return actionCommandsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ActionCommands record) {
        return actionCommandsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<ActionCommands> list) {
        return actionCommandsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<ActionCommands> list) {
        return actionCommandsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<ActionCommands> list) {
        return actionCommandsMapper.batchInsert(list);
    }

}
