package com.mqttsnet.thinglinks.tds.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.thinglinks.tds.api.TdsApi;
import com.mqttsnet.thinglinks.tds.facade.TdsFacade;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 * @author tangyh
 * @since 2024/12/24 21:18
 */
@Service
@Slf4j
public class TdsFacadeImpl implements TdsFacade {
    @Lazy
    @Autowired
    private TdsApi tdsApi;

    @Override
    public R createDatabase(String dataBaseName) {
        return tdsApi.createDatabase(dataBaseName);
    }

    @Override
    public R createSuperTable(String superTableName) {
        return tdsApi.createSuperTable(superTableName);
    }

    @Override
    public R createSuperTableAndColumn(SuperTableDTO superTableDTO) {
        return tdsApi.createSuperTableAndColumn(superTableDTO);
    }

    @Override
    public R batchCreateSuperTable(Map<String, Object> schema) {
        return tdsApi.batchCreateSuperTable(schema);
    }

    @Override
    public R createSubTable(TableDTO tableDTO) {
        return tdsApi.createSubTable(tableDTO);
    }

    @Override
    public R batchCreateSubTable(Map<String, Object> schema) {
        return tdsApi.batchCreateSubTable(schema);
    }

    @Override
    public R dropSuperTable(String superTableName) {
        return tdsApi.dropSuperTable(superTableName);
    }

    @Override
    public R alterSuperTableColumn(SuperTableDTO superTableDTO) {
        return tdsApi.alterSuperTableColumn(superTableDTO);
    }

    @Override
    public R dropSuperTableColumn(SuperTableDTO superTableDTO) {
        return tdsApi.dropSuperTableColumn(superTableDTO);
    }

    @Override
    public R alterSuperTableTag(SuperTableDTO superTableDTO) {
        return tdsApi.alterSuperTableTag(superTableDTO);
    }

    @Override
    public R dropSuperTableTag(SuperTableDTO superTableDTO) {
        return tdsApi.dropSuperTableTag(superTableDTO);
    }

    @Override
    public R alterSuperTableTagRename(String superTableName, String oldName, String newName) {
        return tdsApi.alterSuperTableTagRename(superTableName, oldName, newName);
    }

    @Override
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(String tableName) {
        return tdsApi.describeSuperOrSubTable(tableName);
    }

    @Override
    public R insertTableData(TableDTO tableDTO) {
        return tdsApi.insertTableData(tableDTO);
    }

    @Override
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(String tableName, Long startTime, Long endTime) {
        return tdsApi.getDataInRangeOrLastRecord(tableName, startTime, endTime);
    }
}
