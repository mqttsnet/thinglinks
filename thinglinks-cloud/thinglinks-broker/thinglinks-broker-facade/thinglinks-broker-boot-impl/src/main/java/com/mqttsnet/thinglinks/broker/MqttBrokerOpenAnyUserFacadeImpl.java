package com.mqttsnet.thinglinks.broker;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.broker.mqtt.service.MqttBrokerService;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangyh
 * @since 2024/12/24 15:54
 */
@Service
@Slf4j
public class MqttBrokerOpenAnyUserFacadeImpl implements MqttBrokerOpenAnyUserFacade {
    @Autowired
    private MqttBrokerService mqttBrokerService;

    @Override
    public R<?> sendMessage(PublishMessageRequestVO publishMessageRequestVO) {
        log.info("Received request to send message.param {}", JSON.toJSONString(publishMessageRequestVO));
        try {
            return R.success(mqttBrokerService.publishMessage(publishMessageRequestVO));
        } catch (BizException e) {
            log.error("Failed to send message. param: {}", JSON.toJSONString(publishMessageRequestVO), e);
            return R.fail(e.getMessage());
        }
    }

    @Override
    public R<?> closeConnection(KillClientRequestVO killClientRequestVO) {
        log.info("Received request to close connection. param: {}", JSON.toJSONString(killClientRequestVO));
        try {
            mqttBrokerService.killClientConnection(killClientRequestVO.getTenantId(), killClientRequestVO.getUserId(), killClientRequestVO.getClientId(), killClientRequestVO.getClientType());
            return R.success();
        } catch (BizException e) {
            log.error("Failed to close connection. param: {}", JSON.toJSONString(killClientRequestVO), e);
            return R.fail(e.getMessage());
        }
    }

    @Override
    public R<MqttSessionDetailsResultVO> getSessionInfo(String tenantId, String userId, String clientId) {
        log.info("Received request to get session info for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);

        try {
            MqttSessionDetailsResultVO mqttSessionDetailsResultVO = mqttBrokerService.getSessionInfo(tenantId, userId, clientId);
            log.info("Successfully retrieved session info for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            return R.success(mqttSessionDetailsResultVO);
        } catch (BizException e) {
            log.warn("Business exception while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Error: {}", tenantId, userId, clientId, e.getMessage());
            return R.fail(R.FAIL_CODE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Error: {}", tenantId, userId, clientId, e.getMessage());
            return R.fail("Error retrieving session info", e.getMessage());
        }
    }

    @Override
    public R<Boolean> isOnline(String tenantId, String deviceIdentification, String clientId) {
        return mqttBrokerService.isOnline(tenantId, deviceIdentification, clientId);
    }
}
