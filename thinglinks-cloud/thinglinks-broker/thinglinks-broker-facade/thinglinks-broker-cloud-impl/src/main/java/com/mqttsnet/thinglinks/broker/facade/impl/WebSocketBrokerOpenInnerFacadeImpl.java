package com.mqttsnet.thinglinks.broker.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.WebSocketBrokerOpenInnerFacade;
import com.mqttsnet.thinglinks.broker.api.WebSocketBrokerOpenInnerApi;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/12/24 15:54
 */
@Service
public class WebSocketBrokerOpenInnerFacadeImpl implements WebSocketBrokerOpenInnerFacade {
    @Autowired
    @Lazy
    private WebSocketBrokerOpenInnerApi webSocketBrokerOpenInnerApi;

    @Override
    public R<?> sendMessage(PublishWebSocketMessageRequestVO publishMessageRequestVO) {
        return webSocketBrokerOpenInnerApi.sendMessage(publishMessageRequestVO);
    }
}
