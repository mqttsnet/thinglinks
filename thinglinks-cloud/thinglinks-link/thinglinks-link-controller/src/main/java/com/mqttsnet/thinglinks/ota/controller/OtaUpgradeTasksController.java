package com.mqttsnet.thinglinks.ota.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.ota.entity.OtaUpgradeTasks;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTargetsService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTaskExecutionService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTasksService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradesService;
import com.mqttsnet.thinglinks.ota.vo.param.SendDeviceOtaUpgradeCommandRequestParam;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeTargetsPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeTasksPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradesPageQuery;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeTargetsResultVO;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeTasksResultVO;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradesResultVO;
import com.mqttsnet.thinglinks.ota.vo.save.OtaUpgradeTasksSaveVO;
import com.mqttsnet.thinglinks.ota.vo.update.OtaUpgradeTasksUpdateVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * OTA升级任务表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:40:04
 * @create [2024-01-12 22:40:04] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/otaUpgradeTasks")
@Tag(name = "OTA升级任务")
public class OtaUpgradeTasksController extends SuperController<OtaUpgradeTasksService, Long, OtaUpgradeTasks, OtaUpgradeTasksSaveVO, OtaUpgradeTasksUpdateVO, OtaUpgradeTasksPageQuery, OtaUpgradeTasksResultVO> {
    private final EchoService echoService;

    private final OtaUpgradesService otaUpgradesService;

    private final OtaUpgradeTargetsService otaUpgradeTargetsService;

    private final OtaUpgradeTaskExecutionService otaUpgradeTaskExecutionService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    /**
     * 在上下文中执行异步任务
     *
     * @param task 需要执行的任务
     * @param <T>  返回类型
     * @return {@link CompletableFuture <T>} 任务执行结果
     */
    private <T> CompletableFuture<T> executeWithContext(Supplier<T> task) {
        Map<String, String> localMap = ContextUtil.getLocalMap();
        return CompletableFuture.supplyAsync(() -> {
            ContextUtil.setLocalMap(localMap);
            try {
                return task.get();
            } finally {
                ContextUtil.remove();
            }
        });
    }


    @Override
    public QueryWrap<OtaUpgradeTasks> handlerWrapper(OtaUpgradeTasks model, PageParams<OtaUpgradeTasksPageQuery> params) {
        QueryWrap<OtaUpgradeTasks> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("ota_upgrade_tasks");
        return queryWrap;
    }

    @Override
    public void handlerResult(IPage<OtaUpgradeTasksResultVO> page) {
        super.handlerResult(page);
        // 通用逻辑处理升级任务VO数据
        enrichOtaUpgradeTasksResultVO(page.getRecords());
    }

