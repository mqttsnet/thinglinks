package com.mqttsnet.thinglinks.tdengine.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DataTypeEnum;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.tdengine.api.domain.*;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.visual.SelectVisualDto;
import com.mqttsnet.thinglinks.tdengine.common.constant.TdsConstants;
import com.mqttsnet.thinglinks.tdengine.service.TdEngineService;
import com.mqttsnet.thinglinks.tdengine.utils.TdsUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassDescription: TdEngine Controller
 * @ClassName: TdEngineController
 * @Author: thinglinks
 * @Date: 2021-12-27 13:46:44
 * @Version 1.0
 */

@RestController
@RequestMapping("/dataOperation")
public class TdEngineController {

    @Autowired
    private TdEngineService tdEngineService;

    private static final Logger log = LoggerFactory.getLogger(TdEngineController.class);

    /**
     * @param dataBaseName 数据库名称
     * @return R<?>
     * @MethodDescription 创建tdEngine数据库
     * @author thinglinks
     * @Date 2021/12/27 16:26
     */
    @PostMapping("/createDb")
    public R<?> createDataBase(@RequestBody() String dataBaseName) {
        try {
            //调用创建数据库方法
            this.tdEngineService.createDateBase(dataBaseName);
            log.info("successful operation: created database '" + dataBaseName + "' success");
            return R.ok();
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * @param superTableDto 创建超级表需要的入参的实体类
     * @return R<?>
     * @MethodDescription 创建超级表
     * @author thinglinks
     * @Date 2021/12/27 16:26
     */
    @PostMapping("/createSTb")
    public R<?> createSuperTable(@Validated @RequestBody SuperTableDto superTableDto) {
        //从入参对象获取列字段（超级表结构）对象集合
        List<Fields> schemaFields = superTableDto.getSchemaFields();
        //从入参对象获取标签字段对象集合
        List<Fields> tagsFields = superTableDto.getTagsFields();
        //从入参获取数据库名称
        String dataBaseName = superTableDto.getDataBaseName();
        //从入参获取超级表名称
        String superTableName = superTableDto.getSuperTableName();
        //获取列字段对象集合的第一个对象的字段数据类型
        DataTypeEnum dataType = schemaFields.get(0).getDataType();
        //如果该数据类型不是时间戳，打印和返回报错信息
        if (dataType == null || !"timestamp".equals(dataType.getDataType())) {
            log.error("invalid operation: first column must be timestamp");
            return R.fail("invalid operation: the first column must be timestamp");
        }

        try {
            //将列字段对象集合和标签字段对象集合转码为字段Vo类对象集合
            List<FieldsVo> schemaFieldsVoList = FieldsVo.fieldsTranscoding(schemaFields);
            List<FieldsVo> tagsFieldsVoList = FieldsVo.fieldsTranscoding(tagsFields);
            //创建超级表
            this.tdEngineService.createSuperTable(schemaFieldsVoList, tagsFieldsVoList, dataBaseName, superTableName);
            log.info("successful operation: created superTable '" + superTableName + "' success");
            return R.ok();
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 添加列字段
     *
     * @param superTableDto
     * @return
     */
    @PostMapping("/addColumnInStb")
    public R<?> addColumnForSuperTable(@RequestBody SuperTableDto superTableDto) {

        String superTableName = superTableDto.getSuperTableName();
        if (StringUtils.isBlank(superTableName)) {
            return R.fail("invalid operation: superTableName can not be empty");
        }

        Fields fields = superTableDto.getFields();
        if (fields == null) {
            return R.fail("invalid operation: fields can not be empty");
        }

        try {
            FieldsVo fieldsVo = FieldsVo.fieldsTranscoding(fields);
            this.tdEngineService.addColumnForSuperTable(superTableName, fieldsVo);
            log.info("successful operation: add column for superTable '" + superTableName + "' success");
            return R.ok();
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除列字段
     *
     * @param superTableDto
     * @return
     */
    @PostMapping("/dropColumnInStb")
    public R<?> dropColumnForSuperTable(@RequestBody SuperTableDto superTableDto) {

        String superTableName = superTableDto.getSuperTableName();
        if (StringUtils.isBlank(superTableName)) {
            return R.fail("invalid operation: superTableName can not be empty");
        }

        Fields fields = superTableDto.getFields();
        if (fields == null) {
            return R.fail("invalid operation: fields can not be empty");
        }

        try {
            FieldsVo fieldsVo = FieldsVo.fieldsTranscoding(fields);
            this.tdEngineService.addColumnForSuperTable(superTableName, fieldsVo);
            log.info("successful operation: drop column for superTable '" + superTableName + "' success");
            return R.ok();
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * @param tableDto 创建超级表的子表需要的入参的实体类
     * @return R<?>
     * @MethodDescription 创建超级表的子表
     * @author thinglinks
     * @Date 2021/12/30 14:15
     */
    @PostMapping("/createTb")
    public R<?> createTable(@Validated @RequestBody TableDto tableDto) {
        try {
            this.tdEngineService.createTable(tableDto);
            log.info("successful operation: create table success");
            return R.ok("successful operation: create table success");
        } catch (Exception e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            log.error(message);
            return R.fail(message);
        }
    }

    /**
     * @param tableDto 插入数据需要的入参的实体类
     * @return R<?>
     * @MethodDescription 插入数据
     * @author thinglinks
     * @Date 2022/1/10 14:43
     */
    @PostMapping("/insertData")
    public R<?> insertData(@Validated @RequestBody TableDto tableDto) {
        try {
            List<Fields> tagsFieldValues = tableDto.getTagsFieldValues();
            for (Fields fields : tagsFieldValues) {
                if (StringUtils.isBlank(fields.getFieldName()) || fields.getFieldValue() == null) {
                    log.error("invalid operation: fieldName or fieldValue can not be empty");
                    return R.fail("invalid operation: fieldName or fieldValue can not be empty");
                }
            }
            this.tdEngineService.insertData(tableDto);
            log.info("successful operation: insert data success");
            return R.ok();
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * @param selectDto 查询数据需要的入参的实体类
     * @return R<?>
     * @MethodDescription 根据时间戳查询数据
     * @author thinglinks
     * @Date 2022/1/10 14:44
     */
    @PostMapping("/getDataByTimestamp")
    public R<?> getDataByTimestamp(@Validated @RequestBody SelectDto selectDto) {
        try {
            return R.ok(this.tdEngineService.selectByTimesTamp(selectDto));
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/getCountByTimestamp")
    public R<?> getCountByTimestamp(@Validated @RequestBody SelectDto selectDto) {
        try {
            return R.ok(this.tdEngineService.getCountByTimesTamp(selectDto));
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }


    /**
     * @param selectDto
     * @return R<?>
     * @MethodDescription 查询最新数据
     */
    @PostMapping("/getLastData")
    public R<?> getLastData(@Validated @RequestBody SelectDto selectDto) {
        try {
            return R.ok(this.tdEngineService.getLastData(selectDto));
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }


    /**
     * @param selectVisualDto
     * @return R<?>
     * @MethodDescription 可视化数据  返回格式，时间，数值列
     * 历史数据 SELECT voltage ,ts FROM test.meters where ts between '2017-07-14 02:40:00.000' and '2017-07-14 02:40:00.001' LIMIT [topnums]
     * ts > now - 24h;
     * 实时数据 select col from table LIMIT [topnums]
     * 聚合数据 select [avg/max/sum/count..](col) from table where ts between '2017-07-14 02:40:00.000' and '2017-07-14 02:40:00.001' group by col LIMIT [topnums]
     */
    @PostMapping("/getVisualizeData")
    public R<?> getVisualizeData(@Validated @RequestBody SelectVisualDto selectVisualDto) {
        try {
            if (selectVisualDto.getType() == 0) {//查询历史
                return R.ok(this.tdEngineService.getHistoryData(selectVisualDto));
            } else if (selectVisualDto.getType() == 1) {//查询实时
                return R.ok(this.tdEngineService.getRealtimeData(selectVisualDto));
            } else {//查询聚合
                return R.ok(this.tdEngineService.getAggregateData(selectVisualDto));
            }
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * @param tagsSelectDao
     * @return R<?>
     * @MethodDescription 根据超级表查询包含Tags的最新数据集合
     */
    @PostMapping("/getLastDataByTags")
    public R<Map<String, Map<String, Object>>> getLastDataByTags(@Validated @RequestBody TagsSelectDao tagsSelectDao) {
        try {
            return R.ok(this.tdEngineService.getLastDataByTags(tagsSelectDao));
        } catch (UncategorizedSQLException e) {
            String message = e.getCause().getMessage();
            try {
                message = message.substring(message.lastIndexOf("invalid operation"));
            } catch (Exception ex) {
            }
            log.error(message);
            return R.fail(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            return R.fail(e.getMessage());
        }
    }


    /**
     * 创建超级表
     *
     * @param superTableName 超级表名称
     * @return 执行结果
     */
    @ApiOperation(value = "创建超级表", notes = "创建超级表")
    @PostMapping("/createSuperTable")
    public R createSuperTable(@RequestParam String superTableName) {
        try {
            tdEngineService.createSuperTable(TdsConstants.DATA_BASE, superTableName);
        } catch (Exception e) {
            log.error("创建超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 创建超级表及字段
     *
     * @param superTableDTO 超级表信息
     * @return 执行结果
     */
    @ApiOperation(value = "创建超级表及字段", notes = "创建超级表及字段")
    @PostMapping("/createSuperTableAndColumn")
    public R createSuperTableAndColumn(@RequestBody SuperTableDTO superTableDTO) {
        if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
            superTableDTO.setDataBaseName(TdsConstants.DATA_BASE);
        }
        try {
            tdEngineService.createSuperTableAndColumn(superTableDTO);
        } catch (Exception e) {
            log.error("创建超级表及字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 创建超级表及字段-方式二
     *
     * @param object 超级表json信息
     * @return 执行结果
     */
    @ApiOperation(value = "创建超级表及字段-方式二", notes = "创建超级表及字段-方式二")
    @PostMapping("/createSuperTableAndColumnTwo")
    public R createSuperTableAndColumnOne(@RequestBody JSONObject object) {
        try {
            Map<String, SuperTableDTO> superTableDTOMap = TdsUtils.handleSuperTable(object);
            for (Map.Entry<String, SuperTableDTO> entry : superTableDTOMap.entrySet()) {
                SuperTableDTO value = entry.getValue();
                value.setDataBaseName(TdsConstants.DATA_BASE);
                // 进行相应的操作
                tdEngineService.createSuperTableAndColumn(value);
            }
        } catch (Exception e) {
            log.error("创建超级表及字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 创建子表
     *
     * @param tableDTO 子表信息
     * @return 执行结果
     */
    @ApiOperation(value = "创建子表", notes = "创建子表")
    @PostMapping("/createSubTable")
    public R createSubTable(@RequestBody TableDTO tableDTO) {
        try {
            if (StrUtil.isBlank(tableDTO.getDataBaseName())) {
                tableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.createSubTable(tableDTO);
        } catch (Exception e) {
            log.error("创建子表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 创建子表-方式二
     *
     * @param object 子表json信息
     * @return 执行结果
     */
    @ApiOperation(value = "创建子表-方式二", notes = "创建子表-方式二")
    @PostMapping("/createSubTableTwo")
    public R createSubTableTwo(@RequestBody JSONObject object) {
        try {
            Map<String, TableDTO> subTableMap = TdsUtils.handleSubTable(object);
            for (Map.Entry<String, TableDTO> entry : subTableMap.entrySet()) {
                TableDTO value = entry.getValue();
                value.setDataBaseName(TdsConstants.DATA_BASE);
                // 进行相应的操作
                tdEngineService.createSubTable(value);
            }
        } catch (Exception e) {
            log.error("创建子表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 删除超级表
     *
     * @param superTableName
     * @return
     */
    @ApiOperation(value = "删除超级表", notes = "删除超级表")
    @PostMapping("/dropSuperTable")
    public R dropSuperTable(@RequestParam String superTableName) {
        try {
            tdEngineService.dropSuperTable(TdsConstants.DATA_BASE, superTableName);
        } catch (Exception e) {
            log.error("删除超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 超级表新增字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @ApiOperation(value = "超级表新增字段", notes = "超级表新增字段")
    @PostMapping("/alterSuperTableColumn")
    public R alterSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.alterSuperTableColumn(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("超级表新增字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 超级表删除字段
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @ApiOperation(value = "超级表删除字段", notes = "超级表删除字段")
    @PostMapping("/dropSuperTableColumn")
    public R dropSuperTableColumn(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.dropSuperTableColumn(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("超级表删除字段失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 新增标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @ApiOperation(value = "新增标签", notes = "新增标签")
    @PostMapping("/alterSuperTableTag")
    public R alterSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.alterSuperTableTag(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("新增标签失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 删除标签
     *
     * @param superTableDTO 数据信息
     * @return 执行结果
     */
    @ApiOperation(value = "删除标签", notes = "删除标签")
    @PostMapping("/dropSuperTableTag")
    public R dropSuperTableTag(@RequestBody SuperTableDTO superTableDTO) {
        try {
            if (StrUtil.isBlank(superTableDTO.getDataBaseName())) {
                superTableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.dropSuperTableTag(superTableDTO.getDataBaseName(), superTableDTO.getSuperTableName(), superTableDTO.getFields());
        } catch (Exception e) {
            log.error("删除标签失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    /**
     * 修改标签名称
     *
     * @param superTableName
     * @param oldName
     * @param newName
     * @return
     */
    @ApiOperation(value = "修改标签名称", notes = "修改标签名称")
    @PostMapping("/alterSuperTableTagRename")
    public R alterSuperTableTagRename(@RequestParam String superTableName, @RequestParam String oldName, @RequestParam String newName) {
        try {
            tdEngineService.alterSuperTableTagRename(TdsConstants.DATA_BASE, superTableName, oldName, newName);
        } catch (Exception e) {
            log.error("修改标签名称失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
    }


    /**
     * 查询超级表、子表结构
     *
     * @param tableName
     * @return
     */
    @ApiOperation(value = "查询超级表、子表结构", notes = "查询超级表、子表结构")
    @GetMapping("/describeSuperOrSubTable")
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(@RequestParam(value = "tableName") String tableName) {
        try {
            if (StrUtil.isBlank(tableName)) {
                return R.fail("表名不能为空");
            }
            List<SuperTableDescribeVO> superTableDescribeVOS = tdEngineService.describeSuperOrSubTable(TdsConstants.DATA_BASE, tableName);
            return R.ok(superTableDescribeVOS);
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
    @ApiOperation(value = "新增数据", notes = "新增数据")
    @PostMapping("/insertTableData")
    public R insertTableData(@RequestBody TableDTO tableDTO) {
        try {
            if (StrUtil.isBlank(tableDTO.getDataBaseName())) {
                tableDTO.setDataBaseName(TdsConstants.DATA_BASE);
            }
            tdEngineService.insertTableData(tableDTO);
        } catch (Exception e) {
            log.error("新增数据失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.ok();
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
    @ApiOperation(value = "Query Data from a Regular Table Within a Time Range", notes = "Fetches data within the specified time range if both start and end times are provided; otherwise, retrieves the latest record.")
    @GetMapping("/getDataInRangeOrLastRecord")
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(
            @ApiParam(value = "Name of the regular table", required = true) @RequestParam(value = "tableName") String tableName,
            @ApiParam(value = "Start time for the query", example = "1634572800000", required = false) @RequestParam(value = "startTime", required = false) Long startTime,
            @ApiParam(value = "End time for the query", example = "1634659200000", required = false) @RequestParam(value = "endTime", required = false) Long endTime) {
        try {
            if (StrUtil.isBlank(tableName)) {
                return R.fail("Table name cannot be empty");
            }
            List<Map<String, Object>> dataInRangeOrLastRecord = tdEngineService.getDataInRangeOrLastRecord(TdsConstants.DATA_BASE, tableName, startTime, endTime);
            return R.ok(dataInRangeOrLastRecord);
        } catch (Exception e) {
            log.error("Failed to query the latest record from the table", e);
            return R.fail("Query failed: " + e.getMessage());
        }
    }

}
