package com.mqttsnet.thinglinks.tdengine.mapper;

import com.mqttsnet.thinglinks.tdengine.api.domain.*;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TagsSelectDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.visual.SelectVisualDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassDescription:
 * @ClassName: TdEngineMapper
 * @Author: thinglinks
 * @Date: 2021-12-27 14:52:34
 * @Version 1.0
 */
@Mapper
public interface TdEngineMapper {

    void createDatabase(@Param("dataBaseName") String dataBaseName);

    void createSuperTable(@Param("schemaFields") List<FieldsVo> schemaFields,
                          @Param("tagsFields") List<FieldsVo> tagsFields,
                          @Param("dataBaseName") String dataBaseName,
                          @Param("superTableName") String superTableName);

    void createTable(TableDto tableDto);

    void insertData(TableDto tableDto);

    List<Map<String, Object>> selectByTimestamp(SelectDto selectDto);

    void addColumnForSuperTable(@Param("superTableName") String superTableName,
                                @Param("fieldsVo") FieldsVo fieldsVo);

    void dropColumnForSuperTable(@Param("superTableName") String superTableName,
                                @Param("fieldsVo") FieldsVo fieldsVo);

    void addTagForSuperTable(@Param("superTableName") String superTableName,
                                @Param("fieldsVo") FieldsVo fieldsVo);

    void dropTagForSuperTable(@Param("superTableName") String superTableName,
                                 @Param("fieldsVo") FieldsVo fieldsVo);

    Map<String, Long> getCountByTimestamp(SelectDto selectDto);

    /**
     * 检查表是否存在
     * @param dataBaseName
     * @param tableName 可以为超级表名或普通表名
     * @return
     */
    Integer checkTableExists(@Param("dataBaseName") String dataBaseName, @Param("tableName")String tableName);

    List<Map<String, Object>> getLastData(SelectDto selectDto);

    List<Map<String, Object>> getHistoryData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getRealtimeData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getAggregateData(SelectVisualDto selectVisualDto);
    
    List<Map<String, Object>> getLastDataByTags(TagsSelectDao tagsSelectDao);


    /**
     * 创建超级表
     *
     * @param dataBaseName
     * @param superTableName
     */
    void createSuperTable(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName);

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO
     */
    void createSuperTableAndColumn(SuperTableDTO superTableDTO);


    /**
     * 创建子表
     *
     * @param tableDTO
     */
    void createSubTable(TableDTO tableDTO);

    /**
     * 删除超级表
     *
     * @param dataBaseName
     * @param superTableName
     */
    void dropSuperTable(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName);

    /**
     * 新增字段
     *
     * @param dataBaseName
     * @param superTableName
     * @param fields
     */
    void alterSuperTableColumn(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName, @Param("fields") Fields fields);

    /**
     * 删除字段
     *
     * @param dataBaseName
     * @param superTableName
     * @param fields
     */
    void dropSuperTableColumn(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName, @Param("fields") Fields fields);

    /**
     * 查询表结构
     *
     * @param dataBaseName
     * @param tableName
     */
    List<SuperTableDescribeVO> describeSuperOrSubTable(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);

    /**
     * 添加标签
     *
     * @param dataBaseName
     * @param superTableName
     * @param fields
     */
    void alterSuperTableTag(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName, @Param("fields") Fields fields);

    /**
     * 删除标签
     *
     * @param dataBaseName
     * @param superTableName
     * @param fields
     */
    void dropSuperTableTag(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName, @Param("fields") Fields fields);

    /**
     * 修改标签名
     *
     * @param dataBaseName
     * @param superTableName
     * @param oldName
     * @param newName
     */
    void alterSuperTableTagRename(@Param("dataBaseName") String dataBaseName, @Param("superTableName") String superTableName, @Param("oldName") String oldName,
                                  @Param("newName") String newName);

    /**
     * 新增数据
     *
     * @param tableDTO
     */
    void insertTableData(TableDTO tableDTO);

    /**
     * 查询最新数据
     *
     * @param tagsSelectDTO
     * @return
     */
    List<Map<String, Object>> getLastDataByTags(TagsSelectDTO tagsSelectDTO);

    /**
     * Retrieves the latest data from the specified table within the given database.
     * If both startTime and endTime are provided, it fetches records within that time range.
     * Otherwise, it retrieves the last recorded data.
     *
     * @param dataBaseName The name of the database.
     * @param tableName    The name of the table.
     * @param startTime    The start time for the query range (can be null).
     * @param endTime      The end time for the query range (can be null).
     * @return A {@link List<Map<String,Object>>} containing the latest data.
     */
    List<Map<String, Object>> getDataInRangeOrLastRecord(
            @Param("dataBaseName") String dataBaseName,
            @Param("tableName") String tableName,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
}
