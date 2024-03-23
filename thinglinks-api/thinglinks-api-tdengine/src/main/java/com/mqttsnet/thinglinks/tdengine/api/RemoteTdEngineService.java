package com.mqttsnet.thinglinks.tdengine.api;

/**
 * @InterfaceDescription: 时序性数据库TdEngine服务
 * @InterfaceName: RemoteTdEngineService
 * @Author: thinglinks
 * @Date: 2021-12-31 10:57:16
 * @Version 1.0
 */

import cn.hutool.json.JSONObject;
import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.tdengine.api.domain.*;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
import com.mqttsnet.thinglinks.tdengine.api.factory.RemoteTdEngineFallbackFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteTdEngineService", value = ServiceNameConstants.THINGLINKS_TDENGINE, fallbackFactory = RemoteTdEngineFallbackFactory.class)
public interface RemoteTdEngineService {

    /**
     * @param databaseName 数据库名称
     * @return R
     * @MethodDescription 创建tdEngine数据库
     * @author thinglinks
     * @Date 2021/12/31 11:05
     */
    @PostMapping("/dataOperation/createDb")
    R<?> createDataBase(@RequestParam("databaseName") String databaseName);

    /**
     * @param superTableDto 创建超级表需要的入参的实体类
     * @return R
     * @MethodDescription 创建超级表
     * @author thinglinks
     * @Date 2021/12/27 11:05
     */
    @PostMapping("/dataOperation/createSTb")
    R<?> createSuperTable(@Validated @RequestBody SuperTableDto superTableDto);

    /**
     * @param tableDto 创建超级表的子表需要的入参的实体类
     * @return R
     * @MethodDescription 创建超级表的子表
     * @author thinglinks
     * @Date 2021/12/27 11:06
     */
    @PostMapping("/dataOperation/createTb")
    R<?> createTable(@Validated @RequestBody TableDto tableDto);

    /**
     * @param tableDto 插入数据需要的入参的实体类
     * @return R
     * @MethodDescription 插入数据
     * @author thinglinks
     * @Date 2022/1/10 14:44
     */
    @PostMapping("/dataOperation/insertData")
    R<?> insertData(@Validated @RequestBody TableDto tableDto);

    @PostMapping("/dataOperation/addColumnInStb")
    R<?> addColumnInStb(@RequestBody SuperTableDto superTableDto);

    /**
     * @param selectDto 查询数据需要的入参的实体类
     * @return R
     * @MethodDescription 根据时间戳查询数据
     * @author thinglinks
     * @Date 2022/1/10 14:44
     */
    @PostMapping("/dataOperation/getDataByTimestamp")
    R<?> getDataByTimestamp(@Validated @RequestBody SelectDto selectDto);

    /**
     * @param selectDto
     * @return
     * @MethodDescription 查询最新数据
     */
    @PostMapping("/dataOperation/getLastData")
    R<?> getLastData(@Validated @RequestBody SelectDto selectDto);

    /**
     * 查询最新的数据带标签
     *
     * @param tagsSelectDao
     * @return
     */
    @PostMapping("/dataOperation/getLastDataByTags")
    public R<Map<String, Map<String, Object>>> getLastDataByTags(@Validated @RequestBody TagsSelectDao tagsSelectDao);

    /**
     * 添加列字段
     *
     * @param superTableDto
     * @return
     */
    @PostMapping("/dataOperation/addColumnInStb")
    R<?> addColumnForSuperTable(@RequestBody SuperTableDto superTableDto);

    /**
     * 删除列字段
     *
     * @param superTableDto
     * @return
     */
    @PostMapping("/dataOperation/dropColumnInStb")
    R<?> dropColumnForSuperTable(@RequestBody SuperTableDto superTableDto);

    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createDatabase")
    public R createDatabase(@RequestParam String dataBaseName);

    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createSuperTable")
    public R createSuperTable(@RequestParam String superTableName);

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createSuperTableAndColumn")
    public R createSuperTableAndColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 创建超级表及字段-方式二
     *
     * @param object 超级表json信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createSuperTableAndColumnTwo")
    public R createSuperTableAndColumnTwo(@RequestBody JSONObject object);

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createSubTable")
    public R createSubTable(@RequestBody TableDTO tableDTO);

    /**
     * 创建子表-方式二
     *
     * @param object 子表json信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/createSubTableTwo")
    public R createSubTableTwo(@RequestBody JSONObject object);

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    @PostMapping("/dataOperation/dropSuperTable")
    public R dropSuperTable(@RequestParam String superTableName);

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/alterSuperTableColumn")
    public R alterSuperTableColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/dropSuperTableColumn")
    public R dropSuperTableColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/alterSuperTableTag")
    public R alterSuperTableTag(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/dropSuperTableTag")
    public R dropSuperTableTag(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    @PostMapping("/dataOperation/alterSuperTableTagRename")
    public R alterSuperTableTagRename(@RequestParam String superTableName, @RequestParam String oldName, @RequestParam String newName);


    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    @GetMapping("/dataOperation/describeSuperOrSubTable")
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(@RequestParam(value = "tableName") String tableName);

    /**
     * 新增数据
     *
     * @param tableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dataOperation/insertTableData")
    public R insertTableData(@RequestBody TableDTO tableDTO);


    /**
     * Retrieves the latest data from a regular table. It fetches the most recent record
     * within the specified time range or the last recorded data if the time range is not specified.
     *
     * @param tableName The name of the table.
     * @param startTime The start time for the query range (optional).
     * @param endTime   The end time for the query range (optional).
     * @return {@link R<List<<Map<String,Object>>>} The query result.
     */
    @ApiOperation(value = "Query Data from a Regular Table Within a Time Range", notes = "Fetches data within the specified time range if both start and end times are provided; otherwise, retrieves the latest record.")
    @GetMapping("/dataOperation/getDataInRangeOrLastRecord")
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(
            @ApiParam(value = "Name of the regular table", required = true) @RequestParam(value = "tableName") String tableName,
            @ApiParam(value = "Start time for the query", example = "1634572800000", required = false) @RequestParam(value = "startTime", required = false) Long startTime,
            @ApiParam(value = "End time for the query", example = "1634659200000", required = false) @RequestParam(value = "endTime", required = false) Long endTime);

}
