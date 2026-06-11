package com.mqttsnet.thinglinks.link.facade.impl;

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
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/12/24 17:02
 */
@Service
public class DeviceOpenAnyUserFacadeImpl implements DeviceOpenAnyUserFacade {
    @Lazy
    @Autowired
    private DeviceOpenAnyUserApi deviceOpenAnyUserApi;

    @Override
    public R<Boolean> updateDeviceConnectionStatus(String clientIdentifier, Integer connectionStatus) {
        return deviceOpenAnyUserApi.updateDeviceConnectionStatus(clientIdentifier, connectionStatus);
    }

    @Override
    public R<Boolean> updateDeviceConnectionStatusByEvent(String clientIdentifier, Integer connectionStatus, Long eventHlc) {
        return deviceOpenAnyUserApi.updateDeviceConnectionStatusByEvent(clientIdentifier, connectionStatus, eventHlc);
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return deviceOpenAnyUserApi.saveSubDeviceByMqtt(topoAddSubDeviceParam);
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByNorthbound(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return deviceOpenAnyUserApi.saveSubDeviceByNorthbound(topoAddSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return deviceOpenAnyUserApi.updateSubDeviceConnectStatusByMqtt(topoUpdateSubDeviceStatusParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByNorthbound(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return deviceOpenAnyUserApi.updateSubDeviceConnectStatusByNorthbound(topoUpdateSubDeviceStatusParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deviceOpenAnyUserApi.deleteSubDeviceByMqtt(topoDeleteSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByNorthbound(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deviceOpenAnyUserApi.deleteSubDeviceByNorthbound(topoDeleteSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceOpenAnyUserApi.deviceDataReportByMqtt(topoDeviceDataReportParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByNorthbound(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceOpenAnyUserApi.deviceDataReportByNorthbound(topoDeviceDataReportParam);
    }

    @Override
    public R<DeviceAction> saveDeviceAction(DeviceActionSaveVO deviceActionSaveVO) {
        return deviceOpenAnyUserApi.saveDeviceAction(deviceActionSaveVO);
    }

    @Override
    public R<DeviceCommand> saveDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO) {
        return deviceOpenAnyUserApi.saveDeviceCommand(deviceCommandSaveVO);
    }

    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
        return deviceOpenAnyUserApi.queryDeviceByMqtt(topoQueryDeviceParam);
    }

    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByNorthbound(TopoQueryDeviceParam topoQueryDeviceParam) {
        return deviceOpenAnyUserApi.queryDeviceByNorthbound(topoQueryDeviceParam);
    }

    @Override
    public R<Boolean> reportDeviceHeartbeat(String clientIdentifier, Long heartbeatTime, Long eventHlc) {
        return deviceOpenAnyUserApi.reportDeviceHeartbeat(clientIdentifier, heartbeatTime, eventHlc);
    }

    @Override
    public R<List<DeviceCommandResultVO>> issueCommands(DeviceCommandWrapperParam commandWrapper) {
        return deviceOpenAnyUserApi.issueCommands(commandWrapper);
    }

    @Override
    public R<DeviceResultVO> saveDeviceByNorthbound(DeviceSaveVO deviceSaveVO) {
        return deviceOpenAnyUserApi.saveDeviceByNorthbound(deviceSaveVO);
    }

    @Override
    public R<DeviceDetailsResultVO> getDeviceDetailByNorthbound(String deviceIdentification) {
        return deviceOpenAnyUserApi.getDeviceDetailByNorthbound(deviceIdentification);
    }


    @Override
    public R<ProductResultVO> queryDeviceShadowByNorthbound(String deviceIdentification, Long startTime, Long endTime, String serviceCode) {
        return deviceOpenAnyUserApi.queryDeviceShadowByNorthbound(deviceIdentification, startTime, endTime, serviceCode);
    }

    @Override
    public R<Boolean> updateDeviceStatusByNorthbound(String deviceIdentification, Integer status) {
        return deviceOpenAnyUserApi.updateDeviceStatusByNorthbound(deviceIdentification, status);
    }
}
