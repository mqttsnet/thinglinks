package com.mqttsnet.thinglinks.broker.mqs.protocol.event.listener;

import com.mqttsnet.thinglinks.broker.api.domain.protocol.WebSocketEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * File Name: WebSocketEventListener
 * -----------------------------------------------------------------------------
 * Description:
 * WebSocket事件监听器
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
 * @date 2024/3/11 15:57
 */
@Component
@Slf4j
public class WebSocketEventListener {

    /**
     * 发布WebSocket事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("mqsAsync")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleWebSocketEvent(WebSocketEvent event) {
        log.info("处理WebSocket事件: 消息={}", event.getMessage());
        // TODO 实现WebSocket事件的处理逻辑
    }

}
