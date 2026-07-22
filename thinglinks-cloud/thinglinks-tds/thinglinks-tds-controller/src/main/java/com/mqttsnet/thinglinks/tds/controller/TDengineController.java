package com.mqttsnet.thinglinks.tds.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.utils.TdsUtils;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.tds.service.TdsService;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
 * 时序数据库 Controller
 *
 * @author xiaonannet
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tds")
@Tag(name = "时序数据库API")
public class TDengineController {

    private final TdsService tdsService;

    /**
     * 创建时序数据库
     *
     * @param dataBaseName 数据库名称
     * @return 执行结果
     */
    @Operation(summary = "创建时序数据库", description = "创建时序数据库")
    @PostMapping("/createDatabase")
    @WebLog("创建时序数据库")
    public R createDatabase(@RequestParam String dataBaseName) {
        try {
            tdsService.createDatabase(dataBaseName);
        } catch (Exception e) {
            log.error("创建时序数据库失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("创建超级表")
    public R createSuperTable(@RequestParam String superTableName) {
        try {
            tdsService.createSuperTable(ContextUtil.getDataBase(), superTableName);
        } catch (Exception e) {
            log.error("创建超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("创建超级表及字段")
    public R createSuperTableAndColumn(@RequestBody SuperTableDTO superTableDTO) {
        if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
            superTableDTO.setDataBaseName(ContextUtil.getDataBase());
        }
        try {
            tdsService.createSuperTableAndColumn(superTableDTO);
        } catch (Exception e) {
            log.error("创建超级表及字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    /**
     * 批量创建超级表及字段(单次请求可建多张)
     *
     * @param schema 超级表批量定义,key=超级表名
     * @return 执行结果
     */
    @Operation(summary = "批量创建超级表及字段", description = "单次请求可建多张超级表")
    @PostMapping("/batchCreateSuperTable")
    @WebLog("批量创建超级表及字段")
    public R batchCreateSuperTable(@RequestBody Map<String, Object> schema) {
        try {
            // schema 是 HTTP 中性 Map;TdsUtils(外部库)签名固定接 hutool JSONObject,这里走 putAll 适配
            JSONObject hutoolJson = new JSONObject();
            hutoolJson.putAll(schema);
            Map<String, SuperTableDTO> superTableDTOMap = TdsUtils.handleSuperTable(hutoolJson);
            for (Map.Entry<String, SuperTableDTO> entry : superTableDTOMap.entrySet()) {
                SuperTableDTO value = entry.getValue();
                value.setDataBaseName(ContextUtil.getDataBase());
                // 进行相应的操作
                tdsService.createSuperTableAndColumn(value);
            }
        } catch (Exception e) {
            log.error("创建超级表及字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("创建子表")
    public R createSubTable(@RequestBody TableDTO tableDTO) {
        try {
            if (StrUtil.isBlank(tableDTO.getDataBaseName())) {
                tableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.createSubTable(tableDTO);
        } catch (Exception e) {
            log.error("创建子表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    /**
     * 批量创建子表(单次请求可建多张)
     *
     * @param schema 子表批量定义,key=子表名
     * @return 执行结果
     */
    @Operation(summary = "批量创建子表", description = "单次请求可建多张子表")
    @PostMapping("/batchCreateSubTable")
    @WebLog("批量创建子表")
    public R batchCreateSubTable(@RequestBody Map<String, Object> schema) {
        try {
            // 同 batchCreateSuperTable:Map→hutool JSONObject 适配外部 TdsUtils 签名
            JSONObject hutoolJson = new JSONObject();
            hutoolJson.putAll(schema);
            Map<String, TableDTO> subTableMap = TdsUtils.handleSubTable(hutoolJson);
            for (Map.Entry<String, TableDTO> entry : subTableMap.entrySet()) {
                TableDTO value = entry.getValue();
                value.setDataBaseName(ContextUtil.getDataBase());
                // 进行相应的操作
                tdsService.createSubTable(value);
            }
        } catch (Exception e) {
            log.error("创建子表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("删除超级表")
    public R dropSuperTable(@RequestParam(value = "superTableName") String superTableName) {
        try {
            tdsService.dropSuperTable(ContextUtil.getDataBase(), superTableName);
        } catch (Exception e) {
            log.error("删除超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("超级表新增字段")
    public R alterSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.alterSuperTableColumn(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("超级表新增字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("超级表删除字段")
    public R dropSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.dropSuperTableColumn(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("超级表删除字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("新增标签")
    public R alterSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.alterSuperTableTag(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("新增标签失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("删除标签")
    public R dropSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.dropSuperTableTag(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("删除标签失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    @WebLog("修改标签名称")
    public R alterSuperTableTagRename(@RequestParam(value = "superTableName") String superTableName, @RequestParam(value = "oldName") String oldName, @RequestParam(value = "newName") String newName) {
        try {
            tdsService.alterSuperTableTagRename(ContextUtil.getDataBase(), superTableName, oldName, newName);
        } catch (Exception e) {
            log.error("修改标签名称失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(@RequestParam(value = "tableName") String tableName) {
        try {
            ArgumentAssert.notBlank(tableName, "表名称不能为空");
            List<SuperTableDescribeVO> superTableDescribeVOS = tdsService.describeSuperOrSubTable(ContextUtil.getDataBase(), tableName);
            return R.success(superTableDescribeVOS);
        } catch (Exception e) {
            log.error("查询超级表、子表结构失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
        try {
            if (StrUtil.isBlank(tableDTO.getDataBaseName())) {
                tableDTO.setDataBaseName(ContextUtil.getDataBase());
            }
            tdsService.insertTableData(tableDTO);
        } catch (Exception e) {
            log.error("新增数据失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
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
