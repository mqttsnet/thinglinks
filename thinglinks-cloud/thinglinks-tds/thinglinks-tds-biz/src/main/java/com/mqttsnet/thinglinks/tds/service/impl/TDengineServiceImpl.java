package com.mqttsnet.thinglinks.tds.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.tds.model.Fields;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.model.TagsSelectDTO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.tds.mapper.TDengineMapper;
import com.mqttsnet.thinglinks.tds.service.TdsService;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@DS(DsConstant.TDS_DEFAULTS)
public class TDengineServiceImpl implements TdsService {

    @Autowired
    private TDengineMapper tdengineMapper;

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void createDatabase(String dataBaseName) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        tdengineMapper.createDatabase(dataBaseName);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void createSuperTable(String dataBaseName, String superTableName) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        tdengineMapper.createSuperTable(dataBaseName, superTableName);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void createSuperTableAndColumn(SuperTableDTO superTableDTO) {
        TdsSqlGuard.checkSuperTable(superTableDTO);
        tdengineMapper.createSuperTableAndColumn(superTableDTO);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void createSubTable(TableDTO tableDTO) {
        TdsSqlGuard.checkTable(tableDTO);
        tdengineMapper.createSubTable(tableDTO);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void dropSuperTable(String dataBaseName, String superTableName) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        tdengineMapper.dropSuperTable(dataBaseName, superTableName);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void alterSuperTableColumn(String dataBaseName, String superTableName, Fields fields) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        TdsSqlGuard.checkField(fields, "fields");
        tdengineMapper.alterSuperTableColumn(dataBaseName, superTableName, fields);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void dropSuperTableColumn(String dataBaseName, String superTableName, Fields fields) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        TdsSqlGuard.checkField(fields, "fields");
        tdengineMapper.dropSuperTableColumn(dataBaseName, superTableName, fields);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public List<SuperTableDescribeVO> describeSuperOrSubTable(String dataBaseName, String tableName) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(tableName, "tableName");
        List<SuperTableDescribeVO> superTableDescribeVOS = new ArrayList<>();
        try {
            superTableDescribeVOS = tdengineMapper.describeSuperOrSubTable(dataBaseName, tableName);
        } catch (Exception e) {
            log.warn("describeSuperTable error:{}", e.getMessage());
        }
        return superTableDescribeVOS;
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void alterSuperTableTag(String dataBaseName, String superTableName, Fields fields) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        TdsSqlGuard.checkField(fields, "fields");
        tdengineMapper.alterSuperTableTag(dataBaseName, superTableName, fields);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void dropSuperTableTag(String dataBaseName, String superTableName, Fields fields) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        TdsSqlGuard.checkField(fields, "fields");
        tdengineMapper.dropSuperTableTag(dataBaseName, superTableName, fields);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void alterSuperTableTagRename(String dataBaseName, String superTableName, String oldName, String newName) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(superTableName, "superTableName");
        TdsSqlGuard.checkIdentifier(oldName, "oldName");
        TdsSqlGuard.checkIdentifier(newName, "newName");
        tdengineMapper.alterSuperTableTagRename(dataBaseName, superTableName, oldName, newName);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public void insertTableData(TableDTO tableDTO) {
        TdsSqlGuard.checkTable(tableDTO);
        tdengineMapper.insertTableData(tableDTO);
    }

    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public CompletableFuture<Map<String, Map<String, Object>>> getLastDataByTags(TagsSelectDTO tagsSelectDTO) {
        List<Map<String, Object>> maps = tdengineMapper.getLastDataByTags(tagsSelectDTO);
        Map<String, Map<String, Object>> objectHashMap = new HashMap<>();

        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(tagsSelectDTO.getTagsName()))
                    .map(Object::toString)
                    .ifPresent(key -> objectHashMap.put(key, map));
        }
        return CompletableFuture.completedFuture(objectHashMap);
    }


    @Override
    @DS(DsConstant.EXTEND_TENANT)
    public CompletableFuture<List<Map<String, Object>>> getDataInRangeOrLastRecord(String dataBaseName, String tableName, Long startTime, Long endTime) {
        TdsSqlGuard.checkIdentifier(dataBaseName, "dataBaseName");
        TdsSqlGuard.checkIdentifier(tableName, "tableName");
        return CompletableFuture.completedFuture(tdengineMapper.getDataInRangeOrLastRecord(dataBaseName, tableName, startTime, endTime));
    }
}
