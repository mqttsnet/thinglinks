package com.mqttsnet.thinglinks.link.controller.ota;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.exception.ArgumentException;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.result.OtaUpgradeTasksResultVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeTasksSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeTasksUpdateVO;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradeTasksService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/otaUpgradeTasks")
@Api(value = "OtaUpgradeTasks", tags = "OTA升级任务表")
@Slf4j
public class OtaUpgradeTasksController extends BaseController {

    @Resource
    private OtaUpgradeTasksService otaUpgradeTasksService;

    @ApiOperation(value = "保存OTA升级任务", httpMethod = "POST", notes = "保存一个新的OTA升级任务")
    @PostMapping("/saveUpgradeTask")
    public R<OtaUpgradeTasksSaveVO> saveUpgradeTask(@Valid @RequestBody OtaUpgradeTasksSaveVO saveVO) {
        OtaUpgradeTasksSaveVO saveUpgradeTask = otaUpgradeTasksService.saveUpgradeTask(saveVO);
        return R.ok(saveUpgradeTask);
    }

    @ApiOperation(value = "更新OTA升级任务", httpMethod = "PUT", notes = "更新一个现有的OTA升级任务")
    @PutMapping("/updateUpgradeTask")
    public R<OtaUpgradeTasksUpdateVO> updateUpgradeTask(@Valid @RequestBody OtaUpgradeTasksUpdateVO updateVO) {
        OtaUpgradeTasksUpdateVO updatedTaskVO = otaUpgradeTasksService.updateUpgradeTask(updateVO);
        return R.ok(updatedTaskVO);
    }

    @ApiOperation(value = "更改OTA升级任务状态", httpMethod = "PUT", notes = "更改OTA升级任务的状态")
    @PutMapping("/changeTaskStatus/{id}")
    public R<Boolean> changeTaskStatus(
            @ApiParam(value = "任务ID", required = true) @PathVariable("id") Long id,
            @ApiParam(value = "新任务状态（0：待处理，1：进行中，2：已完成，3：已取消）", required = true, allowableValues = "0,1,2,3") @RequestParam("status") Integer status) {
        log.info("更改任务状态 id:{}, 状态:{}", id, status);
        try {
            return R.ok(otaUpgradeTasksService.changeTaskStatus(id, status));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "删除OTA升级任务", httpMethod = "DELETE", notes = "通过其ID删除OTA升级任务")
    @DeleteMapping("/deleteOtaUpgradeTask/{id}")
    public R<Boolean> deleteOtaUpgradeTask(@ApiParam(value = "OTA升级任务ID", required = true) @PathVariable("id") Long id) {
        log.info("删除OTA升级任务 id: {}", id);
        try {
            return R.ok(otaUpgradeTasksService.deleteOtaUpgradeTask(id));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "批量删除OTA升级任务", httpMethod = "DELETE", notes = "通过它们的ID批量删除OTA升级任务")
    @DeleteMapping("/deleteOtaUpgradeTasks")
    public R<Boolean> deleteOtaUpgradeTasks(@ApiParam(value = "OTA升级任务ID", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除OTA升级任务 ids: {}", ids);
        try {
            boolean allDeleted = ids.stream().distinct().allMatch(id -> {
                try {
                    return otaUpgradeTasksService.deleteOtaUpgradeTask(id);
                } catch (ArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return R.ok(allDeleted);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 根据其ID获取OTA升级任务的详细信息，包括相关升级包信息。
     *
     * @param id 要检索的OTA升级任务的ID。
     * @return {@link OtaUpgradeTasksResultVO} OTA升级任务的详细信息。
     */
    @ApiOperation(value = "获取OTA升级任务详情", httpMethod = "GET", notes = "通过ID检索OTA升级任务的详细信息。")
    @GetMapping("/details/{id}")
    public R<OtaUpgradeTasksResultVO> getUpgradeTaskDetails(@ApiParam(value = "OTA升级任务的唯一标识符。", required = true) @PathVariable Long id) {
        try {
            return R.ok(otaUpgradeTasksService.getUpgradeTaskDetails(id));
        } catch (ArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 根据开始时间及结束时间执行ota升级任务，包括相关升级包信息。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return {@link OtaUpgradeTasksResultVO} OTA升级任务的详细信息。
     */
    @ApiOperation(value = " OTA远程升级", httpMethod = "GET", notes = "根据开始时间及结束时间执行ota升级任务，包括相关升级包信息。。")
    @GetMapping("/upgrade")
    public R<List<OtaUpgradeTasksResultVO>> otaUpgradeTasksExecute(@RequestParam("startTime") LocalDateTime startTime, @RequestParam("endTime") LocalDateTime endTime) {
        return R.ok(otaUpgradeTasksService.otaUpgradeTasksExecute(startTime, endTime));
    }
}
