package com.mqttsnet.thinglinks.link.api;

import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoQueryDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.factory.RemoteDeviceFallbackFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 设备管理服务
 *
 * @author shisen
 */
@FeignClient(contextId = "remoteDeviceService", value = ServiceNameConstants.THINGLINKS_LINK, fallbackFactory = RemoteDeviceFallbackFactory.class)
public interface RemoteDeviceService {

    /**
     * 更新设备在线状态
     *
     * @param device
     * @return
     */
    @PutMapping("/device/updateConnectStatusByClientId")
    public R updateConnectStatusByClientId(@RequestBody Device device);


    /**
     * 客户端身份认证
     *
     * @param params
     * @return
     */
    @PostMapping("/device/clientAuthentication")
    public R<Boolean> clientAuthentication(@RequestBody Map<String, Object> params);

    /**
     * 查询产品下的设备标识
     *
     * @param productIdentification
     * @return
     */
    @GetMapping("/device/selectByProductIdentification/{productIdentification}")
    public R<?> selectByProductIdentification(@PathVariable("productIdentification") String productIdentification);


    /**
     * 查询产品下的设备标识
     *
     * @param productIdentification
     * @return
     */
    @GetMapping("/device/selectByProductIdentificationAndDeviceIdentification/{productIdentification}/{deviceIdentification}")
    public R<Device> selectByProductIdentificationAndDeviceIdentification(@PathVariable("productIdentification") String productIdentification,
                                                                          @PathVariable("deviceIdentification") String deviceIdentification);

    /**
     * 根据客户端标识获取设备信息
     *
     * @param clientId
     * @return
     */
    @PostMapping("/device/findOneByClientId")
    public R<Device> findOneByClientId(String clientId);

    /**
     * 根据产品标识获取产品所有关联设备
     *
     * @param productIdentification
     * @return
     */
    @GetMapping("/device/selectAllByProductIdentification/{productIdentification}")
    public R<?> selectAllByProductIdentification(@PathVariable("productIdentification") String productIdentification);

    @PostMapping("/device/selectDeviceByDeviceIdentificationList")
    public R<?> selectDeviceByDeviceIdentificationList(@RequestBody List<String> deviceIdentificationList);

    /**
     * （MQTT）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（MQTT）协议新增子设备档案", httpMethod = "POST", notes = "（MQTT）协议新增子设备档案")
    @PostMapping("/saveSubDeviceByMqtt")
    R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * （HTTP）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（HTTP）协议新增子设备档案", httpMethod = "POST", notes = "（HTTP）协议新增子设备档案")
    @PostMapping("/saveSubDeviceByHttp")
    public R<TopoAddDeviceResultVO> saveSubDeviceByHttp(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam);


    /**
     * MQTT协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议修改子设备连接状态", httpMethod = "PUT", notes = "（MQTT）协议修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByMqtt")
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(@RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);


    /**
     * HTTP协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议修改子设备连接状态", httpMethod = "PUT", notes = "（HTTP）协议修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByHttp")
    R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByHttp(@RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * MQTT协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议删除子设备", httpMethod = "PUT", notes = "（MQTT）协议删除子设备")
    @PutMapping("/deleteSubDeviceByMqtt")
    R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(@RequestBody @ApiParam(value = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * HTTP协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议删除子设备", httpMethod = "PUT", notes = "（HTTP）协议删除子设备")
    @PutMapping("/deleteSubDeviceByHttp")
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
    @PostMapping("/deviceDataReportByHttp")
    R<TopoDeviceOperationResultVO> deviceDataReportByHttp(@RequestBody @ApiParam(value = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam);

    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via MQTT Protocol", httpMethod = "POST", notes = "Queries device information using the MQTT protocol")
    @PostMapping("/queryDeviceByMqtt")
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);

    /**
     * Queries device information using the HTTP protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via HTTP Protocol", httpMethod = "POST", notes = "Queries device information using the HTTP protocol")
    @PostMapping("/queryDeviceByHttp")
    public R<TopoQueryDeviceResultVO> queryDeviceByHttp(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam);


    /**
     * Receives and saves a new OTA upgrade record from an MQTT message. This endpoint
     * captures the command response parameters from the MQTT message body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via MQTT.
     * @return {@link R<OtaCommandResponseParam>} A response entity containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record", httpMethod = "POST", notes = "Saves a new OTA upgrade record from MQTT message data.")
    @PostMapping("/saveUpgradeRecordByMqtt")
    public R<OtaCommandResponseParam> saveUpgradeRecordByMqtt(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam);


    /**
     * Receives and saves a new OTA upgrade record from an HTTP request. This endpoint
     * captures the command response parameters from the request body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via HTTP.
     * @return {@link R<OtaCommandResponseParam>} A response wrapper containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record via HTTP", httpMethod = "POST", notes = "Saves a new OTA upgrade record from HTTP request data.")
    @PostMapping("/saveUpgradeRecordByHttp")
    public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam);



}