    /**
     * 通用方法：为OTA升级任务结果VO填充关联数据
     * 包括升级包信息和升级目标值列表
     *
     * @param tasks OTA升级任务结果VO列表
     */
    private void enrichOtaUpgradeTasksResultVO(List<OtaUpgradeTasksResultVO> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        List<Long> upgradeIds = tasks.stream()
                .map(OtaUpgradeTasksResultVO::getUpgradeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Long> taskIds = tasks.stream()
                .map(OtaUpgradeTasksResultVO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (upgradeIds.isEmpty() && taskIds.isEmpty()) {
            return;
        }

        CompletableFuture<Map<Long, OtaUpgradesResultVO>> otaUpgradesFuture =
                CollectionUtils.isEmpty(upgradeIds) ?
                        CompletableFuture.completedFuture(Collections.emptyMap()) :
                        executeWithContext(() ->
                                otaUpgradesService.getOtaUpgradesResultDTOList(
                                        OtaUpgradesPageQuery.builder().ids(upgradeIds).build()
                                )
                        ).thenApply(otaUpgrades ->
                                Optional.ofNullable(otaUpgrades)
                                        .orElseGet(Collections::emptyList)
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .map(dto -> BeanPlusUtil.toBeanIgnoreError(dto, OtaUpgradesResultVO.class))
                                        .filter(vo -> vo.getId() != null)
                                        .collect(Collectors.toMap(
                                                OtaUpgradesResultVO::getId,
                                                Function.identity(),
                                                (first, second) -> first
                                        ))
                        );

        CompletableFuture<Map<Long, List<String>>> otaUpgradeTargetsFuture =
                CollectionUtils.isEmpty(taskIds) ?
                        CompletableFuture.completedFuture(Collections.emptyMap()) :
                        executeWithContext(() ->
                                otaUpgradeTargetsService.getOtaUpgradeTargetsResultDTOList(
                                        OtaUpgradeTargetsPageQuery.builder().taskIds(taskIds).build()
                                )
                        ).thenApply(otaUpgradeTargets ->
                                Optional.ofNullable(otaUpgradeTargets)
                                        .orElseGet(Collections::emptyList)
                                        .stream()
                                        .filter(Objects::nonNull)
                                        .map(dto -> BeanPlusUtil.toBeanIgnoreError(dto, OtaUpgradeTargetsResultVO.class))
                                        .collect(Collectors.groupingBy(
                                                OtaUpgradeTargetsResultVO::getTaskId,
                                                Collectors.mapping(OtaUpgradeTargetsResultVO::getTargetValue, Collectors.toList())
                                        ))
                        );

        CompletableFuture.allOf(otaUpgradesFuture, otaUpgradeTargetsFuture).join();
        Map<Long, OtaUpgradesResultVO> otaUpgradesMap = otaUpgradesFuture.join();
        Map<Long, List<String>> otaUpgradeTargetsMap = otaUpgradeTargetsFuture.join();

        tasks.forEach(item -> {
            // 设置OTA升级包信息
            Optional.ofNullable(item.getUpgradeId())
                    .map(otaUpgradesMap::get)
                    .ifPresent(item::setOtaUpgradesResult);

            // 设置OTA升级目标信息列表
            Optional.ofNullable(item.getId())
                    .map(otaUpgradeTargetsMap::get)
                    .ifPresent(item::setTargetValueList);
        });
    }


    @Operation(summary = "保存OTA升级任务", description = "保存一个新的OTA升级任务")
    @PostMapping("/saveUpgradeTask")
    @WebLog(value = "保存OTA升级任务", request = false)
    public R<OtaUpgradeTasksSaveVO> saveUpgradeTask(@Valid @RequestBody OtaUpgradeTasksSaveVO saveVO) {
        OtaUpgradeTasksSaveVO saveUpgradeTask = superService.saveUpgradeTask(saveVO);
        return R.success(saveUpgradeTask);
    }

    @Operation(summary = "更新OTA升级任务", description = "更新一个现有的OTA升级任务")
    @PutMapping("/updateUpgradeTask")
    @WebLog(value = "更新OTA升级任务", request = false)
    public R<OtaUpgradeTasksUpdateVO> updateUpgradeTask(@Valid @RequestBody OtaUpgradeTasksUpdateVO updateVO) {
        OtaUpgradeTasksUpdateVO updatedTaskVO = superService.updateUpgradeTask(updateVO);
        return R.success(updatedTaskVO);
    }

    @Operation(summary = "更改OTA升级任务状态", description = "更改OTA升级任务的状态")
    @Parameters({@Parameter(name = "id", description = "任务ID", required = true), @Parameter(name = "status", description = "新任务状态（0：待处理，1：进行中，2：已完成，3：已取消）", required = true)})
    @PutMapping("/changeTaskStatus/{id}")
    @WebLog(value = "更改OTA升级任务状态", request = false)
    public R<Boolean> changeTaskStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("更改任务状态 id:{}, 状态:{}", id, status);
        return R.success(superService.changeTaskStatus(id, status));
    }

    @Operation(summary = "删除OTA升级任务", description = "通过其ID删除OTA升级任务")
    @Parameters({@Parameter(name = "id", description = "OTA升级任务ID", required = true)})
    @DeleteMapping("/deleteOtaUpgradeTask/{id}")
    @WebLog(value = "删除OTA升级任务", request = false)
    public R<Boolean> deleteOtaUpgradeTask(@PathVariable("id") Long id) {
        log.info("删除OTA升级任务 id: {}", id);
        return R.success(superService.deleteOtaUpgradeTask(id));
    }

    @Operation(summary = "批量删除OTA升级任务", description = "通过它们的ID批量删除OTA升级任务")
    @DeleteMapping("/deleteOtaUpgradeTasks")
    @WebLog(value = "批量删除OTA升级任务", request = false)
    public R<Boolean> deleteOtaUpgradeTasks(@RequestBody List<Long> ids) {
        log.info("批量删除OTA升级任务 ids: {}", ids);
        boolean allDeleted = ids.stream().distinct().allMatch(id -> superService.deleteOtaUpgradeTask(id));
        return R.success(allDeleted);
    }

    /**
     * 根据其ID获取OTA升级任务的详细信息，包括相关升级包信息。
     *
     * @param id 要检索的OTA升级任务的ID。
     * @return {@link OtaUpgradeTasksResultVO} OTA升级任务的详细信息。
     */
    @Operation(summary = "获取OTA升级任务详情", description = "通过ID检索OTA升级任务的详细信息")
    @Parameters({@Parameter(name = "id", description = "OTA升级任务ID", required = true)})
    @GetMapping("/details/{id}")
    public R<OtaUpgradeTasksResultVO> getUpgradeTaskDetails(@PathVariable Long id) {
        OtaUpgradeTasksResultVO upgradeTaskDetails = superService.getUpgradeTaskDetails(id);
        enrichOtaUpgradeTasksResultVO(Collections.singletonList(upgradeTaskDetails));
        echoService.action(upgradeTaskDetails);
        return R.success(upgradeTaskDetails);
    }

    /**
     * 从MQTT消息接收并保存新的OTA升级记录
     *
     * @param topoOtaCommandResponseParam 来自通过MQTT发送的OTA命令的响应参数。
     * @return {@link R<TopoOtaCommandResponseParam>} 包含已保存OTA升级记录的响应实体。
     */
    @Operation(summary = "通过MQTT保存OTA升级记录", description = "从MQTT消息数据保存一个新的OTA升级记录。")
    @PostMapping("/saveOtaUpgradeRecordByMqtt")
    @WebLog(value = "通过Mqtt保存OTA升级记录", request = false)
    public R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        log.info("通过MQTT保存OTA升级记录: {}", JSON.toJSONString(topoOtaCommandResponseParam));
        TopoOtaCommandResponseParam savedRecord = superService.saveOtaUpgradeRecordByMqtt(topoOtaCommandResponseParam);
        return R.success(savedRecord);
    }

