package com.mqttsnet.thinglinks.mqs.uplink.service;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.link.facade.OtaOpenAnyUserFacade;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * -----------------------------------------------------------------------------
 * File Name: EventOtaCommandResponseService
 * -----------------------------------------------------------------------------
 * Description:
 * OTA 读取软固件信息响应  mqtt事件业务层
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
public class EventOtaReadResponseService {

    @Autowired
    private OtaOpenAnyUserFacade otaOpenAnyUserFacade;


    /**
     * Handles the OTA_READ_RESPONSE topic event.
     *
     * @param topoOtaReadResponseParam The OTA_READ_RESPONSE topic event data.
     */
    public void handleMqttEventOtaReadResponse(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        try {
            R<?> otaReadResponseParamR = otaOpenAnyUserFacade.otaReadResponseByMqtt(topoOtaReadResponseParam);
            log.info("OTA Read response: {}", JSON.toJSONString(otaReadResponseParamR));

            if (!otaReadResponseParamR.getIsSuccess()) {
                log.error("Failed to save OTA command response: {}", otaReadResponseParamR.getErrorMsg());
            }

        } catch (Exception e) {
            log.error("Failed to process OTA Read Response: {}", topoOtaReadResponseParam, e);
        }
    }

}
