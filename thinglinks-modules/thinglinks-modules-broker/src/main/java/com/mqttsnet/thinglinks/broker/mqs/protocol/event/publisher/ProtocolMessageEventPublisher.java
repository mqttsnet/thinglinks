package com.mqttsnet.thinglinks.broker.mqs.protocol.event.publisher;

import com.mqttsnet.thinglinks.broker.api.domain.protocol.HttpEvent;
import com.mqttsnet.thinglinks.broker.api.domain.protocol.MqttEvent;
import com.mqttsnet.thinglinks.broker.api.domain.protocol.TcpEvent;
import com.mqttsnet.thinglinks.broker.api.domain.protocol.WebSocketEvent;
import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProtocolMessageEventPublisher
 * -----------------------------------------------------------------------------
 * Description:
 * 协议消息事件发布器
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/3/11       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2024/3/11 15:00
 */
@Component
@Slf4j
public class ProtocolMessageEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 根据协议类型发布对应的事件
     *
     * @param protocolType 协议类型
     * @param message      消息内容
     */
    public void publishEvent(ProtocolTypeEnum protocolType, String message) {
        log.info("发布{}事件, 消息内容: {}", protocolType.getDesc(), message);

        switch (protocolType) {
            case MQTT:
                eventPublisher.publishEvent(new MqttEvent(this, message));
                break;
            case HTTP:
                eventPublisher.publishEvent(new HttpEvent(this, message));
                break;
            case TCP:
                eventPublisher.publishEvent(new TcpEvent(this, message));
                break;
            case WEBSOCKET:
                eventPublisher.publishEvent(new WebSocketEvent(this, message));
                break;
            // ...为其他协议添加case语句...
            default:
                log.error("未知的协议类型: {}", protocolType);
                break;
        }
    }

}
