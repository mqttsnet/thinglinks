package com.mqttsnet.thinglinks.link.api.anyuser.hystrix;

import java.util.List;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.entity.DeviceCommand;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceCommandSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.link.api.anyuser.DeviceOpenAnyUserApi;
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
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud
 * @description: 设备开放API熔断
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:37
 **/
@Component
public class DeviceOpenAnyUserApiFallback implements DeviceOpenAnyUserApi {

    @Override
    public R<Boolean> updateDeviceConnectionStatus(String clientIdentifier, Integer connectionStatus) {
        return R.timeout();
    }

    @Override
    public R<Boolean> updateDeviceConnectionStatusByEvent(String clientIdentifier, Integer connectionStatus, Long eventHlc) {
        return R.timeout();
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByNorthbound(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return R.timeout();
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByNorthbound(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return R.timeout();
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByNorthbound(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return R.timeout();
    }


    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByNorthbound(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return R.timeout();
    }

    @Override
    public R<DeviceAction> saveDeviceAction(DeviceActionSaveVO deviceActionSaveVO) {
        return R.timeout();
    }

    @Override
    public R<DeviceCommand> saveDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO) {
        return R.timeout();
    }


    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByNorthbound(TopoQueryDeviceParam topoQueryDeviceParam) {
        return R.timeout();
    }

    @Override
    public R<Boolean> reportDeviceHeartbeat(String clientIdentifier, Long heartbeatTime, Long eventHlc) {
        return R.timeout();
    }

    @Override
    public R<List<DeviceCommandResultVO>> issueCommands(DeviceCommandWrapperParam commandWrapper) {
        return R.timeout();
    }

    @Override
    public R<DeviceResultVO> saveDeviceByNorthbound(DeviceSaveVO deviceSaveVO) {
        return R.timeout();
    }

    @Override
    public R<DeviceDetailsResultVO> getDeviceDetailByNorthbound(String deviceIdentification) {
        return R.timeout();
    }


    @Override
    public R<ProductResultVO> queryDeviceShadowByNorthbound(String deviceIdentification, Long startTime, Long endTime, String serviceCode) {
        return R.timeout();
    }

    @Override
    public R<Boolean> updateDeviceStatusByNorthbound(String deviceIdentification, Integer status) {
        return R.timeout();
    }
}
