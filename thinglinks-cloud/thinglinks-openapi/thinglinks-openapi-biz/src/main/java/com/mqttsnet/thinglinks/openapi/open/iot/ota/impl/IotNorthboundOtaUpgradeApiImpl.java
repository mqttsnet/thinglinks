package com.mqttsnet.thinglinks.openapi.open.iot.ota.impl;

import java.util.ArrayList;
import java.util.List;

import com.gitee.sop.support.context.OpenContext;
import com.gitee.sop.support.exception.OpenException;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.link.facade.OtaOpenInnerFacade;
import com.mqttsnet.thinglinks.openapi.enumeration.ErrorStoryMessageEnum;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.IotNorthboundOtaUpgradeApi;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.converter.OtaResponseConverter;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaConfirmTaskRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaListUpgradeableVersionsRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaPullRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaReadResponseRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaRejectTaskRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaReportRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.req.IotNorthboundOtaSaveUpgradeRecordRequest;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaConfirmTaskResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaGetAvailableUpgradeVersionsResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaPullResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaReadResponseResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaRejectTaskResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaReportResponse;
import com.mqttsnet.thinglinks.openapi.open.iot.ota.resp.IotNorthboundOtaSaveUpgradeRecordResponse;
import com.mqttsnet.thinglinks.ota.vo.param.DeviceOtaUpgradeAppConfirmationParam;
import com.mqttsnet.thinglinks.ota.vo.result.DeviceOtaUpgradeAppConfirmationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaListUpgradeableVersionsResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Description:
 * 物联网北向API-设备OTA升级接口实现类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/12/10
 */
@DubboService
@Slf4j
public class IotNorthboundOtaUpgradeApiImpl implements IotNorthboundOtaUpgradeApi {

    @Resource
    private OtaOpenInnerFacade otaOpenInnerFacade;

