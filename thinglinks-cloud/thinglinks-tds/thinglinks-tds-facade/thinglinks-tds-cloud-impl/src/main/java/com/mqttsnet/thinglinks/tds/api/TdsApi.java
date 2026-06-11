package com.mqttsnet.thinglinks.tds.api;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.thinglinks.tds.api.hystrix.TdsApiFallback;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 时序数据库远程接口
 *
 * @author xiaonannet
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-tds-server}", fallback = TdsApiFallback.class, path = "/anyUser/tds")
public interface TdsApi {

    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    @PostMapping("/createDatabase")
    R createDatabase(@RequestParam String dataBaseName);

    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    @PostMapping("/createSuperTable")
    R createSuperTable(@RequestParam String superTableName);

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    @PostMapping("/createSuperTableAndColumn")
    R createSuperTableAndColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 批量创建超级表及字段(单次请求可建多张)。
     *
     * @param schema 超级表批量定义,key=超级表名
     * @return 执行结果
     */
    @PostMapping("/batchCreateSuperTable")
    R batchCreateSuperTable(@RequestBody Map<String, Object> schema);

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    @PostMapping("/createSubTable")
    R createSubTable(@RequestBody TableDTO tableDTO);

    /**
     * 批量创建子表(单次请求可建多张)。
     *
     * @param schema 子表批量定义,key=子表名
     * @return 执行结果
     */
    @PostMapping("/batchCreateSubTable")
    R batchCreateSubTable(@RequestBody Map<String, Object> schema);

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    @PostMapping("/dropSuperTable")
    R dropSuperTable(@RequestParam(value = "superTableName") String superTableName);

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/alterSuperTableColumn")
    R alterSuperTableColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dropSuperTableColumn")
    R dropSuperTableColumn(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/alterSuperTableTag")
    R alterSuperTableTag(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/dropSuperTableTag")
    R dropSuperTableTag(@RequestBody SuperTableDTO superTableDTO);

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    @PostMapping("/alterSuperTableTagRename")
    R alterSuperTableTagRename(@RequestParam(value = "superTableName") String superTableName, @RequestParam(value = "oldName") String oldName, @RequestParam(value = "newName") String newName);


    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    @GetMapping("/describeSuperOrSubTable")
    R<List<SuperTableDescribeVO>> describeSuperOrSubTable(@RequestParam(value = "tableName") String tableName);

    /**
     * 新增数据
     *
     * @param tableDTO 数据信息
     * @return 执行结果
     */
    @PostMapping("/insertTableData")
    R insertTableData(@RequestBody TableDTO tableDTO);


    /**
     * Retrieves the latest data from a regular table. It fetches the most recent record
     * within the specified time range or the last recorded data if the time range is not specified.
     *
     * @param tableName The name of the table.
     * @param startTime The start time for the query range (optional).
     * @param endTime   The end time for the query range (optional).
     * @return {@link R<List<<Map<String,Object>>>} The query result.
     */
    @Operation(summary = "Query Data from a Regular Table Within a Time Range", description = "Fetches data within the specified time range if both start and end times are provided; otherwise, retrieves the latest record.")
    @GetMapping("/getDataInRangeOrLastRecord")
    R<List<Map<String, Object>>> getDataInRangeOrLastRecord(
            @RequestParam(value = "tableName") String tableName,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime);


}
