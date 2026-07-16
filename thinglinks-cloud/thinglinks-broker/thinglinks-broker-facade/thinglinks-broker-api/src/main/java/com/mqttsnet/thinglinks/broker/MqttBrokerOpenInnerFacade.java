package com.mqttsnet.thinglinks.broker;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;

import java.util.List;

/**
 * @program: thinglinks-cloud
 * @description: MqttBroker-开放接口API
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:35
 **/
public interface MqttBrokerOpenInnerFacade {


    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    R<?> sendMessage(PublishMessageRequestVO publishMessageRequestVO);

    /**
     * 关闭客户端连接
     *
     * @param killClientRequestVO 关闭客户端请求参数
     * @return {@link R} 结果
     */
    R<?> closeConnection(KillClientRequestVO killClientRequestVO);


    /**
     * Retrieves session information for a specified user and client ID from the MQTT broker.
     * This is useful for system administrators to monitor or manage MQTT session states.
     *
     * @param tenantId The tenant identifier under which the session is registered, required.
     * @param userId   The unique identifier of the user who established the session, required.
     * @param clientId The unique client identifier of the MQTT session, required.
     * @return {@link R<MqttSessionDetailsResultVO>} containing the session details or an error message if not found.
     */
    R<MqttSessionDetailsResultVO> getSessionInfo(String tenantId, String userId, String clientId);

    /**
     * 查询设备 BifroMQ session 实时在线状态(三态语义).
     *
     * @param tenantId             租户 ID
     * @param deviceIdentification 设备标识(作为 BifroMQ userId)
     * @param clientId             MQTT clientId
     * @return {@link R#success(Object)} {@code (true)} 在线;{@link R#success(Object)} {@code (false)} 离线(broker 404);
     *         {@link R#fail()} 不确定(broker 临时异常 / 超时,调用方应保留现状)
     */
    R<Boolean> isOnline(String tenantId, String deviceIdentification, String clientId);
}
