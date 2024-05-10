package com.mqttsnet.thinglinks.broker.mqs.protocol.event.listener;

import com.mqttsnet.thinglinks.broker.api.domain.protocol.HttpEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * -----------------------------------------------------------------------------
 * File Name: HttpEventListener
 * -----------------------------------------------------------------------------
 * Description:
 * HTTP事件监听器
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
 * @email 13733918655@136.com
 * @date 2024/3/11 15:50
 */
@Component
@Slf4j
public class HttpEventListener {

    /**
     * 发布HTTP事件
     *
     * @param event 事件消息
     */
    @EventListener
    @Async("mqsAsync")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void handleHttpEvent(HttpEvent event) {
        log.info("处理HTTP事件: 消息={}", event.getMessage());
        // TODO 实现HTTP事件的处理逻辑
    }
}
