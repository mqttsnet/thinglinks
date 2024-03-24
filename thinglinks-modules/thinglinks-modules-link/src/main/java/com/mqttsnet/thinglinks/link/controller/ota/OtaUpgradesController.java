package com.mqttsnet.thinglinks.link.controller.ota;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.exception.ArgumentException;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradesSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradesUpdateVO;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/otaUpgrades")
@Api(value = "OtaUpgrades", tags = "OTA升级包")
@Slf4j
public class OtaUpgradesController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private OtaUpgradesService otaUpgradesService;

    @ApiOperation(value = "保存OTA升级包", httpMethod = "POST", notes = "保存一个新的OTA升级包")
    @PostMapping("/saveUpgradePackage")
    public R<OtaUpgradesSaveVO> saveUpgradePackage(@Valid @RequestBody OtaUpgradesSaveVO saveVO) {
        // 从服务中保存OTA升级包
        OtaUpgradesSaveVO createdSaveVO = otaUpgradesService.saveUpgradePackage(saveVO);
        return R.ok(createdSaveVO);
    }

    @ApiOperation(value = "更新OTA升级包", httpMethod = "PUT", notes = "更新一个现有的OTA升级包")
    @PutMapping("/updateUpgradePackage")
    public R<?> updateUpgradePackage(@Valid @RequestBody OtaUpgradesUpdateVO updateVO) {
        // 从服务中保存OTA升级包
        OtaUpgradesUpdateVO otaUpgradesUpdateVO = otaUpgradesService.updateUpgradePackage(updateVO);
        return R.ok(otaUpgradesUpdateVO);
    }

    @ApiOperation(value = "更新OTA升级包状态", httpMethod = "PUT", notes = "更新OTA升级包的状态")
    @PutMapping("/updateOtaUpgradeStatus/{id}")
    public R<Boolean> updateOtaUpgradeStatus(
            @ApiParam(value = "包ID", required = true) @PathVariable("id") Long id,
            @ApiParam(value = "新状态值（1：启用，-1：禁用）", required = true) @RequestParam("status") Integer status) {
        // 记录信息
        log.info("更新OTA升级状态 id:{}, 状态:{}", id, status);
        try {
            // 返回更新状态的成功响应
            return R.ok(otaUpgradesService.updateOtaUpgradeStatus(id, status));
        } catch (ArgumentException e) {
            // 返回更新状态的失败响应
            return R.fail(e.getMessage());
        }

    }

    @ApiOperation(value = "删除OTA升级包", httpMethod = "DELETE", notes = "通过其ID删除一个OTA升级包")
    @DeleteMapping("/deleteOtaUpgrade/{id}")
    public R<Boolean> deleteOtaUpgrade(@ApiParam(value = "OTA升级包ID", required = true) @PathVariable("id") Long id) {
        log.info("删除OTA升级包 id: {}", id);
        try {
            // 返回删除操作的成功响应
            return R.ok(otaUpgradesService.deleteOtaUpgrade(id));
        } catch (ArgumentException e) {
            // 返回删除操作的失败响应
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "批量删除OTA升级包", httpMethod = "DELETE", notes = "通过它们的ID批量删除OTA升级包")
    @DeleteMapping("/deleteOtaUpgrades")
    public R<Boolean> deleteOtaUpgrades(@ApiParam(value = "OTA升级包ID", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除OTA升级包 ids: {}", ids);
        try {
            boolean allDeleted = ids.stream().distinct().allMatch(id -> {
                try {
                    return otaUpgradesService.deleteOtaUpgrade(id);
                } catch (ArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return R.ok(allDeleted);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

}
