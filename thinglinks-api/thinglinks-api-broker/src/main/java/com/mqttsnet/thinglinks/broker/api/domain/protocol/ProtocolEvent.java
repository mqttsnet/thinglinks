package com.mqttsnet.thinglinks.broker.api.domain.protocol;

import org.springframework.context.ApplicationEvent;

/**
 * -----------------------------------------------------------------------------
 * File Name: ProtocolEvent
 * -----------------------------------------------------------------------------
 * Description:
 * 协议事件类
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
 * @date 2024/3/11 15:03
 */
public class ProtocolEvent extends ApplicationEvent {
    private final String message;

    public ProtocolEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
