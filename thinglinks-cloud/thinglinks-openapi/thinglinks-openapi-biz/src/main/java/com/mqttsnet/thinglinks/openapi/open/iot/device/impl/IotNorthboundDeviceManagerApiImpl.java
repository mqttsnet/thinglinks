package com.mqttsnet.thinglinks.openapi.open.iot.device.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.gitee.sop.support.context.OpenContext;
import com.gitee.sop.support.exception.OpenException;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import com.mqttsnet.thinglinks.openapi.enumeration.ErrorStoryMessageEnum;
import com.mqttsnet.thinglinks.openapi.open.iot.device.IotNorthboundDeviceManagerApi;
import com.mqttsnet.thinglinks.openapi.open.iot.device.converter.DeviceResponseConverter;
import com.mqttsnet.thinglinks.openapi.open.iot.device.converter.DeviceShadowConverter;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceCreateRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceDataReportRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceDeleteSubDeviceRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceGetDetailRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceIssueCommandRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceQueryRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceQueryShadowRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceUpdateStatusRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.req.IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceCreateResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceDataReportResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceDeleteSubDeviceResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceGetDetailResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceIssueCommandResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceQueryResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceQueryShadowResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceUpdateStatusResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceCommandWrapperParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.sop.facade.NotifyFacade;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Description:
 * 物联网北向API-设备管理实现类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/6/22
 */
@DubboService
@Slf4j
public class IotNorthboundDeviceManagerApiImpl implements IotNorthboundDeviceManagerApi {
    @Resource
    private DeviceOpenInnerFacade deviceOpenInnerFacade;
    @Resource
    private NotifyFacade notifyFacade;


    @Override
    public IotNorthboundDeviceCreateResponse createDevice(IotNorthboundDeviceCreateRequest request, OpenContext context) {
        log.info("createDevice...params: appId={}, tenantId={}, deviceIdentification={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        DeviceSaveVO deviceSaveVO = BeanPlusUtil.toBean(request, DeviceSaveVO.class);

        var result = deviceOpenInnerFacade.saveDeviceByNorthbound(deviceSaveVO);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_CREATE_FAILED.getSolution());
        }

