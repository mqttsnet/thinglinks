package com.mqttsnet.thinglinks.mqs.uplink.service;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.facade.OtaOpenAnyUserFacade;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * -----------------------------------------------------------------------------
 * File Name: EventOtaCommandResponseService
 * -----------------------------------------------------------------------------
 * Description:
 * OTA 远程升级  mqtt事件业务层
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
 * @date 2024/1/18 22:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventOtaCommandResponseService {

    @Autowired
    private OtaOpenAnyUserFacade otaOpenAnyUserFacade;


    /**
     * Saves the OTA command response from MQTT events.
     *
     * @param deviceCacheVO Device cache information for the device receiving the OTA command.
     * @param dataBody      The message body containing the OTA command response.
     */
    public void saveMqttEventOtaCommandResponse(DeviceCacheVO deviceCacheVO, String dataBody) {
        try {
            // Convert the JSON data body to an OtaCommandResponseParam object
            TopoOtaCommandResponseParam topoOtaCommandResponseParam = JSON.parseObject(dataBody, TopoOtaCommandResponseParam.class);

            // Assuming a method that handles the actual saving of the response
            saveOtaCommandResponse(topoOtaCommandResponseParam);

            // Log the successfully saved command response
            log.info("OTA command response successfully saved: {}", JSON.toJSONString(topoOtaCommandResponseParam));

        } catch (Exception e) {
            // Log the error along with the stack trace for debugging
            log.error("Failed to process OTA command response: {}", dataBody, e);

            // Re-throw the exception to maintain the flow (can be customized as needed)
            throw new IllegalStateException("Failed to save OTA command response", e);
        }
    }

    // This is a placeholder for the actual saving logic
    private void saveOtaCommandResponse(TopoOtaCommandResponseParam responseParam) {
        // Implementation for saving the response in your system
        R<TopoOtaCommandResponseParam> otaCommandResponseParamR = otaOpenAnyUserFacade.saveOtaUpgradeRecordByMqtt(responseParam);
        log.info("otaCommandResponseParamR:{}", JSON.toJSONString(otaCommandResponseParamR));
    }


}
