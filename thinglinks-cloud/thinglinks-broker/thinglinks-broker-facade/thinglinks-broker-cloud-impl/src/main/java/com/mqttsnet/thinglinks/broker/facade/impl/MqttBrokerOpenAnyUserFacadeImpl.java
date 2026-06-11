package com.mqttsnet.thinglinks.broker.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.broker.api.MqttBrokerOpenAnyUserApi;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangyh
 * @since 2024/12/24 15:54
 */
@Service
public class MqttBrokerOpenAnyUserFacadeImpl implements MqttBrokerOpenAnyUserFacade {
    @Autowired
    @Lazy
    private MqttBrokerOpenAnyUserApi mqttBrokerOpenAnyUserApi;

    @Override
    public R<?> sendMessage(PublishMessageRequestVO publishMessageRequestVO) {
        return mqttBrokerOpenAnyUserApi.sendMessage(publishMessageRequestVO);
    }

    @Override
    public R<?> closeConnection(KillClientRequestVO killClientRequestVO) {
        return mqttBrokerOpenAnyUserApi.closeConnection(killClientRequestVO);
    }

    @Override
    public R<MqttSessionDetailsResultVO> getSessionInfo(String tenantId, String userId, String clientId) {
        return mqttBrokerOpenAnyUserApi.getSessionInfo(tenantId, userId, clientId);
    }

    @Override
    public R<Boolean> isOnline(String tenantId, String deviceIdentification, String clientId) {
        return mqttBrokerOpenAnyUserApi.isOnline(tenantId, deviceIdentification, clientId);
    }
}
