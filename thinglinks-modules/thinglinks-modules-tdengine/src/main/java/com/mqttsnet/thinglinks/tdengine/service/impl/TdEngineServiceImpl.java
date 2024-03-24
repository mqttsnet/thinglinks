package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TagsSelectDTO;
import com.mqttsnet.thinglinks.tdengine.mapper.TdEngineMapper;
import com.mqttsnet.thinglinks.tdengine.service.TdEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassDescription: TdEngine业务层的实现层
 * @ClassName: TdEngineServiceImpl
 * @Author: thinglinks
 * @Date: 2021-12-27 13:55:49
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class TdEngineServiceImpl implements TdEngineService {

    @Autowired
    private TdEngineMapper tdengineMapper;

    @Override
    public void createDatabase(String dataBaseName) {
        tdengineMapper.createDatabase(dataBaseName);
    }

    @Override
    public void createSuperTable(String dataBaseName, String superTableName) {
        tdengineMapper.createSuperTable(dataBaseName, superTableName);
    }

    @Override
    public void createSuperTableAndColumn(SuperTableDTO superTableDTO) {
        tdengineMapper.createSuperTableAndColumn(superTableDTO);
    }

    @Override
    public void createSubTable(TableDTO tableDTO) {
        tdengineMapper.createSubTable(tableDTO);
    }

    @Override
    public void dropSuperTable(String dataBaseName, String superTableName) {
        tdengineMapper.dropSuperTable(dataBaseName, superTableName);
    }

    @Override
    public void alterSuperTableColumn(String dataBaseName, String superTableName, Fields fields) {
        tdengineMapper.alterSuperTableColumn(dataBaseName, superTableName, fields);
    }

    @Override
    public void dropSuperTableColumn(String dataBaseName, String superTableName, Fields fields) {
        tdengineMapper.dropSuperTableColumn(dataBaseName, superTableName, fields);
    }

    @Override
    public List<SuperTableDescribeVO> describeSuperOrSubTable(String dataBaseName, String tableName) {
        try {
            return tdengineMapper.describeSuperOrSubTable(dataBaseName, tableName);
        } catch (Exception e) {
            log.warn("Error describing super or sub table. Database: {}, Table: {}, Error: {}", dataBaseName, tableName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void alterSuperTableTag(String dataBaseName, String superTableName, Fields fields) {
        tdengineMapper.alterSuperTableTag(dataBaseName, superTableName, fields);
    }

    @Override
    public void dropSuperTableTag(String dataBaseName, String superTableName, Fields fields) {
        tdengineMapper.dropSuperTableTag(dataBaseName, superTableName, fields);
    }

    @Override
    public void alterSuperTableTagRename(String dataBaseName, String superTableName, String oldName, String newName) {
        tdengineMapper.alterSuperTableTagRename(dataBaseName, superTableName, oldName, newName);
    }

    @Override
    public void insertTableData(TableDTO tableDTO) {
        tdengineMapper.insertTableData(tableDTO);
    }

    @Override
    public Map<String, Map<String, Object>> getLastDataByTags(TagsSelectDTO tagsSelectDTO) {
        List<Map<String, Object>> maps = tdengineMapper.getLastDataByTags(tagsSelectDTO);
        Map<String, Map<String, Object>> objectHashMap = new HashMap<>();

        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(tagsSelectDTO.getTagsName()))
                    .map(Object::toString)
                    .ifPresent(key -> objectHashMap.put(key, map));
        }
        return objectHashMap;
    }


    @Override
    public List<Map<String, Object>> getDataInRangeOrLastRecord(String dataBaseName, String tableName, Long startTime, Long endTime) {
        return tdengineMapper.getDataInRangeOrLastRecord(dataBaseName, tableName, startTime, endTime);
    }


    @Override
    public List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) throws Exception {
        List<Map<String, Object>> maps = tdengineMapper.selectByTimestamp(selectDto);
        for (Map<String, Object> map : maps) {
            Map<String, Object> filterMap = map.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return maps;
    }


    /**
     * @param selectDto
     * @return
     */
    @Override
    public List<Map<String, Object>> getLastData(SelectDto selectDto) throws Exception {
        List<Map<String, Object>> maps = this.tdengineMapper.getLastData(selectDto);
//        for (Map<String, Object> map : maps) {
//            Map<String, Object> filterMap = map.entrySet()
//                    .stream()
//                    .filter(entry -> entry.getValue() != null)
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        }
        return maps;
    }


}
