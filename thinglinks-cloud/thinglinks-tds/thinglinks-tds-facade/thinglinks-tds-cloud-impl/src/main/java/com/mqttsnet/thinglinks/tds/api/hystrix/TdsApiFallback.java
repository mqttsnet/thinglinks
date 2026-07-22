package com.mqttsnet.thinglinks.tds.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.thinglinks.tds.api.TdsApi;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @program: thinglinks-cloud
 * @description: Tds API熔断
 * @packagename: com.mqttsnet.thinglinks.tds.api.hystrix
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-09-22 12:37
 **/
@Component
public class TdsApiFallback implements TdsApi {


    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    @Override
    public R createDatabase(String dataBaseName) {
        return R.timeout();
    }

    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    @Override
    public R createSuperTable(String superTableName) {
        return R.timeout();
    }

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    @Override
    public R createSuperTableAndColumn(SuperTableDTO superTableDTO) {
        return R.timeout();
    }

    /**
     * 批量创建超级表及字段
     *
     * @param schema 超级表批量定义
     * @return 执行结果
     */
    @Override
    public R batchCreateSuperTable(Map<String, Object> schema) {
        return R.timeout();
    }

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    @Override
    public R createSubTable(TableDTO tableDTO) {
        return R.timeout();
    }

    /**
     * 批量创建子表
     *
     * @param schema 子表批量定义
     * @return 执行结果
     */
    @Override
    public R batchCreateSubTable(Map<String, Object> schema) {
        return R.timeout();
    }

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    @Override
    public R dropSuperTable(String superTableName) {
        return R.timeout();
    }

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Override
    public R alterSuperTableColumn(SuperTableDTO superTableDTO) {
        return R.timeout();
    }

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Override
    public R dropSuperTableColumn(SuperTableDTO superTableDTO) {
        return R.timeout();
    }

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Override
    public R alterSuperTableTag(SuperTableDTO superTableDTO) {
        return R.timeout();
    }

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Override
    public R dropSuperTableTag(SuperTableDTO superTableDTO) {
        return R.timeout();
    }

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    @Override
    public R alterSuperTableTagRename(String superTableName, String oldName, String newName) {
        return R.timeout();
    }

    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    @Override
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(String tableName) {
        return R.timeout();
    }

    /**
     * 新增数据
     *
     * @param tableDTO 数据信息
     * @return 执行结果
     */
    @Override
    public R insertTableData(TableDTO tableDTO) {
        return R.timeout();
    }

    /**
     * Retrieves the latest data from a regular table. It fetches the most recent record
     * within the specified time range or the last recorded data if the time range is not specified.
     *
     * @param tableName The name of the table.
     * @param startTime The start time for the query range (optional).
     * @param endTime   The end time for the query range (optional).
     * @return {@link R<List<Map<String,Object>>>} The query result.
     */
    @Override
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(String tableName, Long startTime, Long endTime) {
        return R.timeout();
    }

}