    /**
     * 从HTTP请求接收并保存新的OTA升级记录
     *
     * @param topoOtaCommandResponseParam OTA升级任务的响应参数。
     * @return {@link R<TopoOtaCommandResponseParam>} 包含已保存OTA升级记录的响应实体。
     */
    @Operation(summary = "北向API保存OTA升级记录", description = "北向API保存一个新的OTA升级记录。")
    @PostMapping("/saveUpgradeRecordByNorthbound")
    @WebLog(value = "北向API保存OTA升级记录")
    public R<TopoOtaCommandResponseParam> saveUpgradeRecordByNorthbound(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        log.info("北向API保存OTA升级记录: {}", JSON.toJSONString(topoOtaCommandResponseParam));
        TopoOtaCommandResponseParam savedRecord = superService.saveUpgradeRecordByNorthbound(topoOtaCommandResponseParam);
        return R.success(savedRecord);
    }


    /**
     * Send an OTA upgrade command to a specified device.
     *
     * @param param The details of the OTA upgrade request.
     * @return The result of the OTA upgrade operation.
     */
    @Operation(summary = "发送OTA升级命令", description = "Sends an OTA upgrade command to the specified device.")
    @PostMapping("/sendDeviceOtaUpgradeCommand")
    public R<?> sendDeviceOtaUpgradeCommand(@RequestBody SendDeviceOtaUpgradeCommandRequestParam param) {
        try {
            log.info("sending OTA upgrade command to device . param: {}", JSON.toJSONString(param));
            otaUpgradeTaskExecutionService.sendDeviceOtaUpgradeCommand(param);
            return R.success("OTA upgrade command sent successfully.");
        } catch (Exception e) {
            return R.fail(e);
        }
    }


}