package com.mqttsnet.thinglinks.broker.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.broker.api.MqttBrokerOpenAnyUserApi;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: thinglinks-cloud
 * @description: MqttBroker开放API熔断
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:37
 **/
@Component
public class MqttBrokerOpenAnyUserApiFallback implements MqttBrokerOpenAnyUserApi {

    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @Override
    public R<?> sendMessage(PublishMessageRequestVO publishMessageRequestVO) {
        return R.timeout();
    }

    /**
     * 关闭客户端连接
     *
     * @param killClientRequestVO 关闭客户端请求参数
     * @return {@link R} 结果
     */
    @Override
    public R closeConnection(KillClientRequestVO killClientRequestVO) {
        return R.timeout();
    }

    @Override
    public R<MqttSessionDetailsResultVO> getSessionInfo(String tenantId, String userId, String clientId) {
        return R.timeout();
    }

    @Override
    public R<Boolean> isOnline(String tenantId, String deviceIdentification, String clientId) {
        return R.timeout();
    }
}
