package com.mqttsnet.thinglinks.link.api.inner;

import java.util.List;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.link.api.inner.hystrix.DeviceOpenInnerApiFallback;
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
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: thinglinks-cloud
 * @description: 设备-开放接口API
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:35
 **/
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-link-server}",
        fallback = DeviceOpenInnerApiFallback.class, path = "/inner/deviceOpen")
public interface DeviceOpenInnerApi {

    @Operation(summary = "修改设备连接状态", description = "根据客户端标识修改设备连接状态（无条件覆盖,供运维/后台/非事件驱动场景使用）")
    @PutMapping("/updateDeviceConnectionStatus/{clientIdentifier}")
    R<Boolean> updateDeviceConnectionStatus(@Parameter(description = "客户端标识符", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
                                            @Parameter(description = "新连接状态值（0:未连接、1:在线、2:离线）", required = true, example = "0,1,2") @RequestParam("connectionStatus") Integer connectionStatus);

    @Operation(summary = "基于上游事件的连接状态变更(HLC CAS)",
            description = "基于上游因果时钟 HLC 做 event-time LWW CAS,防止异步/乱序事件回退状态;返回 false 表示事件过期被拒绝")
    @PutMapping("/updateDeviceConnectionStatusByEvent/{clientIdentifier}")
    R<Boolean> updateDeviceConnectionStatusByEvent(
            @Parameter(description = "客户端标识符", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
            @Parameter(description = "新连接状态值（0:未连接、1:在线、2:离线）", required = true, example = "0,1,2") @RequestParam("connectionStatus") Integer connectionStatus,
            @Parameter(description = "上游因果时钟 HLC,必须 > 0", required = true) @RequestParam("eventHlc") Long eventHlc);


    /**
     * （MQTT）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @Operation(summary = "（MQTT）协议新增子设备档案", description = "（MQTT）协议新增子设备档案")
    @PostMapping(path = "/saveSubDeviceByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * （HTTP）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @Operation(summary = "北向API新增子设备档案", description = "北向API新增子设备档案")
    @PostMapping(path = "/saveSubDeviceByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoAddDeviceResultVO> saveSubDeviceByNorthbound(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * MQTT协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "（MQTT）协议修改子设备连接状态", description = "（MQTT）协议修改子设备连接状态")
    @PutMapping(path = "/updateSubDeviceConnectStatusByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(@RequestBody @Parameter(description = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);


    /**
     * 北向API-修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "北向API修改子设备连接状态", description = "北向API-修改子设备连接状态")
    @PutMapping(path = "/updateSubDeviceConnectStatusByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByNorthbound(@RequestBody @Parameter(description = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * MQTT协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @Operation(summary = "（MQTT）协议删除子设备", description = "（MQTT）协议删除子设备")
    @PutMapping(path = "/deleteSubDeviceByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(@RequestBody @Parameter(description = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * 北向API-删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 删除结果
     */
    @Operation(summary = "北向API删除子设备", description = "北向API-删除子设备")
    @PutMapping(path = "/deleteSubDeviceByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> deleteSubDeviceByNorthbound(@RequestBody @Parameter(description = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * MQTT协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Operation(summary = "（MQTT）协议数据上报", description = "（MQTT）协议数据上报")
    @PostMapping(path = "/deviceDataReportByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(@RequestBody @Parameter(description = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam);

    /**
     * 北向API-设备数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Operation(summary = "北向API设备数据上报", description = "北向API-设备数据上报")
    @PostMapping(path = "/deviceDataReportByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoDeviceOperationResultVO> deviceDataReportByNorthbound(@RequestBody @Parameter(description = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam);


    /**
     * 新增设备动作
     *
     * @param deviceActionSaveVO 新增设备动作参数
     * @return {@link DeviceAction} 结果
     */
    @Operation(summary = "新增设备动作", description = "新增设备动作")
    @PostMapping(path = "/saveDeviceAction", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<DeviceAction> saveDeviceAction(@RequestBody @Parameter(description = "新增设备动作参数") DeviceActionSaveVO deviceActionSaveVO);


    /**
     * Creates a new device command entry in the database.
     *
     * @param deviceCommandSaveVO The device command data to be saved.
     * @return The saved device command data.
     */
    @Operation(summary = "Create Device Command", description = "Saves a new device command to the database.")
    @PostMapping(path = "/saveDeviceCommand", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<DeviceCommand> saveDeviceCommand(@RequestBody DeviceCommandSaveVO deviceCommandSaveVO);


    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @Operation(summary = "Query Device Information via MQTT Protocol", description = "Queries device information using the MQTT protocol")
    @PostMapping(path = "/queryDeviceByMqtt", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoQueryDeviceResultVO> queryDeviceByMqtt(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);

    /**
     * 北向API-查询设备信息
     *
     * @param topoQueryDeviceParam 设备查询参数
     * @return {@link TopoQueryDeviceResultVO} 设备查询结果
     */
    @Operation(summary = "北向API查询设备信息", description = "北向API-查询设备信息")
    @PostMapping(path = "/queryDeviceByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<TopoQueryDeviceResultVO> queryDeviceByNorthbound(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);


    /**
     * 上报设备心跳信息
     *
     * @param clientIdentifier 客户端标识符
     * @return 上报结果
     */
    @Operation(summary = "上报设备心跳信息", description = "上报设备心跳信息")
    @PutMapping(path = "/reportDeviceHeartbeat/{clientIdentifier}")
    R<Boolean> reportDeviceHeartbeat(@Parameter(description = "客户端标识符", required = true) @PathVariable("clientIdentifier") String clientIdentifier,
                                     @Parameter(description = "心跳时间", required = true) @RequestParam("heartbeatTime") Long heartbeatTime,
                                     @Parameter(description = "事件因果时钟 HLC,非空走 CAS 置在线") @RequestParam(value = "eventHlc", required = false) Long eventHlc);

    /**
     * 下发设备命令（串行和并行）
     *
     * @param commandWrapper 命令包装参数
     * @return {@link R<List<DeviceCommandResultVO>>} 下发结果
     */
    @Operation(summary = "下发设备命令", description = "下发设备命令（串行和并行）")
    @PostMapping(path = "/issueCommands", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<List<DeviceCommandResultVO>> issueCommands(@Parameter(description = "命令包装参数") DeviceCommandWrapperParam commandWrapper);


    /**
     * 北向API-保存设备
     *
     * @param deviceSaveVO 设备保存参数
     * @return 保存的设备信息
     */
    @Operation(summary = "北向API保存设备", description = "北向API-保存设备")
    @PostMapping(path = "/saveDeviceByNorthbound", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<DeviceResultVO> saveDeviceByNorthbound(@RequestBody @Parameter(description = "设备保存参数") DeviceSaveVO deviceSaveVO);

    /**
     * 北向API-根据设备标识查询设备详情
     *
     * @param deviceIdentification 设备标识
     * @return 设备详情信息
     */
    @Operation(summary = "北向API查询设备详情", description = "北向API-根据设备标识查询设备详情")
    @GetMapping(path = "/getDeviceDetailByNorthbound/{deviceIdentification}")
    R<DeviceDetailsResultVO> getDeviceDetailByNorthbound(@Parameter(description = "设备标识", required = true) @PathVariable("deviceIdentification") String deviceIdentification);
    /**
     * 北向API-查询设备影子
     *
     * @param deviceIdentification 设备标识（必填）
     * @param startTime           开始时间戳（选填）
     * @param endTime             结束时间戳（选填）
     * @param serviceCode         服务编码（选填）
     * @return {@link R<ProductResultVO>} 设备影子信息
     */
    @Operation(summary = "北向API查询设备影子", description = "北向API-查询设备影子信息")
    @GetMapping(path = "/queryDeviceShadowByNorthbound")
    R<ProductResultVO> queryDeviceShadowByNorthbound(
            @Parameter(description = "设备标识", required = true) @RequestParam("deviceIdentification") String deviceIdentification,
            @Parameter(description = "开始时间戳（选填）") @RequestParam(value = "startTime", required = false) Long startTime,
            @Parameter(description = "结束时间戳（选填）") @RequestParam(value = "endTime", required = false) Long endTime,
            @Parameter(description = "服务编码（选填）") @RequestParam(value = "serviceCode", required = false) String serviceCode);

    /**
     * 北向API-修改设备状态
     *
     * @param deviceIdentification 设备标识
     * @param status               设备状态（0:未激活、1:已激活、2:已禁用）
     * @return {@link R<Boolean>} 修改结果
     */
    @Operation(summary = "北向API修改设备状态", description = "北向API-修改设备状态")
    @PutMapping(path = "/updateDeviceStatusByNorthbound/{deviceIdentification}")
    R<Boolean> updateDeviceStatusByNorthbound(
            @Parameter(description = "设备标识", required = true) @PathVariable("deviceIdentification") String deviceIdentification,
            @Parameter(description = "设备状态（0:未激活、1:已激活、2:已禁用）", required = true) @RequestParam("status") Integer status);
}
