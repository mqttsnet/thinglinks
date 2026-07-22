package com.mqttsnet.thinglinks.inner.controller;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.ota.enumeration.OtaTaskRecordAppConfirmStatusEnum;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTaskExecutionService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTasksService;
import com.mqttsnet.thinglinks.ota.vo.param.DeviceOtaUpgradeAppConfirmationParam;
import com.mqttsnet.thinglinks.ota.vo.result.DeviceOtaUpgradeAppConfirmationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaListUpgradeableVersionsResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * OTA相关内部接口（inner）
 * Feign 服务间 RPC(Nacos 直连、不过网关)：透传 TenantId、无需 Token；注意设置 ContextUtil.setTenantId(tenantId)。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/12/15
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/inner/otaOpen")
@Tag(name = "inner-OTA相关API")
public class OtaOpenInnerController {

    private final OtaUpgradeTaskExecutionService otaUpgradeTaskExecutionService;
    private final OtaUpgradeTasksService otaUpgradeTasksService;


    /**
     * Executes the OTA (Over-the-Air) upgrade tasks for a specific tenant identified by the tenant ID.
     * <p>
     * This method is responsible for initiating the execution of OTA upgrade tasks for a given tenant.
     * It requires the tenant's ID as a parameter. The method returns a list of results containing the details
     * of the executed OTA upgrade tasks.
     * </p>
     *
     * @param tenantId The ID of the tenant.
     */
    @Operation(summary = "执行OTA升级任务", description = "Executes the OTA upgrade tasks for a specific tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true)
    })
    @PostMapping("/otaUpgradeTasksExecute")
    public R<?> otaUpgradeTasksExecute(@RequestParam("tenantId") Long tenantId) {
        ArgumentAssert.notNull(tenantId, "tenantId Cannot be null");
        log.info("Executing OTA Upgrade Tasks - Tenant ID: {}", tenantId);
        ContextUtil.setTenantId(tenantId);
        otaUpgradeTaskExecutionService.otaUpgradeTasksExecute(tenantId);
        return R.success();
    }

    /**
     * APP确认升级
     * <p>
     * 处理APP对升级任务的确认操作，通过状态机管理确认流程
     * 只有待确认状态的记录才能进行确认操作，确认后状态变更为已确认或已拒绝
     * </p>
     *
     * @param param APP确认升级参数
     * @return {@link DeviceOtaUpgradeAppConfirmationResultVO}  确认结果
     */
    @Operation(summary = "APP确认升级", description = "处理APP对升级任务的确认操作，通过状态机管理确认流程")
    @PostMapping("/otaUpgradeAppConfirmation")
    public R<DeviceOtaUpgradeAppConfirmationResultVO> otaUpgradeAppConfirmation(@RequestBody @Validated DeviceOtaUpgradeAppConfirmationParam param) {
        log.info("otaUpgradeAppConfirmation...OTA升级任务APP确认升级...param: {}", JSON.toJSONString(param));
        ContextUtil.setTenantId(param.getTenantId());
        try {
            return R.success(otaUpgradeTaskExecutionService.otaUpgradeAppConfirmation(param.getTaskId(), param.getDeviceIdentificationList(), param.getAppConfirmationStatus() ? OtaTaskRecordAppConfirmStatusEnum.CONFIRMED : OtaTaskRecordAppConfirmStatusEnum.REJECTED));
        } catch (BizException be) {
            return R.fail(be);
        } catch (Exception e) {
            log.error("OTA升级任务APP确认升级，系统异常: {}", e.getMessage(), e);
            return R.fail();
        }
    }

    /**
     * MQTT协议保存OTA升级记录
     *
     * @param topoOtaCommandResponseParam OTA命令响应参数
     * @return {@link R<TopoOtaCommandResponseParam>} 保存的OTA升级记录
     */
    @Operation(summary = "MQTT协议保存OTA升级记录", description = "MQTT协议保存OTA升级记录")
    @PostMapping("/saveOtaUpgradeRecordByMqtt")
    public R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        try {
            log.info("saveOtaUpgradeRecordByMqtt param:{}", JSON.toJSONString(topoOtaCommandResponseParam));
            TopoOtaCommandResponseParam savedRecord = otaUpgradeTasksService.saveOtaUpgradeRecordByMqtt(topoOtaCommandResponseParam);
            return R.success(savedRecord);
        } catch (Exception e) {
            log.error("保存OTA升级记录失败", e);
            return R.fail("Error saving OTA upgrade record: " + e.getMessage());
        }
    }

    /**
     * 北向API-保存OTA升级记录
     *
     * @param topoOtaCommandResponseParam OTA命令响应参数
     * @return {@link R<TopoOtaCommandResponseParam>} 保存的OTA升级记录
     */
    @Operation(summary = "北向API保存OTA升级记录", description = "北向API-保存OTA升级记录")
    @PostMapping("/saveOtaUpgradeRecordByNorthbound")
    @WebLog("北向API保存OTA升级记录")
    public R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByNorthbound(@Valid @RequestBody TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        try {
            log.info("saveOtaUpgradeRecordByNorthbound param:{}", JSON.toJSONString(topoOtaCommandResponseParam));
            TopoOtaCommandResponseParam savedRecord = otaUpgradeTasksService.saveUpgradeRecordByNorthbound(topoOtaCommandResponseParam);
            return R.success(savedRecord);
        } catch (Exception e) {
            log.error("保存OTA升级记录失败", e);
            return R.fail("保存OTA升级记录失败: " + e.getMessage());
        }
    }

    /**
     * MQTT协议拉取OTA信息
     *
     * @param topoOtaPullParam OTA拉取参数
     * @return {@link R<TopoOtaPullResponseParam>} OTA拉取响应结果
     */
    @Operation(summary = "MQTT协议拉取OTA信息", description = "MQTT协议拉取OTA信息")
    @PostMapping("/otaPullByMqtt")
    public R<TopoOtaPullResponseParam> otaPullByMqtt(@Valid @RequestBody TopoOtaPullParam topoOtaPullParam) {
        try {
            log.info("otaPullByMqtt param:{}", JSON.toJSONString(topoOtaPullParam));
            TopoOtaPullResponseParam savedResponse = otaUpgradeTasksService.otaPullByMqtt(topoOtaPullParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA拉取软固件信息失败", e);
            return R.fail("Error processing OTA pull event: " + e.getMessage());
        }
    }

    /**
     * 北向API-OTA拉取软固件信息
     *
     * @param topoOtaPullParam OTA拉取参数
     * @return {@link R<TopoOtaPullResponseParam>} OTA拉取响应结果
     */
    @Operation(summary = "北向API-OTA拉取软固件信息", description = "北向API-OTA拉取软固件信息")
    @PostMapping("/otaPullByNorthbound")
    @WebLog("北向API-OTA拉取软固件信息")
    public R<TopoOtaPullResponseParam> otaPullByNorthbound(@Valid @RequestBody TopoOtaPullParam topoOtaPullParam) {
        try {
            log.info("otaPullByNorthbound param:{}", JSON.toJSONString(topoOtaPullParam));
            TopoOtaPullResponseParam savedResponse = otaUpgradeTasksService.otaPullByNorthbound(topoOtaPullParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA拉取软固件信息失败", e);
            return R.fail("OTA拉取软固件信息失败: " + e.getMessage());
        }
    }

    /**
     * MQTT协议上报OTA信息
     *
     * @param topoOtaReportParam OTA上报参数
     * @return {@link R<TopoOtaReportResponseParam>} OTA上报响应结果
     */
    @Operation(summary = "MQTT协议上报OTA信息", description = "MQTT协议上报OTA信息")
    @PostMapping("/otaReportByMqtt")
    public R<TopoOtaReportResponseParam> otaReportByMqtt(@Valid @RequestBody TopoOtaReportParam topoOtaReportParam) {
        try {
            log.info("otaReportByMqtt param:{}", JSON.toJSONString(topoOtaReportParam));
            TopoOtaReportResponseParam savedResponse = otaUpgradeTasksService.otaReportByMqtt(topoOtaReportParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA上报软固件版本失败", e);
            return R.fail("Error processing OTA report event: " + e.getMessage());
        }
    }

    /**
     * 北向API-OTA上报软固件版本
     *
     * @param topoOtaReportParam OTA上报参数
     * @return {@link R<TopoOtaReportResponseParam>} OTA上报响应结果
     */
    @Operation(summary = "北向API-OTA上报软固件版本", description = "北向API-OTA上报软固件版本")
    @PostMapping("/otaReportByNorthbound")
    @WebLog("北向API-OTA上报软固件版本")
    public R<TopoOtaReportResponseParam> otaReportByNorthbound(@Valid @RequestBody TopoOtaReportParam topoOtaReportParam) {
        try {
            log.info("otaReportByNorthbound param:{}", JSON.toJSONString(topoOtaReportParam));
            TopoOtaReportResponseParam savedResponse = otaUpgradeTasksService.otaReportByNorthbound(topoOtaReportParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA上报软固件版本失败", e);
            return R.fail("OTA上报软固件版本失败: " + e.getMessage());
        }
    }

    /**
     * MQTT协议OTA读取响应
     *
     * @param topoOtaReadResponseParam OTA读取响应参数
     * @return {@link R<?>} OTA读取响应结果
     */
    @Operation(summary = "MQTT协议OTA读取响应", description = "MQTT协议OTA读取响应")
    @PostMapping("/otaReadResponseByMqtt")
    public R<?> otaReadResponseByMqtt(@Valid @RequestBody TopoOtaReadResponseParam topoOtaReadResponseParam) {
        try {
            log.info("otaReadResponseByMqtt param:{}", JSON.toJSONString(topoOtaReadResponseParam));
            otaUpgradeTasksService.otaReadResponseByMqtt(topoOtaReadResponseParam);
            return R.success();
        } catch (Exception e) {
            log.error("OTA读取设备软固件版本信息响应失败", e);
            return R.fail("Error processing OTA read response event: " + e.getMessage());
        }
    }

    /**
     * 北向API-OTA读取设备软固件版本信息响应
     *
     * @param topoOtaReadResponseParam OTA读取响应参数
     * @return {@link R<?>} OTA读取响应结果
     */
    @Operation(summary = "北向API-OTA读取设备软固件版本信息响应", description = "北向API-OTA读取设备软固件版本信息响应")
    @PostMapping("/otaReadResponseByNorthbound")
    @WebLog("北向API-OTA读取设备软固件版本信息响应")
    public R<?> otaReadResponseByNorthbound(@Valid @RequestBody TopoOtaReadResponseParam topoOtaReadResponseParam) {
        try {
            log.info("otaReadResponseByNorthbound param:{}", JSON.toJSONString(topoOtaReadResponseParam));
            otaUpgradeTasksService.otaReadResponseByNorthbound(topoOtaReadResponseParam);
            return R.success();
        } catch (Exception e) {
            log.error("OTA读取设备软固件版本信息响应失败", e);
            return R.fail("OTA读取设备软固件版本信息响应失败: " + e.getMessage());
        }
    }

    /**
     * 北向API-OTA获取可升级版本列表
     *
     * @param deviceIdentification 设备标识
     * @param packageType          包类型
     * @return {@link R<TopoOtaListUpgradeableVersionsResponseParam>} 可升级版本列表响应
     */
    @Operation(summary = "北向API-OTA获取可升级版本列表", description = "北向API-OTA获取可升级版本列表")
    @GetMapping("/getAvailableUpgradeVersions")
    @WebLog("北向API-OTA获取可升级版本列表")
    public R<TopoOtaListUpgradeableVersionsResponseParam> getAvailableUpgradeVersionsByNorthbound(
            @Parameter(description = "设备标识") @RequestParam("deviceIdentification") String deviceIdentification,
            @Parameter(description = "包类型") @RequestParam("packageType") Integer packageType) {
        try {
            log.info("getAvailableUpgradeVersionsByNorthbound params: deviceIdentification={}, packageType={}",
                    deviceIdentification, packageType);
            TopoOtaListUpgradeableVersionsResponseParam response = otaUpgradeTasksService.getAvailableUpgradeVersionsByNorthbound(
                    deviceIdentification, packageType);
            return R.success(response);
        } catch (Exception e) {
            log.error("OTA获取可升级版本列表失败", e);
            return R.fail("OTA获取可升级版本列表失败: " + e.getMessage());
        }
    }

}
