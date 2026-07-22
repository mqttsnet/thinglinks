package com.mqttsnet.thinglinks.broker;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;

/**
 * @program: thinglinks-cloud
 * @description: WebSocketBroker-开放接口API
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:35
 **/
public interface WebSocketBrokerOpenInnerFacade {


    /**
     * 推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    R<?> sendMessage(PublishWebSocketMessageRequestVO publishMessageRequestVO);
}
