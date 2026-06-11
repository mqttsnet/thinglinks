package com.mqttsnet.thinglinks.tds.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;

import java.util.List;
import java.util.Map;

/**
 * 时序数据库远程接口
 *
 * @author xiaonannet
 */
public interface TdsFacade {

    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    R createDatabase(String dataBaseName);

    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    R createSuperTable(String superTableName);

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    R createSuperTableAndColumn(SuperTableDTO superTableDTO);

    /**
     * 批量创建超级表及字段(单次请求可建多张)。入参用 {@link Map} 而非具体 JSON 类型,使 Feign 契约层保持中性、不绑定任何第三方 JSON 框架。
     *
     * @param schema 超级表批量定义,key=超级表名,value=超级表字段/tags 定义
     * @return 执行结果
     */
    R batchCreateSuperTable(Map<String, Object> schema);

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    R createSubTable(TableDTO tableDTO);

    /**
     * 批量创建子表(单次请求可建多张)。入参 schema 结构同 batchCreateSuperTable,key=子表名。
     *
     * @param schema 子表批量定义
     * @return 执行结果
     */
    R batchCreateSubTable(Map<String, Object> schema);

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    R dropSuperTable(String superTableName);

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    R alterSuperTableColumn(SuperTableDTO superTableDTO);

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    R dropSuperTableColumn(SuperTableDTO superTableDTO);

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    R alterSuperTableTag(SuperTableDTO superTableDTO);

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    R dropSuperTableTag(SuperTableDTO superTableDTO);

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    R alterSuperTableTagRename(
            String superTableName,
            String oldName, String newName);


    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    R<List<SuperTableDescribeVO>> describeSuperOrSubTable(String tableName);

    /**
     * 新增数据
     *
     * @param tableDTO 数据信息
     * @return 执行结果
     */
    R insertTableData(TableDTO tableDTO);


    /**
     * Retrieves the latest data from a regular table. It fetches the most recent record
     * within the specified time range or the last recorded data if the time range is not specified.
     *
     * @param tableName The name of the table.
     * @param startTime The start time for the query range (optional).
     * @param endTime   The end time for the query range (optional).
     * @return {@link R<List<<Map<String,Object>>>} The query result.
     */
    R<List<Map<String, Object>>> getDataInRangeOrLastRecord(
            String tableName,
            Long startTime,
            Long endTime);


}
