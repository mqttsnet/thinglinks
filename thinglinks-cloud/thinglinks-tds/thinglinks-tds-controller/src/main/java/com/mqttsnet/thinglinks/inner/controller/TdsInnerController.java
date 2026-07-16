package com.mqttsnet.thinglinks.inner.controller;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.tds.service.TdsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * 时序数据库内部接口（inner）
 * Feign 服务间 RPC(Nacos 直连、不过网关)：透传 TenantId、无需 Token；网关拒绝外部访问。
 *
 * @author mqttsnet
 */

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/inner/tds")
@Tag(name = "inner-TDS时序数据库")
public class TdsInnerController {

    @Autowired
    private TdsService tdsService;


    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    @Operation(summary = "创建时序数据库", description = "创建时序数据库")
    @PostMapping("/createDatabase")
    public R createDatabase(@RequestParam(value = "dataBaseName") String dataBaseName) {
        tdsService.createDatabase(dataBaseName);
        return R.success();
    }

    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    @Operation(summary = "创建超级表", description = "创建超级表")
    @PostMapping("/createSuperTable")
    public R createSuperTable(@RequestParam(value = "superTableName") String superTableName) {
        tdsService.createSuperTable(ContextUtil.getDataBase(), superTableName);
        return R.success();
    }

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    @Operation(summary = "创建超级表及字段", description = "创建超级表及字段")
    @PostMapping("/createSuperTableAndColumn")
    public R createSuperTableAndColumn(@RequestBody SuperTableDTO superTableDTO) {
        superTableDTO.setDataBaseName(ContextUtil.getDataBase());
        tdsService.createSuperTableAndColumn(superTableDTO);
        return R.success();
    }

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    @Operation(summary = "创建子表", description = "创建子表")
    @PostMapping("/createSubTable")
    public R createSubTable(@RequestBody TableDTO tableDTO) {
        tableDTO.setDataBaseName(ContextUtil.getDataBase());
        tdsService.createSubTable(tableDTO);
        return R.success();
    }

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    @Operation(summary = "删除超级表", description = "删除超级表")
    @PostMapping("/dropSuperTable")
    public R dropSuperTable(@RequestParam(value = "superTableName") String superTableName) {
        tdsService.dropSuperTable(ContextUtil.getDataBase(), superTableName);
        return R.success();
    }

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Operation(summary = "超级表新增字段", description = "超级表新增字段")
    @PostMapping("/alterSuperTableColumn")
    public R alterSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        tdsService.alterSuperTableColumn(ContextUtil.getDataBase(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        return R.success();
    }

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Operation(summary = "超级表删除字段", description = "超级表删除字段")
    @PostMapping("/dropSuperTableColumn")
    public R dropSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        tdsService.dropSuperTableColumn(ContextUtil.getDataBase(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        return R.success();
    }

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Operation(summary = "新增标签", description = "新增标签")
    @PostMapping("/alterSuperTableTag")
    public R alterSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        tdsService.alterSuperTableTag(ContextUtil.getDataBase(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        return R.success();
    }

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @Operation(summary = "删除标签", description = "删除标签")
    @PostMapping("/dropSuperTableTag")
    public R dropSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        tdsService.dropSuperTableTag(ContextUtil.getDataBase(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        return R.success();
    }

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    @Operation(summary = "修改标签名称", description = "修改标签名称")
    @PostMapping("/alterSuperTableTagRename")
    public R alterSuperTableTagRename(@RequestParam(value = "superTableName") String superTableName, @RequestParam(value = "oldName") String oldName, @RequestParam(value = "newName") String newName) {
        tdsService.alterSuperTableTagRename(ContextUtil.getDataBase(), superTableName, oldName, newName);
        return R.success();
    }


    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    @Operation(summary = "查询超级表、子表结构", description = "查询超级表、子表结构")
    @GetMapping("/describeSuperOrSubTable")
    public R describeSuperOrSubTable(@RequestParam(value = "tableName") String tableName) {
        return R.success(tdsService.describeSuperOrSubTable(ContextUtil.getDataBase(), tableName));
    }

    /**
     * 新增数据
     *
     * @param tableDTO 数据信息
     * @return 执行结果
     */
    @Operation(summary = "新增数据", description = "新增数据")
    @PostMapping("/insertTableData")
    public R insertTableData(@RequestBody TableDTO tableDTO) {
        tableDTO.setDataBaseName(ContextUtil.getDataBase());
        tdsService.insertTableData(tableDTO);
        return R.success();
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
    @Operation(summary = "Query Data from a Regular Table Within a Time Range", description = "Fetches data within the specified time range if both start and end times are provided; otherwise, retrieves the latest record.")
    @Parameters({
            @Parameter(description = "Name of the regular table", required = true),
            @Parameter(description = "Start time for the query, in nanoseconds. Example: 1634572800000000000 represents 2021-10-18 00:00:00 UTC", example = "1634572800000000000", required = false),
            @Parameter(description = "End time for the query, in nanoseconds. Example: 1634659200000000000 represents 2021-10-19 00:00:00 UTC", example = "1634659200000000000", required = false)
    })
    @GetMapping("/getDataInRangeOrLastRecord")
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(@RequestParam(value = "tableName") String tableName, @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        try {
            ArgumentAssert.notBlank(tableName, "Table name cannot be blank");
            CompletableFuture<List<Map<String, Object>>> dataInRangeOrLastRecord = tdsService.getDataInRangeOrLastRecord(ContextUtil.getDataBase(), tableName, startTime, endTime);
            return R.success(dataInRangeOrLastRecord.get());
        } catch (Exception e) {
            log.error("Failed to query the latest record from the table", e);
            return R.fail("Query failed: " + e.getMessage());
        }
    }

}
