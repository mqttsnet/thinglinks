package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.FieldsVo;
import com.mqttsnet.thinglinks.tdengine.mapper.TdEngineMapper;
import com.mqttsnet.thinglinks.tdengine.service.TdEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassDescription: TdEngine业务层的实现层
 * @ClassName: TdEngineServiceImpl
 * @Author: thinglinks
 * @Date: 2021-12-27 13:55:49
 * @Version 1.0
 */
@Service
public class TdEngineServiceImpl implements TdEngineService {

    @Autowired
    private TdEngineMapper tdEngineMapper;

    @Override
    public void createDateBase(String databaseName) {
        this.tdEngineMapper.createDatabase(databaseName);
    }

    @Override
    public void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String databaseName, String superTableName) {
        this.tdEngineMapper.createSuperTable(schemaFields, tagsFields, databaseName, superTableName);
    }

    @Override
    public void createTable(TableDto tableDto) {
        this.tdEngineMapper.createTable(tableDto);
    }

    @Override
    public void insertData(TableDto tableDto) {
        this.tdEngineMapper.insertData(tableDto);
    }

    @Override
    public List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) {
        List<Map<String, Object>> maps = this.tdEngineMapper.selectByTimestamp(selectDto);
        for (Map<String, Object> map : maps) {
            Map<String, Object> filterMap = map.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return maps;
    }

    @Override
    public void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo) {
        this.tdEngineMapper.addColumnForSuperTable(superTableName, fieldsVo);
    }
}
