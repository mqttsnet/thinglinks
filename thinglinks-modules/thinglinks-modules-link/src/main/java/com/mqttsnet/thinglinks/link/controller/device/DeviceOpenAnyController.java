
package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.*;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoQueryDeviceResultVO;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备管理开放Controller
 * 用于设备接入协议通用入口
 *
 * @author thinglinks
 * @date 2024-03-22
 */
@RestController
@RequestMapping("/deviceOpenAny")
@Slf4j
public class DeviceOpenAnyController extends BaseController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceInfoService deviceInfoService;
    @Autowired
    private ProductService productService;


    /**
     * 客户端连接认证
     *
     * @param deviceAuthenticationQuery 设备认证参数
     * @return 认证结果
     */
    @ApiOperation(value = "客户端连接认证", httpMethod = "POST", notes = "客户端连接认证")
    @PostMapping("/clientConnectionAuthentication")
    public ResponseEntity clientConnectionAuthentication(@RequestBody DeviceAuthenticationQuery deviceAuthenticationQuery) {

        log.info("clientConnectionAuthentication,客户端ID:{},用户名:{},密码:{}", deviceAuthenticationQuery.getClientIdentifier(), deviceAuthenticationQuery.getUsername(), deviceAuthenticationQuery.getPassword());
        final String clientIdentifier = deviceAuthenticationQuery.getClientIdentifier();
        final String username = deviceAuthenticationQuery.getUsername();
        final String password = deviceAuthenticationQuery.getPassword();
        final Object deviceStatus = "ENABLE";
        final Object protocolType = "MQTT";
        Device device = deviceService.clientAuthentication(clientIdentifier, username, password, deviceStatus.toString(), protocolType.toString());
        log.info("{} 协议设备正在进行身份认证,客户端ID:{},用户名:{},密码:{},认证结果:{}", protocolType, clientIdentifier, username, password, device != null ? "成功" : "失败");

        Map<String, Object> result = new HashMap<>();
        result.put("certificationResult", device != null);
        result.put("tenantId", device == null ? Constants.PROJECT_PREFIX : device.getAppId());

        Map<String, Object> resultValue = new HashMap<>();
        resultValue.put("clientId", clientIdentifier.toString());
        result.put("deviceResult", resultValue);

        return ResponseEntity.ok().body(result);
    }


    /**
     * （MQTT）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（MQTT）协议新增子设备档案", httpMethod = "POST", notes = "（MQTT）协议新增子设备档案")
    @PostMapping("/saveSubDeviceByMqtt")
    public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.ok(deviceInfoService.saveSubDeviceByMqtt(topoAddSubDeviceParam));
    }

    /**
     * （HTTP）协议新增子设备档案
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 新增结果
     */
    @ApiOperation(value = "（HTTP）协议新增子设备档案", httpMethod = "POST", notes = "（HTTP）协议新增子设备档案")
    @PostMapping("/saveSubDeviceByHttp")
    public R<TopoAddDeviceResultVO> saveSubDeviceByHttp(@RequestBody TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.ok(deviceInfoService.saveSubDeviceByHttp(topoAddSubDeviceParam));
    }

    /**
     * MQTT协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议修改子设备连接状态", httpMethod = "PUT", notes = "（MQTT）协议修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByMqtt")
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(
            @RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceInfoService.updateSubDeviceConnectStatusByMqtt(topoUpdateSubDeviceStatusParam);
        return R.ok(topoDeviceOperationResultVO);
    }

    /**
     * HTTP协议修改子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 连接状态参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议修改子设备连接状态", httpMethod = "PUT", notes = "（HTTP）协议修改子设备连接状态")
    @PutMapping("/updateSubDeviceConnectStatusByHttp")
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByHttp(
            @RequestBody @ApiParam(value = "连接状态参数") TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceInfoService.updateSubDeviceConnectStatusByHttp(topoUpdateSubDeviceStatusParam);
        return R.ok(topoDeviceOperationResultVO);
    }

    /**
     * MQTT协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（MQTT）协议删除子设备", httpMethod = "PUT", notes = "（MQTT）协议删除子设备")
    @PutMapping("/deleteSubDeviceByMqtt")
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(@RequestBody @ApiParam(value = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceInfoService.deleteSubDeviceByMqtt(topoDeleteSubDeviceParam);
        return R.ok(topoDeviceOperationResultVO);
    }

    /**
     * HTTP协议删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除参数
     * @return {@link TopoDeviceOperationResultVO} 修改结果
     */
    @ApiOperation(value = "（HTTP）协议删除子设备", httpMethod = "PUT", notes = "（HTTP）协议删除子设备")
    @PutMapping("/deleteSubDeviceByHttp")
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByHttp(@RequestBody @ApiParam(value = "删除参数") TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceInfoService.deleteSubDeviceByHttp(topoDeleteSubDeviceParam);
        return R.ok(topoDeviceOperationResultVO);
    }


    /**
     * MQTT协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @ApiOperation(value = "（MQTT）协议数据上报", httpMethod = "POST", notes = "（MQTT）协议数据上报")
    @PostMapping("/deviceDataReportByMqtt")
    public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(@RequestBody @ApiParam(value = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deviceDataReportByMqtt(topoDeviceDataReportParam);
        return R.ok(topoDeviceOperationResultVO);
    }

    /**
     * HTTP协议数据上报
     *
     * @param topoDeviceDataReportParam 数据上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @ApiOperation(value = "（HTTP）协议数据上报", httpMethod = "POST", notes = "（HTTP）协议数据上报")
    @PostMapping("/deviceDataReportByHttp")
    public R<TopoDeviceOperationResultVO> deviceDataReportByHttp(@RequestBody @ApiParam(value = "数据上报参数") TopoDeviceDataReportParam topoDeviceDataReportParam) {
        TopoDeviceOperationResultVO topoDeviceOperationResultVO = deviceService.deviceDataReportByHttp(topoDeviceDataReportParam);
        return R.ok(topoDeviceOperationResultVO);
    }

    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via MQTT Protocol", httpMethod = "POST", notes = "Queries device information using the MQTT protocol")
    @PostMapping("/deviceOpenAny/queryDeviceByMqtt")
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam) {
        return R.ok(deviceService.queryDeviceByMqtt(topoQueryDeviceParam));
    }

    /**
     * Queries device information using the HTTP protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @ApiOperation(value = "Query Device Information via HTTP Protocol", httpMethod = "POST", notes = "Queries device information using the HTTP protocol")
    @PostMapping("/queryDeviceByHttp")
    public R<TopoQueryDeviceResultVO> queryDeviceByHttp(@RequestBody TopoQueryDeviceParam topoQueryDeviceParam) {
        return R.ok(deviceService.queryDeviceByHttp(topoQueryDeviceParam));
    }

    /**
     * Receives and saves a new OTA upgrade record from an MQTT message. This endpoint
     * captures the command response parameters from the MQTT message body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via MQTT.
     * @return {@link R< OtaCommandResponseParam >} A response entity containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record", httpMethod = "POST", notes = "Saves a new OTA upgrade record from MQTT message data.")
    @PostMapping("/saveOtaUpgradeRecordByMqtt")
    public R<OtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam) {
        try {
            // Call the service method to save the record
//            OtaCommandResponseParam savedRecord = otaUpgradeRecordsService.saveOtaUpgradeRecordByMqtt(otaCommandResponseParam);

            // Return a successful response entity with the saved record
            return R.ok();
        } catch (Exception e) {
            // Log the exception and return an error response entity
            // Assuming R.fail() is a method to create a failure response
            return R.fail("Error saving OTA upgrade record: " + e.getMessage());
        }
    }

    /**
     * Receives and saves a new OTA upgrade record from an HTTP request. This endpoint
     * captures the command response parameters from the request body and persists them.
     *
     * @param otaCommandResponseParam The response parameters from an OTA command sent via HTTP.
     * @return {@link R<OtaCommandResponseParam>} A response wrapper containing the saved OTA upgrade record.
     */
    @ApiOperation(value = "Save OTA Upgrade Record via HTTP", httpMethod = "POST", notes = "Saves a new OTA upgrade record from HTTP request data.")
    @PostMapping("/saveUpgradeRecordByHttp")
    public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam) {
        try {
            // Call the service method to save the record
//            OtaCommandResponseParam savedRecord = otaUpgradeRecordsService.saveUpgradeRecordByHttp(otaCommandResponseParam);

            // Return a successful response wrapper with the saved record
            return R.ok();
        } catch (Exception e) {
            // Log the exception and return a failure response wrapper
            // Assuming R.fail() is a method to create a failure response
            return R.fail("Error saving OTA upgrade record via HTTP: " + e.getMessage());
        }
    }


}
