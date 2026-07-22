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
import com.mqttsnet.thinglinks.link.api.inner.DeviceOpenInnerApi;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
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
public class DeviceOpenInnerFacadeImpl implements DeviceOpenInnerFacade {
    @Lazy
    @Autowired
    private DeviceOpenInnerApi deviceOpenInnerApi;

    @Override
    public R<Boolean> updateDeviceConnectionStatus(String clientIdentifier, Integer connectionStatus) {
        return deviceOpenInnerApi.updateDeviceConnectionStatus(clientIdentifier, connectionStatus);
    }

    @Override
    public R<Boolean> updateDeviceConnectionStatusByEvent(String clientIdentifier, Integer connectionStatus, Long eventHlc) {
        return deviceOpenInnerApi.updateDeviceConnectionStatusByEvent(clientIdentifier, connectionStatus, eventHlc);
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return deviceOpenInnerApi.saveSubDeviceByMqtt(topoAddSubDeviceParam);
    }

    @Override
    public R<TopoAddDeviceResultVO> saveSubDeviceByNorthbound(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return deviceOpenInnerApi.saveSubDeviceByNorthbound(topoAddSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return deviceOpenInnerApi.updateSubDeviceConnectStatusByMqtt(topoUpdateSubDeviceStatusParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> updateSubDeviceConnectStatusByNorthbound(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return deviceOpenInnerApi.updateSubDeviceConnectStatusByNorthbound(topoUpdateSubDeviceStatusParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deviceOpenInnerApi.deleteSubDeviceByMqtt(topoDeleteSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deleteSubDeviceByNorthbound(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deviceOpenInnerApi.deleteSubDeviceByNorthbound(topoDeleteSubDeviceParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceOpenInnerApi.deviceDataReportByMqtt(topoDeviceDataReportParam);
    }

    @Override
    public R<TopoDeviceOperationResultVO> deviceDataReportByNorthbound(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceOpenInnerApi.deviceDataReportByNorthbound(topoDeviceDataReportParam);
    }

    @Override
    public R<DeviceAction> saveDeviceAction(DeviceActionSaveVO deviceActionSaveVO) {
        return deviceOpenInnerApi.saveDeviceAction(deviceActionSaveVO);
    }

    @Override
    public R<DeviceCommand> saveDeviceCommand(DeviceCommandSaveVO deviceCommandSaveVO) {
        return deviceOpenInnerApi.saveDeviceCommand(deviceCommandSaveVO);
    }

    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
        return deviceOpenInnerApi.queryDeviceByMqtt(topoQueryDeviceParam);
    }

    @Override
    public R<TopoQueryDeviceResultVO> queryDeviceByNorthbound(TopoQueryDeviceParam topoQueryDeviceParam) {
        return deviceOpenInnerApi.queryDeviceByNorthbound(topoQueryDeviceParam);
    }

    @Override
    public R<Boolean> reportDeviceHeartbeat(String clientIdentifier, Long heartbeatTime, Long eventHlc) {
        return deviceOpenInnerApi.reportDeviceHeartbeat(clientIdentifier, heartbeatTime, eventHlc);
    }

    @Override
    public R<List<DeviceCommandResultVO>> issueCommands(DeviceCommandWrapperParam commandWrapper) {
        return deviceOpenInnerApi.issueCommands(commandWrapper);
    }

    @Override
    public R<DeviceResultVO> saveDeviceByNorthbound(DeviceSaveVO deviceSaveVO) {
        return deviceOpenInnerApi.saveDeviceByNorthbound(deviceSaveVO);
    }

    @Override
    public R<DeviceDetailsResultVO> getDeviceDetailByNorthbound(String deviceIdentification) {
        return deviceOpenInnerApi.getDeviceDetailByNorthbound(deviceIdentification);
    }


    @Override
    public R<ProductResultVO> queryDeviceShadowByNorthbound(String deviceIdentification, Long startTime, Long endTime, String serviceCode) {
        return deviceOpenInnerApi.queryDeviceShadowByNorthbound(deviceIdentification, startTime, endTime, serviceCode);
    }

    @Override
    public R<Boolean> updateDeviceStatusByNorthbound(String deviceIdentification, Integer status) {
        return deviceOpenInnerApi.updateDeviceStatusByNorthbound(deviceIdentification, status);
    }
}