    @Override
    public IotNorthboundOtaConfirmTaskResponse confirmOtaTask(IotNorthboundOtaConfirmTaskRequest request, OpenContext context) throws OpenException {
        log.info("confirmOtaTask...params: appId={}, tenantId={}, taskId={}, deviceList={}", context.getAppId(), context.getTenantId(), request.getTaskId(), request.getDeviceIdentificationList());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());
        R<DeviceOtaUpgradeAppConfirmationResultVO> confirmResult = otaOpenInnerFacade.otaUpgradeAppConfirmation(DeviceOtaUpgradeAppConfirmationParam.builder()
                .tenantId(context.getTenantId())
                .taskId(request.getTaskId())
                .deviceIdentificationList(request.getDeviceIdentificationList())
                .appConfirmationStatus(true)
                .build());
        if (!confirmResult.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_CONFIRM_UPGRADE_FAILED.getSubCode(), ErrorStoryMessageEnum.OTA_CONFIRM_UPGRADE_FAILED.getSubMsg() + confirmResult.getMsg(), ErrorStoryMessageEnum.OTA_CONFIRM_UPGRADE_FAILED.getSolution());
        }

        return BeanPlusUtil.toBean(confirmResult.getData(), IotNorthboundOtaConfirmTaskResponse.class);
    }

    @Override
    public IotNorthboundOtaRejectTaskResponse rejectOtaTask(IotNorthboundOtaRejectTaskRequest request, OpenContext context) throws OpenException {
        log.info("rejectOtaTask...params: appId={}, tenantId={}, taskId={}, deviceList={}", context.getAppId(), context.getTenantId(), request.getTaskId(), request.getDeviceIdentificationList());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());
        R<DeviceOtaUpgradeAppConfirmationResultVO> rejectResult = otaOpenInnerFacade.otaUpgradeAppConfirmation(DeviceOtaUpgradeAppConfirmationParam.builder()
                .tenantId(context.getTenantId())
                .taskId(request.getTaskId())
                .deviceIdentificationList(request.getDeviceIdentificationList())
                .appConfirmationStatus(false)
                .build());

        if (!rejectResult.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_REJECT_UPGRADE_FAILED.getSubCode(), ErrorStoryMessageEnum.OTA_CONFIRM_UPGRADE_FAILED.getSubMsg() + rejectResult.getMsg(), ErrorStoryMessageEnum.OTA_REJECT_UPGRADE_FAILED.getSolution());
        }
        return BeanPlusUtil.toBean(rejectResult.getData(), IotNorthboundOtaRejectTaskResponse.class);
    }

    @Override
    public IotNorthboundOtaSaveUpgradeRecordResponse saveOtaUpgradeRecord(IotNorthboundOtaSaveUpgradeRecordRequest request, OpenContext context) throws OpenException {
        log.info("saveOtaUpgradeRecord...params: appId={}, tenantId={}, deviceIdentification={}, otaTaskId={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getOtaTaskId());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoOtaCommandResponseParam param = new TopoOtaCommandResponseParam();
        param.setDeviceIdentification(request.getDeviceIdentification());
        param.setOtaTaskId(request.getOtaTaskId());
        param.setUpgradeStatus(request.getUpgradeStatus());
        param.setProgress(request.getProgress());
        param.setStartTime(request.getStartTime());
        param.setEndTime(request.getEndTime());
        param.setErrorCode(request.getErrorCode());
        param.setErrorMessage(request.getErrorMessage());
        param.setSuccessDetails(request.getSuccessDetails());
        param.setFailureDetails(request.getFailureDetails());
        param.setLogDetails(request.getLogDetails());

        R<TopoOtaCommandResponseParam> result = otaOpenInnerFacade.saveOtaUpgradeRecordByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_SAVE_UPGRADE_RECORD_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.OTA_SAVE_UPGRADE_RECORD_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.OTA_SAVE_UPGRADE_RECORD_FAILED.getSolution());
        }

        return OtaResponseConverter.convertToSaveUpgradeRecordResponse(result.getData(), request);
    }

    @Override
    public IotNorthboundOtaPullResponse otaPull(IotNorthboundOtaPullRequest request, OpenContext context) throws OpenException {
        log.info("otaPull...params: appId={}, tenantId={}, deviceIdentification={}, packageType={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getPackageType());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoOtaPullParam param = TopoOtaPullParam.builder()
                .deviceIdentification(request.getDeviceIdentification())
                .packageType(request.getPackageType())
                .currentVersion(request.getCurrentVersion())
                .requestVersion(request.getRequestVersion())
                .build();

        R<TopoOtaPullResponseParam> result = otaOpenInnerFacade.otaPullByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_PULL_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.OTA_PULL_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.OTA_PULL_FAILED.getSolution());
        }

        return OtaResponseConverter.convertToOtaPullResponse(result.getData(), request);
    }

    @Override
    public IotNorthboundOtaReportResponse otaReport(IotNorthboundOtaReportRequest request, OpenContext context) throws OpenException {
        log.info("otaReport...params: appId={}, tenantId={}, deviceIdentification={}, packageType={}, currentVersion={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getPackageType(), request.getCurrentVersion());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoOtaReportParam param = TopoOtaReportParam.builder()
                .deviceIdentification(request.getDeviceIdentification())
                .packageType(request.getPackageType())
                .currentVersion(request.getCurrentVersion())
                .build();

        R<TopoOtaReportResponseParam> result = otaOpenInnerFacade.otaReportByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_REPORT_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.OTA_REPORT_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.OTA_REPORT_FAILED.getSolution());
        }

        return OtaResponseConverter.convertToOtaReportResponse(request);
    }

    @Override
    public IotNorthboundOtaReadResponseResponse otaReadResponse(IotNorthboundOtaReadResponseRequest request, OpenContext context) throws OpenException {
        log.info("otaReadResponse...params: appId={}, tenantId={}, deviceIdentification={}, packageType={}, currentVersion={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getPackageType(), request.getCurrentVersion());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        // 转换请求参数
        TopoOtaReadResponseParam param = TopoOtaReadResponseParam.builder()
                .deviceIdentification(request.getDeviceIdentification())
                .packageType(request.getPackageType())
                .currentVersion(request.getCurrentVersion())
                .build();

        R<?> result = otaOpenInnerFacade.otaReadResponseByNorthbound(param);
        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_READ_RESPONSE_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.OTA_READ_RESPONSE_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.OTA_READ_RESPONSE_FAILED.getSolution());
        }

        return OtaResponseConverter.convertToOtaReadResponseResponse(request);
    }

    @Override
    public IotNorthboundOtaGetAvailableUpgradeVersionsResponse getAvailableUpgradeVersions(IotNorthboundOtaListUpgradeableVersionsRequest request, OpenContext context) throws OpenException {
        log.info("getAvailableUpgradeVersions...params: appId={}, tenantId={}, deviceIdentification={}, packageType={}",
                context.getAppId(), context.getTenantId(), request.getDeviceIdentification(), request.getPackageType());
        ArgumentAssert.notNull(context.getTenantId(), "请传递租户ID");
        ContextUtil.setTenantId(context.getTenantId());

        R<TopoOtaListUpgradeableVersionsResponseParam> result = otaOpenInnerFacade.getAvailableUpgradeVersionsByNorthbound(
                request.getDeviceIdentification(),
                request.getPackageType()
        );

        if (!result.getIsSuccess()) {
            throw new OpenException(ErrorStoryMessageEnum.OTA_PULL_FAILED.getSubCode(),
                    ErrorStoryMessageEnum.OTA_PULL_FAILED.getSubMsg() + result.getMsg(),
                    ErrorStoryMessageEnum.OTA_PULL_FAILED.getSolution());
        }

        TopoOtaListUpgradeableVersionsResponseParam param = result.getData();

        // 转换为openapi响应
        IotNorthboundOtaGetAvailableUpgradeVersionsResponse response = IotNorthboundOtaGetAvailableUpgradeVersionsResponse.builder()
                .deviceIdentification(param.getDeviceIdentification())
                .productIdentification(param.getProductIdentification())
                .packageType(param.getPackageType())
                .currentVersion(param.getCurrentVersion())
                .build();

        if (param.getUpgradeVersions() != null) {
            List<IotNorthboundOtaGetAvailableUpgradeVersionsResponse.UpgradeVersionInfo> versions = new ArrayList<>();
            for (TopoOtaListUpgradeableVersionsResponseParam.UpgradeVersionInfo info : param.getUpgradeVersions()) {
                versions.add(IotNorthboundOtaGetAvailableUpgradeVersionsResponse.UpgradeVersionInfo.builder()
                        .otaTaskId(info.getOtaTaskId())
                        .otaTaskName(info.getOtaTaskName())
                        .packageName(info.getPackageName())
                        .version(info.getVersion())
                        .fileLocation(info.getFileLocation())
                        .description(info.getDescription())
                        .customInfo(info.getCustomInfo())
                        .signMethod(info.getSignMethod())
                        .sign(info.getSign())
                        .build());
            }
            response.setUpgradeVersions(versions);
        }

        return response;
    }

}

