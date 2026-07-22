package com.mqttsnet.thinglinks.link.facade;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.ota.enumeration.OtaTaskRecordAppConfirmStatusEnum;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTaskExecutionService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTasksService;
import com.mqttsnet.thinglinks.ota.vo.param.DeviceOtaUpgradeAppConfirmationParam;
import com.mqttsnet.thinglinks.ota.vo.result.DeviceOtaUpgradeAppConfirmationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaListUpgradeableVersionsResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * OTA相关开放接口实现类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/12/26
 */
@Slf4j
@Service
public class OtaOpenInnerFacadeImpl implements OtaOpenInnerFacade {

    @Autowired
    private OtaUpgradeTaskExecutionService otaUpgradeTaskExecutionService;

    @Autowired
    private OtaUpgradeTasksService otaUpgradeTasksService;

    @Override
    public R<DeviceOtaUpgradeAppConfirmationResultVO> otaUpgradeAppConfirmation(DeviceOtaUpgradeAppConfirmationParam param) {
        try {
            log.info("otaUpgradeAppConfirmation param: {}", JSON.toJSONString(param));
            return R.success(otaUpgradeTaskExecutionService.otaUpgradeAppConfirmation(param.getTaskId(), param.getDeviceIdentificationList(), param.getAppConfirmationStatus() ? OtaTaskRecordAppConfirmStatusEnum.CONFIRMED : OtaTaskRecordAppConfirmStatusEnum.REJECTED));
        } catch (Exception e) {
            log.error("otaUpgradeAppConfirmation failed: {}", e.getMessage(), e);
            return R.fail("Error processing OTA upgrade app confirmation: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        try {
            log.info("saveOtaUpgradeRecordByMqtt param:{}", JSON.toJSONString(topoOtaCommandResponseParam));
            TopoOtaCommandResponseParam savedRecord = otaUpgradeTasksService.saveOtaUpgradeRecordByMqtt(topoOtaCommandResponseParam);
            return R.success(savedRecord);
        } catch (Exception e) {
            log.error("保存OTA升级记录失败", e);
            return R.fail("Error saving OTA upgrade record: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaCommandResponseParam> saveOtaUpgradeRecordByNorthbound(TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        log.info("saveOtaUpgradeRecordByNorthbound param:{}", JSON.toJSONString(topoOtaCommandResponseParam));
        try {
            TopoOtaCommandResponseParam savedRecord = otaUpgradeTasksService.saveUpgradeRecordByNorthbound(topoOtaCommandResponseParam);
            return R.success(savedRecord);
        } catch (Exception e) {
            log.error("保存OTA升级记录失败", e);
            return R.fail("保存OTA升级记录失败: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaPullResponseParam> otaPullByMqtt(TopoOtaPullParam topoOtaPullParam) {
        try {
            log.info("otaPullByMqtt param:{}", JSON.toJSONString(topoOtaPullParam));
            TopoOtaPullResponseParam savedResponse = otaUpgradeTasksService.otaPullByMqtt(topoOtaPullParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA拉取软固件信息失败", e);
            return R.fail("Error processing OTA pull event: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaPullResponseParam> otaPullByNorthbound(TopoOtaPullParam topoOtaPullParam) {
        log.info("otaPullByNorthbound param:{}", JSON.toJSONString(topoOtaPullParam));
        try {
            TopoOtaPullResponseParam savedResponse = otaUpgradeTasksService.otaPullByNorthbound(topoOtaPullParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA拉取软固件信息失败", e);
            return R.fail("OTA拉取软固件信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaReportResponseParam> otaReportByMqtt(TopoOtaReportParam topoOtaReportParam) {
        try {
            log.info("otaReportByMqtt param:{}", JSON.toJSONString(topoOtaReportParam));
            TopoOtaReportResponseParam savedResponse = otaUpgradeTasksService.otaReportByMqtt(topoOtaReportParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA上报软固件版本失败", e);
            return R.fail("Error processing OTA report event: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaReportResponseParam> otaReportByNorthbound(TopoOtaReportParam topoOtaReportParam) {
        log.info("otaReportByNorthbound param:{}", JSON.toJSONString(topoOtaReportParam));
        try {
            TopoOtaReportResponseParam savedResponse = otaUpgradeTasksService.otaReportByNorthbound(topoOtaReportParam);
            return R.success(savedResponse);
        } catch (Exception e) {
            log.error("OTA上报软固件版本失败", e);
            return R.fail("OTA上报软固件版本失败: " + e.getMessage());
        }
    }

    @Override
    public R<?> otaReadResponseByMqtt(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        try {
            log.info("otaReadResponseByMqtt param:{}", JSON.toJSONString(topoOtaReadResponseParam));
            otaUpgradeTasksService.otaReadResponseByMqtt(topoOtaReadResponseParam);
            return R.success();
        } catch (Exception e) {
            log.error("OTA读取设备软固件版本信息响应失败", e);
            return R.fail("Error processing OTA read response event: " + e.getMessage());
        }
    }

    @Override
    public R<?> otaReadResponseByNorthbound(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        log.info("otaReadResponseByNorthbound param:{}", JSON.toJSONString(topoOtaReadResponseParam));
        try {
            otaUpgradeTasksService.otaReadResponseByNorthbound(topoOtaReadResponseParam);
            return R.success();
        } catch (Exception e) {
            log.error("OTA读取设备软固件版本信息响应失败", e);
            return R.fail("OTA读取设备软固件版本信息响应失败: " + e.getMessage());
        }
    }

    @Override
    public R<TopoOtaListUpgradeableVersionsResponseParam> getAvailableUpgradeVersionsByNorthbound(String deviceIdentification, Integer packageType) {
        log.info("getAvailableUpgradeVersionsByNorthbound params: deviceIdentification={}, packageType={}",
                deviceIdentification, packageType);
        try {
            TopoOtaListUpgradeableVersionsResponseParam response = otaUpgradeTasksService.getAvailableUpgradeVersionsByNorthbound(
                    deviceIdentification, packageType);
            return R.success(response);
        } catch (Exception e) {
            log.error("OTA获取可升级版本列表失败", e);
            return R.fail("OTA获取可升级版本列表失败: " + e.getMessage());
        }
    }
}
