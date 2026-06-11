package com.mqttsnet.thinglinks.anyuser.controller;

import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.service.DeviceActionService;
import com.mqttsnet.thinglinks.device.service.DeviceCommandService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.service.DeviceShadowService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceShadowPageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceCommandResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoQueryDeviceResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备相关开放接口（anyUser）
 * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
 *
 * @author mqttsnet
 * @date 2021-06-30
 * @create [2021-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/anyUser/deviceOpen")
@Tag(name = "anyUser-设备相关API")
public class DeviceOpenAnyUserController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceActionService deviceActionService;

    @Autowired
    private DeviceCommandService deviceCommandService;

    @Autowired
    private DeviceShadowService deviceShadowService;

    @Autowired
    private EchoService echoService;


    /**
     * 修改设备连接状态
     *
     * @param clientIdentifier 客户端标识
     * @param connectionStatus 新连接状态值
     * @return 更新结果
     */
    @Operation(summary = "修改设备连接状态", description = "根据客户端标识修改设备连接状态")
    @PutMapping("/updateDeviceConnectionStatus/{clientIdentifier}")
    public R<Boolean> updateDeviceConnectionStatus(@Parameter(description = "设备ID", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
                                                   @Parameter(description = "新连接状态值（0:未连接、1:在线、2:离线）", required = true, example = "0,1,2") @RequestParam("connectionStatus") Integer connectionStatus) {
        log.info("updateDeviceConnectionStatus clientIdentifier:{}, connectionStatus:{}", clientIdentifier, connectionStatus);
        ArgumentAssert.notBlank(clientIdentifier, "clientIdentifier Cannot be null");
        //根据设备标识解析出租户ID 例如：clientId: 1000000000000000001@tenantId
        String tenantId = Optional.ofNullable(clientIdentifier)
                .filter(StrUtil::isNotBlank)
                .map(id -> StrUtil.subAfter(id, ContextConstants.SPECIAL_CHARACTER, true))
                .orElse(ContextConstants.BUILT_IN_TENANT_ID_STR);

        ContextUtil.setTenantIdStr(tenantId);


        DeviceResultVO deviceResultVO = deviceService.findOneByClientId(clientIdentifier);
        ArgumentAssert.notNull(deviceResultVO, "设备不存在");

        return R.success(deviceService.updateDeviceConnectionStatusById(deviceResultVO.getId(), connectionStatus));
    }

    /**
     * 基于上游事件的连接状态变更(HLC CAS).
     * <p>
     * 仅当 device.last_status_event_hlc 严格小于入参 eventHlc 时才覆盖,
     * 防止异步 / 乱序事件回退状态;返回 false 表示 CAS 拒绝(过期事件).
     * 供 mqs DeviceConnectStatusSyncer 通过 Feign 调用.
     *
     * @param clientIdentifier 客户端标识
     * @param connectionStatus 目标连接状态
     * @param eventHlc         上游因果时钟 HLC,必须 &gt; 0
     * @return true=CAS 写入生效, false=过期事件被拒绝
     */
    @Operation(summary = "基于上游事件的连接状态变更(HLC CAS)",
            description = "用于上游事件流驱动的状态同步,event-time LWW CAS 保护")
    @PutMapping("/updateDeviceConnectionStatusByEvent/{clientIdentifier}")
    public R<Boolean> updateDeviceConnectionStatusByEvent(
            @Parameter(description = "客户端标识符", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
            @Parameter(description = "新连接状态值（0:未连接、1:在线、2:离线）", required = true) @RequestParam("connectionStatus") Integer connectionStatus,
            @Parameter(description = "上游因果时钟 HLC", required = true) @RequestParam("eventHlc") Long eventHlc) {
        log.info("updateDeviceConnectionStatusByEvent clientId:{} status:{} hlc:{}",
                clientIdentifier, connectionStatus, eventHlc);
        ArgumentAssert.notBlank(clientIdentifier, "clientIdentifier cannot be blank");
        ArgumentAssert.notNull(connectionStatus, "connectionStatus cannot be null");
        ArgumentAssert.isTrue(eventHlc != null && eventHlc > 0, "eventHlc must be > 0");

        String tenantId = Optional.ofNullable(clientIdentifier)
                .filter(StrUtil::isNotBlank)
                .map(id -> StrUtil.subAfter(id, ContextConstants.SPECIAL_CHARACTER, true))
                .orElse(ContextConstants.BUILT_IN_TENANT_ID_STR);
        ContextUtil.setTenantIdStr(tenantId);

        return R.success(deviceService.updateDeviceConnectionStatusByEvent(clientIdentifier, connectionStatus, eventHlc));
    }


    /**
     * （MQTT）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @Operation(summary = "（MQTT）协议新增子设备档案", description = "（MQTT）协议新增子设备档案")
    @PostMapping("/saveSubDeviceByMqtt")
    public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.success(deviceService.saveSubDeviceByMqtt(topoAddSubDeviceParam));
    }

    /**
     * （HTTP）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @Operation(summary = "北向API新增子设备档案", description = "北向API新增子设备档案")
    @PostMapping("/saveSubDeviceByNorthbound")
    public R<TopoAddDeviceResultVO> saveSubDeviceByNorthbound(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.success(deviceService.saveSubDeviceByNorthbound(topoAddSubDeviceParam));
    }

    /**
     * MQTT协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "（MQTT）协议修改子设备连接状态", description = "（MQTT）协议修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByMqtt")
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(
            @RequestBody @Parameter(description = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.updateSubDeviceConnectStatusByMqtt(topoUpdateSubDeviceStatusParam);
        return R.success(topoDeviceOperationResultVO);
    }

    /**
     * 北向API-修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "北向API修改子设备连接状态", description = "北向API-修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByNorthbound")
    @WebLog("北向API修改子设备连接状态")
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByNorthbound(
            @RequestBody @Parameter(description = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        try {
            log.info("updateSubDeviceConnectStatusByNorthbound param:{}", JSON.toJSONString(topoUpdateSubDeviceStatusParam));
            TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.updateSubDeviceConnectStatusByNorthbound(topoUpdateSubDeviceStatusParam);
            return R.success(topoDeviceOperationResultVO);
        } catch (Exception e) {
            log.error("修改子设备连接状态失败", e);
            return R.fail("修改子设备连接状态失败: " + e.getMessage());
        }
    }

    /**
     * MQTT协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "（MQTT）协议删除子设备", description = "（MQTT）协议删除子设备")
    @PutMapping("/deleteSubDeviceByMqtt")
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(@RequestBody @Parameter(description = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deleteSubDeviceByMqtt(topoDeleteSubDeviceParam);
        return R.success(topoDeviceOperationResultVO);
    }

    /**
     * 北向API-删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    @Operation(summary = "北向API删除子设备", description = "北向API-删除子设备")
    @PutMapping("/deleteSubDeviceByNorthbound")
    @WebLog("北向API删除子设备")
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByNorthbound(@RequestBody @Parameter(description = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        try {
            log.info("deleteSubDeviceByNorthbound param:{}", JSON.toJSONString(topoDeleteSubDeviceParam));
            TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deleteSubDeviceByNorthbound(topoDeleteSubDeviceParam);
            return R.success(topoDeviceOperationResultVO);
        } catch (Exception e) {
            log.error("删除子设备失败", e);
            return R.fail("删除子设备失败: " + e.getMessage());
        }
    }


    /**
     * MQTT协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Operation(summary = "（MQTT）协议数据上报", description = "（MQTT）协议数据上报")
    @PostMapping("/deviceDataReportByMqtt")
    public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(@RequestBody @Parameter(description = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deviceDataReportByMqtt(topoDeviceDataReportParam);
        return R.success(topoDeviceOperationResultVO);
    }

    /**
     * 北向API-设备数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Operation(summary = "北向API设备数据上报", description = "北向API-设备数据上报")
    @PostMapping("/deviceDataReportByNorthbound")
    @WebLog("北向API设备数据上报")
    public R<TopoDeviceOperationResultVO> deviceDataReportByNorthbound(@RequestBody @Parameter(description = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam) {
        try {
            log.info("deviceDataReportByNorthbound param:{}", JSON.toJSONString(topoDeviceDataReportParam));
            TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deviceDataReportByNorthbound(topoDeviceDataReportParam);
            return R.success(topoDeviceOperationResultVO);
        } catch (Exception e) {
            log.error("设备数据上报失败", e);
            return R.fail("设备数据上报失败: " + e.getMessage());
        }
    }

    /**
     * 新增设备动作
     *
     * @param deviceActionSaveVO 新增设备动作参数
     * @return {@link DeviceAction} 结果
     */
    @Operation(summary = "新增设备动作", description = "新增设备动作")
    @PostMapping("/saveDeviceAction")
    public R<DeviceAction> saveDeviceAction(@RequestBody @Parameter(description = "新增设备动作参数") DeviceActionSaveVO deviceActionSaveVO) {
        return R.success(deviceActionService.saveDeviceAction(deviceActionSaveVO));
    }

    /**
     * Creates a new device command entry in the database.
     *
     * @param deviceCommandSaveVO The device command data to be saved.
     * @return The saved device command data.
     */
    @Operation(summary = "Create Device Command", description = "Saves a new device command to the database.")
    @PostMapping("/saveDeviceCommand")
    @WebLog(value = "Save Device Command", request = false)
    public R<DeviceCommand> saveDeviceCommand(@RequestBody DeviceCommandSaveVO deviceCommandSaveVO) {
        DeviceCommand savedDeviceCommand = deviceCommandService.saveDeviceCommand(deviceCommandSaveVO);
        return R.success(savedDeviceCommand);
    }

    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @Operation(summary = "Query Device Information via MQTT Protocol", description = "Queries device information using the MQTT protocol")
    @PostMapping("/queryDeviceByMqtt")
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam) {
        return R.success(deviceService.queryDeviceByMqtt(topoQueryDeviceParam));
    }

    /**
     * 北向API-查询设备信息
     *
     * @param topoQueryDeviceParam 设备查询参数
     * @return {@link TopoQueryDeviceResultVO} 设备查询结果
     */
    @Operation(summary = "北向API查询设备信息", description = "北向API-查询设备信息")
    @PostMapping("/queryDeviceByNorthbound")
    @WebLog("北向API查询设备信息")
    public R<TopoQueryDeviceResultVO> queryDeviceByNorthbound(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam) {
        try {
            log.info("queryDeviceByNorthbound param:{}", JSON.toJSONString(topoQueryDeviceParam));
            return R.success(deviceService.queryDeviceByNorthbound(topoQueryDeviceParam));
        } catch (Exception e) {
            log.error("查询设备信息失败", e);
            return R.fail("查询设备信息失败: " + e.getMessage());
        }
    }

    /**
     * 上报设备心跳信息
     *
     * @param clientIdentifier 客户端标识符
     * @param heartbeatTime    心跳时间(毫秒时间戳)
     * @return 上报结果
     */
    @Operation(summary = "上报设备心跳信息", description = "上报设备心跳信息")
    @PutMapping(path = "/reportDeviceHeartbeat/{clientIdentifier}")
    public R<Boolean> reportDeviceHeartbeat(@Parameter(description = "客户端标识符", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
                                            @Parameter(description = "心跳时间(毫秒时间戳)", required = true) @RequestParam("heartbeatTime") Long heartbeatTime,
                                            @Parameter(description = "事件因果时钟 HLC,非空走 CAS 置在线") @RequestParam(value = "eventHlc", required = false) Long eventHlc) {
        try {
            log.info("reportDeviceHeartbeat clientIdentifier:{}, heartbeatTime:{}, eventHlc:{}", clientIdentifier, heartbeatTime, eventHlc);
            Boolean result = deviceService.reportDeviceHeartbeat(clientIdentifier, heartbeatTime, eventHlc);
            return R.success(result);
        } catch (Exception e) {
            log.error("上报设备心跳失败,clientIdentifier:{}", clientIdentifier, e);
            return R.fail("上报设备心跳失败");
        }
    }

    /**
     * 北向API-保存设备
     *
     * @param deviceSaveVO 设备保存参数
     * @return 保存的设备信息
     */
    @Operation(summary = "北向API保存设备", description = "北向API-保存设备")
    @PostMapping("/saveDeviceByNorthbound")
    public R<DeviceResultVO> saveDeviceByNorthbound(@RequestBody @Parameter(description = "设备保存参数") DeviceSaveVO deviceSaveVO) {
        try {
            log.info("saveDeviceByNorthbound deviceSaveVO:{}", JSON.toJSONString(deviceSaveVO));
            DeviceResultVO result = deviceService.saveDeviceByNorthbound(deviceSaveVO);
            echoService.action(result);
            return R.success(result);
        } catch (Exception e) {
            log.error("保存设备失败", e);
            return R.fail("保存设备失败: " + e.getMessage());
        }
    }

    /**
     * 北向API-根据设备标识查询设备详情
     *
     * @param deviceIdentification 设备标识
     * @return 设备详情信息
     */
    @Operation(summary = "北向API查询设备详情", description = "北向API-根据设备标识查询设备详情")
    @GetMapping("/getDeviceDetailByNorthbound/{deviceIdentification}")
    public R<DeviceDetailsResultVO> getDeviceDetailByNorthbound(@Parameter(description = "设备标识", required = true) @PathVariable("deviceIdentification") String deviceIdentification) {
        try {
            log.info("getDeviceDetailByNorthbound deviceIdentification:{}", deviceIdentification);
            DeviceDetailsResultVO result = deviceService.findOneByDeviceIdentification(deviceIdentification);
            echoService.action(result);
            return R.success(result);
        } catch (Exception e) {
            log.error("查询设备详情失败,deviceIdentification:{}", deviceIdentification, e);
            return R.fail("查询设备详情失败: " + e.getMessage());
        }
    }


    /**
     * 下发设备命令（串行和并行）
     *
     * @param commandWrapper 命令包装参数
     * @return 下发结果
     */
    @Operation(summary = "下发设备命令", description = "下发设备命令（串行和并行）")
    @PostMapping("/issueCommands")
    public R<List<DeviceCommandResultVO>> issueCommands(@RequestBody @Parameter(description = "命令包装参数") DeviceCommandWrapperParam commandWrapper) {
        try {
            log.info("issueCommands commandWrapper:{}", JSON.toJSONString(commandWrapper));
            List<DeviceCommandResultVO> results = deviceCommandService.processDeviceCommands(commandWrapper);
            echoService.action(results);
            return R.success(results);
        } catch (Exception e) {
            log.error("下发设备命令失败", e);
            return R.fail("下发设备命令失败: " + e.getMessage());
        }
    }


    /**
     * 北向API-下发设备命令
     *
     * @param commandWrapper 命令包装参数
     * @return 下发结果
     */
    @Operation(summary = "北向下发设备命令", description = "北向API-下发设备命令")
    @PostMapping("/issueCommandByNorthbound")
    public R<?> issueCommandByNorthbound(@RequestBody @Parameter(description = "命令包装参数") DeviceCommandWrapperParam commandWrapper) {
        try {
            log.info("issueCommandByNorthbound commandWrapper:{}", JSON.toJSONString(commandWrapper));
            deviceCommandService.processDeviceCommands(commandWrapper);
            return R.success();
        } catch (Exception e) {
            log.error("下发设备命令失败", e);
            return R.fail("下发设备命令失败: " + e.getMessage());
        }
    }

    /**
     * 北向API-查询设备影子
     *
     * @param deviceIdentification 设备标识（必填）
     * @param startTime            开始时间戳（选填）
     * @param endTime              结束时间戳（选填）
     * @param serviceCode          服务编码（选填）
     * @return 设备影子信息
     */
    @Operation(summary = "北向API查询设备影子", description = "北向API-查询设备影子信息")
    @GetMapping("/queryDeviceShadowByNorthbound")
    public R<ProductResultVO> queryDeviceShadowByNorthbound(
            @Parameter(description = "设备标识", required = true, example = "7939700746264577")
            @RequestParam(value = "deviceIdentification") String deviceIdentification,
            @Parameter(description = "开始时间戳（选填），格式：19位纳秒时间戳", example = "1622552643000000000")
            @RequestParam(value = "startTime", required = false) Long startTime,
            @Parameter(description = "结束时间戳（选填），格式：19位纳秒时间戳", example = "1622552643000000000")
            @RequestParam(value = "endTime", required = false) Long endTime,
            @Parameter(description = "服务编码（选填）", example = "serviceCode1")
            @RequestParam(value = "serviceCode", required = false) String serviceCode) {
        try {
            log.info("queryDeviceShadowByNorthbound deviceIdentification:{}, startTime:{}, endTime:{}, serviceCode:{}", deviceIdentification, startTime, endTime, serviceCode);
            DeviceShadowPageQuery deviceShadowPageQuery = new DeviceShadowPageQuery();
            deviceShadowPageQuery.setDeviceIdentification(deviceIdentification);
            deviceShadowPageQuery.setStartTime(startTime);
            deviceShadowPageQuery.setEndTime(endTime);
            deviceShadowPageQuery.setServiceCode(serviceCode);
            ProductResultVO productResultVO = deviceShadowService.queryDeviceShadow(deviceShadowPageQuery);
            echoService.action(productResultVO);
            return R.success(productResultVO);
        } catch (Exception e) {
            log.error("查询设备影子失败", e);
            return R.fail("查询设备影子失败: " + e.getMessage());
        }
    }

    /**
     * 北向API-修改设备状态
     *
     * @param deviceIdentification 设备标识
     * @param status               设备状态（0:未激活、1:已激活、2:已禁用）
     * @return 修改结果
     */
    @Operation(summary = "北向API修改设备状态", description = "北向API-修改设备状态")
    @PutMapping("/updateDeviceStatusByNorthbound/{deviceIdentification}")
    @WebLog("北向API修改设备状态")
    public R<Boolean> updateDeviceStatusByNorthbound(
            @Parameter(description = "设备标识", required = true) @PathVariable("deviceIdentification") String deviceIdentification,
            @Parameter(description = "设备状态（0:未激活、1:已激活、2:已禁用）", required = true) @RequestParam("status") Integer status) {
        try {
            log.info("updateDeviceStatusByNorthbound deviceIdentification:{}, status:{}", deviceIdentification, status);
            DeviceDetailsResultVO deviceDetails = deviceService.findOneByDeviceIdentification(deviceIdentification);
            if (deviceDetails == null) {
                return R.fail("设备不存在");
            }
            Boolean result = deviceService.updateDeviceStatus(deviceDetails.getId(), status);
            return R.success(result);
        } catch (Exception e) {
            log.error("修改设备状态失败,deviceIdentification:{}", deviceIdentification, e);
            return R.fail("修改设备状态失败: " + e.getMessage());
        }
    }

}
