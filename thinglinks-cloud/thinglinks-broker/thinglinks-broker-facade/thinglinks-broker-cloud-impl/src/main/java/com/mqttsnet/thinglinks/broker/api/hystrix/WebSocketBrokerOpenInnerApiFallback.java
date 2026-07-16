package com.mqttsnet.thinglinks.broker.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.api.WebSocketBrokerOpenInnerApi;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud
 * @description: WebSocket Broker开放API熔断
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:37
 **/
@Component
public class WebSocketBrokerOpenInnerApiFallback implements WebSocketBrokerOpenInnerApi {

    /**
     * WebSocket 推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @Override
    public R<?> sendMessage(PublishWebSocketMessageRequestVO publishMessageRequestVO) {
        return R.timeout();
    }
}
