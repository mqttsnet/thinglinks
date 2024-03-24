package com.mqttsnet.thinglinks.broker.mqs.protocol.handler;

import com.mqttsnet.thinglinks.broker.mqs.protocol.event.publisher.ProtocolMessageEventPublisher;
import com.mqttsnet.thinglinks.link.api.domain.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * File Name: TcpHandler
 * -----------------------------------------------------------------------------
 * Description:
 * TCP协议处理器
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
 * @date 2024/3/11 17:11
 */
@Component
@Slf4j
public class TcpHandler implements ProtocolHandler {

    @Autowired
    private ProtocolMessageEventPublisher eventPublisher;

    @Override
    public void processMessage(String message) {
        // TCP协议特定的预处理逻辑
        log.info("处理TCP协议消息: {}", message);
        eventPublisher.publishEvent(getProtocolTypeEnum(), message);
    }


    /**
     * 获取协议类型枚举
     *
     * @return 协议类型
     */
    @Override
    public ProtocolTypeEnum getProtocolTypeEnum() {
        return ProtocolTypeEnum.TCP;
    }

}