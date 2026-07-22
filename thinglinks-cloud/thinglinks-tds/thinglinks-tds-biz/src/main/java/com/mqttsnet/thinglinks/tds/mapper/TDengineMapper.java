package com.mqttsnet.thinglinks.tds.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.mqttsnet.basic.tds.model.Fields;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.model.TagsSelectDTO;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * TDengine 时序库 Mapper。
 *
 * <p><b>必须 {@link InterceptorIgnore} 跳过 JSqlParser 系拦截器</b>:TDengine 的 DDL/DML 是非标准语法
 * (CREATE STABLE / ... TAGS(...) / DESCRIBE 等),而 createSuperTableAndColumn 等建表/改表/删表在 XML 中映射为
 * {@code <update>/<delete>},会被 MyBatis 归类为 UPDATE/DELETE。MyBatis-Plus 的 BlockAttackInnerInterceptor 只对
 * UPDATE/DELETE 用 JSqlParser 解析,而 JSqlParser 解析不了 TDengine 语法(报 {@code Encountered unexpected token "if"}),
 * 整条 DDL 失败。tenantLine / illegalSql 同理:TD 表无租户列、SQL 也非标准,均不应被这些 MySQL 取向的拦截器解析改写。
 * 隔离靠 @DS 切到 TD 数据源 + 库名维度,与这些拦截器无关。</p>
 */
@Repository
@InterceptorIgnore(tenantLine = "true", blockAttack = "true", illegalSql = "true")
public interface TDengineMapper {

    /**
     * 创建数据库
     *
     * @param dataBaseName
     */
    void createDatabase(@Param("dataBaseName") String dataBaseName);

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
     * @return {@link List<Map<String,Object>>} containing the latest data.
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