        // 转换返回结果
        return DeviceResponseConverter.convertToCreateDeviceResponse(result.getData());
    }

    @Override
    public IotNorthboundDeviceGetDetailResponse getDeviceDetail(IotNorthboundDeviceGetDetailRequest request, OpenContext context) {
        log.info("getDeviceDetail...params: appId={}, tenantId={}, deviceIdentification={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 调用 Facade 查询设备详情
        var result = deviceOpenInnerFacade.getDeviceDetailByNorthbound(request.getDeviceIdentification());
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSolution());
        }

        // 转换返回结果
        DeviceDetailsResultVO deviceDetailsVO = result.getData();
        return BeanPlusUtil.toBean(deviceDetailsVO, IotNorthboundDeviceGetDetailResponse.class);
    }

    @Override
    public IotNorthboundDeviceIssueCommandResponse issueCommand(IotNorthboundDeviceIssueCommandRequest request, OpenContext context) {
        log.info("issueCommand...params: appId={}, tenantId={}, serial={}, parallel={}",
                context.getAppId(), context.getTenantId(),
                Optional.ofNullable(request.getSerial()).map(List::size).orElse(0),
                Optional.ofNullable(request.getParallel()).map(List::size).orElse(0));
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        DeviceCommandWrapperParam commandWrapper = BeanPlusUtil.toBean(request, DeviceCommandWrapperParam.class);

        // 调用 Facade 下发命令
        var result = deviceOpenInnerFacade.issueCommands(commandWrapper);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_COMMAND_ISSUE_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_COMMAND_ISSUE_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_COMMAND_ISSUE_FAILED.getSolution());
        }

        // 转换命令结果
        return DeviceResponseConverter.convertToIssueCommandResponse(result.getData());
    }

    @Override
    public IotNorthboundDeviceQueryShadowResponse queryShadow(IotNorthboundDeviceQueryShadowRequest request, OpenContext context) {
        log.info("queryShadow...params: appId={}, tenantId={}, deviceIdentification={}, startTime={}, endTime={}, serviceCode={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(),
                request.getStartTime(), request.getEndTime(), request.getServiceCode());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 调用 Facade 查询设备影子
        var result = deviceOpenInnerFacade.queryDeviceShadowByNorthbound(
                request.getDeviceIdentification(),
                request.getStartTime(),
                request.getEndTime(),
                request.getServiceCode()
        );
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSolution());
        }

        // 转换返回结果
        ProductResultVO productResultVO = result.getData();
        return DeviceShadowConverter.convert(productResultVO);
    }

    @Override
    public IotNorthboundDeviceUpdateStatusResponse updateDeviceStatus(IotNorthboundDeviceUpdateStatusRequest request, OpenContext context) {
        log.info("updateDeviceStatus...params: appId={}, tenantId={}, deviceIdentification={}, status={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getStatus());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 1. 先获取设备详情（获取更新前的状态）
        var detailResult = deviceOpenInnerFacade.getDeviceDetailByNorthbound(request.getDeviceIdentification());
        if (!detailResult.getIsSuccess() || Objects.isNull(detailResult.getData())) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubMsg() + "设备不存在",
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSolution());
        }

        DeviceDetailsResultVO deviceDetails = detailResult.getData();
        Integer previousStatus = deviceDetails.getDeviceStatus();

        // 2. 调用 Facade 修改设备状态
        var result = deviceOpenInnerFacade.updateDeviceStatusByNorthbound(
                request.getDeviceIdentification(),
                request.getStatus()
        );
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSolution());
        }

        // 3. 构建返回结果（包含完整的设备信息）
        return DeviceResponseConverter.convertToUpdateDeviceStatusResponse(
                result.getData(), deviceDetails, previousStatus, request.getStatus());
    }

    @Override
    public IotNorthboundDeviceUpdateSubDeviceConnectStatusResponse updateSubDeviceConnectStatus(IotNorthboundDeviceUpdateSubDeviceConnectStatusRequest request, OpenContext context) {
        log.info("updateSubDeviceConnectStatus...params: appId={}, tenantId={}, gatewayIdentification={}",
                context.getAppId(), context.getTenantId(), request.getGatewayIdentification());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoUpdateSubDeviceStatusParam param = new TopoUpdateSubDeviceStatusParam();
        param.setGatewayIdentification(request.getGatewayIdentification());
        if (CollUtil.isNotEmpty(request.getDeviceStatuses())) {
            List<TopoUpdateSubDeviceStatusParam.DeviceStatus> deviceStatuses = request.getDeviceStatuses().stream()
                    .map(ds -> TopoUpdateSubDeviceStatusParam.DeviceStatus.builder()
                            .deviceId(ds.getDeviceId())
                            .status(com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum.valueOf(ds.getStatus()))
                            .build())
                    .collect(Collectors.toList());
            param.setDeviceStatuses(deviceStatuses);
        }

        var result = deviceOpenInnerFacade.updateSubDeviceConnectStatusByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_UPDATE_STATUS_FAILED.getSolution());
        }

        // 转换返回结果
        return DeviceResponseConverter.convertToUpdateSubDeviceConnectStatusResponse(result.getData());
    }

    @Override
    public IotNorthboundDeviceDeleteSubDeviceResponse deleteSubDevice(IotNorthboundDeviceDeleteSubDeviceRequest request, OpenContext context) {
        log.info("deleteSubDevice...params: appId={}, tenantId={}, gatewayIdentification={}, deviceIds={}",
                context.getAppId(), context.getTenantId(), request.getGatewayIdentification(), request.getDeviceIds());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoDeleteSubDeviceParam param = TopoDeleteSubDeviceParam.builder()
                .gatewayIdentification(request.getGatewayIdentification())
                .deviceIds(request.getDeviceIds())
                .build();

        var result = deviceOpenInnerFacade.deleteSubDeviceByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_DELETE_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_DELETE_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_DELETE_FAILED.getSolution());
        }

        // 转换返回结果
        return DeviceResponseConverter.convertToDeleteSubDeviceResponse(result.getData());
    }

    @Override
    public IotNorthboundDeviceDataReportResponse deviceDataReport(IotNorthboundDeviceDataReportRequest request, OpenContext context) {
        log.info("deviceDataReport...params: appId={}, tenantId={}, deviceCount={}",
                context.getAppId(), context.getTenantId(), Optional.ofNullable(request.getDevices()).map(List::size).orElse(0));
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoDeviceDataReportParam param = new TopoDeviceDataReportParam();
        if (CollUtil.isNotEmpty(request.getDevices())) {
            List<TopoDeviceDataReportParam.DeviceS> devices = request.getDevices().stream()
                    .map(d -> {
                        TopoDeviceDataReportParam.DeviceS device = new TopoDeviceDataReportParam.DeviceS();
                        device.setDeviceId(d.getDeviceId());
                        if (CollUtil.isNotEmpty(d.getServices())) {
                            List<TopoDeviceDataReportParam.DeviceS.Services> services = d.getServices().stream()
                                    .map(s -> TopoDeviceDataReportParam.DeviceS.Services.builder()
                                            .serviceCode(s.getServiceCode())
                                            .data(s.getData())
                                            .eventTime(s.getEventTime())
                                            .build())
                                    .collect(Collectors.toList());
                            device.setServices(services);
                        }
                        return device;
                    })
                    .collect(Collectors.toList());
            param.setDevices(devices);
        }

        var result = deviceOpenInnerFacade.deviceDataReportByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_DATA_REPORT_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_DATA_REPORT_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_DATA_REPORT_FAILED.getSolution());
        }

        // 转换返回结果
        return DeviceResponseConverter.convertToDataReportResponse(result.getData());
    }

    @Override
    public IotNorthboundDeviceQueryResponse queryDevice(IotNorthboundDeviceQueryRequest request, OpenContext context) {
        log.info("queryDevice...params: appId={}, tenantId={}, deviceIds={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIds());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoQueryDeviceParam param = TopoQueryDeviceParam.builder()
                .deviceIds(request.getDeviceIds())
                .build();

        var result = deviceOpenInnerFacade.queryDeviceByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.DEVICE_QUERY_FAILED.getSolution());
        }

        // 转换返回结果
        return DeviceResponseConverter.convertToQueryDeviceResponse(result.getData());
    }

}
