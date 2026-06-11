package com.mqttsnet.thinglinks.mqs.uplink.service;

import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.facade.OtaOpenAnyUserFacade;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * -----------------------------------------------------------------------------
 * File Name: EventOtaCommandResponseService
 * -----------------------------------------------------------------------------
 * Description:
 * OTA 上报软固件版本信息  mqtt事件业务层
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/1/18       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/03/15 15:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventOtaReportService {

    @Autowired
    private OtaOpenAnyUserFacade otaOpenAnyUserFacade;


    /**
     * Handles the OTA_REPORT topic event.
     *
     * @param topoOtaReportParam The OTA_REPORT topic event.
     * @return {@link Optional} of {@link TopoOtaPullResponseParam} containing the saved OTA command response.
     */
    public Optional<TopoOtaReportResponseParam> handleMqttEventOtaReport(TopoOtaReportParam topoOtaReportParam) {
        try {

            R<TopoOtaReportResponseParam> otaReportResponseParamR = otaOpenAnyUserFacade.otaReportByMqtt(topoOtaReportParam);
            log.info("OTA Report Response: {}", JSON.toJSONString(otaReportResponseParamR));

            if (!otaReportResponseParamR.getIsSuccess()) {
                log.error("Failed to OTA Report: {}", otaReportResponseParamR.getErrorMsg());
                return Optional.empty();
            }

            return Optional.of(otaReportResponseParamR.getData());
        } catch (Exception e) {
            log.error("Failed to OTA Report", e);
            return Optional.empty();
        }
    }

}
