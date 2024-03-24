package com.mqttsnet.thinglinks.broker.mqs.protocol;

import com.mqttsnet.thinglinks.broker.mqs.protocol.handler.*;
import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProtocolHandlerFactory
 * -----------------------------------------------------------------------------
 * Description:
 * 协议处理工厂
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
 * @email
 * @date 2024/3/11 16:26
 */
@Slf4j
@Component
public class ProtocolHandlerFactory {
    private final MqttHandler mqttHandler;
    private final WebSocketHandler webSocketHandler;
    private final HttpHandler httpHandler;
    private final TcpHandler tcpHandler;

    @Autowired
    public ProtocolHandlerFactory(
            MqttHandler mqttHandler,
            WebSocketHandler webSocketHandler,
            HttpHandler httpHandler,
            TcpHandler tcpHandler) {
        this.mqttHandler = mqttHandler;
        this.webSocketHandler = webSocketHandler;
        this.httpHandler = httpHandler;
        this.tcpHandler = tcpHandler;
    }

    public ProtocolHandler getHandler(ProtocolTypeEnum type) {
        switch (type) {
            case MQTT:
                return mqttHandler;
            case WEBSOCKET:
                return webSocketHandler;
            case HTTP:
                return httpHandler;
            case TCP:
                return tcpHandler;
            default:
                log.error("Unknown protocol type: {}", type);
                throw new IllegalStateException("Unknown protocol type: " + type);
        }
    }
}