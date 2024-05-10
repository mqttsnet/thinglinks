
package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.device.vo.result.TopoQueryDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceOpenAnyFallbackFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Map;

/**
 * 设备管理开放服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteDeviceOpenService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceOpenAnyFallbackFactory.class)
public interface RemoteDeviceOpenAnyService {


    /**
     * 客户端身份认证
     *
     * @param params
     * @return
     */
    @PostMapping("/deviceOpenAny/clientAuthentication")
    public R<Boolean> clientAuthentication(@RequestBody Map<String, Object> params);

    /**
     * （MQTT）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（MQTT）协议新增子设备档案", httpMethod = "POST", notes = "（MQTT）协议新增子设备档案")
    @PostMapping("/deviceOpenAny/saveSubDeviceByMqtt")
    R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * （HTTP）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（HTTP）协议新增子设备档案", httpMethod = "POST", notes = "（HTTP）协议新增子设备档案")
    @PostMapping("/deviceOpenAny/saveSubDeviceByHttp")
    public R<TopoAddDeviceResultVO> saveSubDeviceByHttp(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * MQTT协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议修改子设备连接状态", httpMethod = "PUT", notes = "（MQTT）协议修改子设备连接状态")
    @PutMapping("/deviceOpenAny/updateSubDeviceConnectStatusByMqtt")
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(@RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);


    /**
     * HTTP协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议修改子设备连接状态", httpMethod = "PUT", notes = "（HTTP）协议修改子设备连接状态")
    @PutMapping("/deviceOpenAny/updateSubDeviceConnectStatusByHttp")
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByHttp(@RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * MQTT协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议删除子设备", httpMethod = "PUT", notes = "（MQTT）协议删除子设备")
    @PutMapping("/deviceOpenAny/deleteSubDeviceByMqtt")
    R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(@RequestBody @ApiParam(value = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * HTTP协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议删除子设备", httpMethod = "PUT", notes = "（HTTP）协议删除子设备")
    @PutMapping("/deviceOpenAny/deleteSubDeviceByHttp")
    R<TopoDeviceOperationResultVO> deleteSubDeviceByHttp(@RequestBody @ApiParam(value = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);

    /**
     * MQTT协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @ApiOperation(value = "（MQTT）协议数据上报", httpMethod = "PUT", notes = "（MQTT）协议数据上报")
    @PostMapping("/deviceDataReportByMqtt")
    R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(@RequestBody @ApiParam(value = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam);

    /**
     * HTTP协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @ApiOperation(value = "（HTTP）协议数据上报", httpMethod = "PUT", notes = "（HTTP）协议数据上报")
    @PostMapping("/deviceOpenAny/deviceDataReportByHttp")
    R<TopoDeviceOperationResultVO> deviceDataReportByHttp(@RequestBody @ApiParam(value = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam);

    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via MQTT Protocol", httpMethod = "POST", notes = "Queries device information using the MQTT protocol")
    @PostMapping("/deviceOpenAny/queryDeviceByMqtt")
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);

    /**
     * Queries device information using the HTTP protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via HTTP Protocol", httpMethod = "POST", notes = "Queries device information using the HTTP protocol")
    @PostMapping("/deviceOpenAny/queryDeviceByHttp")
    public R<TopoQueryDeviceResultVO> queryDeviceByHttp(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);


    /**
     * Receives and saves a new OTA upgrade record from an MQTT message. This endpoint
     * captures the command response parameters from the MQTT message body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via MQTT.
     * @return {@link R< OtaCommandResponseParam >} A response entity containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record", httpMethod = "POST", notes = "Saves a new OTA upgrade record from MQTT message data.")
    @PostMapping("/deviceOpenAny/saveUpgradeRecordByMqtt")
    public R<OtaCommandResponseParam> saveUpgradeRecordByMqtt(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam);


    /**
     * Receives and saves a new OTA upgrade record from an HTTP request. This endpoint
     * captures the command response parameters from the request body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via HTTP.
     * @return {@link R<OtaCommandResponseParam>} A response wrapper containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record via HTTP", httpMethod = "POST", notes = "Saves a new OTA upgrade record from HTTP request data.")
    @PostMapping("/deviceOpenAny/saveUpgradeRecordByHttp")
    public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam);


}
