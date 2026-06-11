package com.mqttsnet.thinglinks.tds.facade.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.utils.TdsUtils;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.tds.facade.TdsFacade;
import com.mqttsnet.thinglinks.tds.service.TdsService;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author tangyh
 * @since 2024/12/24 21:18
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TdsFacadeImpl implements TdsFacade {
    private final TdsService tdsService;

    @Override
    public R createDatabase(String dataBaseName) {
        try {
            tdsService.createDatabase(dataBaseName);
        } catch (Exception e) {
            log.error("创建时序数据库失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    @Override
    public R createSuperTable(String superTableName) {
        try {
            tdsService.createSuperTable(ContextUtil.getDataBase(), superTableName);
        } catch (Exception e) {
            log.error("创建超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    @Override
    public R createSuperTableAndColumn(SuperTableDTO superTableDTO) {
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

    @Override
    public R batchCreateSuperTable(Map<String, Object> schema) {
        try {
            // schema 是 Feign 契约中性 Map;TdsUtils(thinglinks-util-pro 外部库)签名固定接 hutool JSONObject,
            // 这里走 Map.putAll 适配 ── 只用 Map 接口,不走 JSONUtil.toBean / parseObj 的 setFormatIfDate 风险 path
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

    @Override
    public R createSubTable(TableDTO tableDTO) {
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

    @Override
    public R batchCreateSubTable(Map<String, Object> schema) {
        try {
            // 同 batchCreateSuperTable:Map→hutool JSONObject 适配 TdsUtils 外部库签名,走 putAll 避开 JSONConverter
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

    @Override
    public R dropSuperTable(String superTableName) {
        try {
            tdsService.dropSuperTable(ContextUtil.getDataBase(), superTableName);
        } catch (Exception e) {
            log.error("删除超级表失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    @Override
    public R alterSuperTableColumn(SuperTableDTO superTableDTO) {
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

    @Override
    public R dropSuperTableColumn(SuperTableDTO superTableDTO) {
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

    @Override
    public R alterSuperTableTag(SuperTableDTO superTableDTO) {
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

    @Override
    public R dropSuperTableTag(SuperTableDTO superTableDTO) {
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

    @Override
    public R alterSuperTableTagRename(String superTableName, String oldName, String newName) {
        try {
            tdsService.alterSuperTableTagRename(ContextUtil.getDataBase(), superTableName, oldName, newName);
        } catch (Exception e) {
            log.error("修改标签名称失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
        return R.success();
    }

    @Override
    public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(String tableName) {
        try {
            ArgumentAssert.notBlank(tableName, "表名称不能为空");
            List<SuperTableDescribeVO> superTableDescribeVOS = tdsService.describeSuperOrSubTable(ContextUtil.getDataBase(), tableName);
            return R.success(superTableDescribeVOS);
        } catch (Exception e) {
            log.error("查询超级表、子表结构失败{}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    @Override
    public R insertTableData(TableDTO tableDTO) {
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

    @Override
    public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(String tableName, Long startTime, Long endTime) {
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
