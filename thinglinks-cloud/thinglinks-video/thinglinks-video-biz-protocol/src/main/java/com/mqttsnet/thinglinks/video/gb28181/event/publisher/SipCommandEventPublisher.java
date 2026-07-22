package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.SipCommandSentEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * SIP 命令事件发布器。
 * 发布 SIP 命令相关事件：发送成功、发送失败、收到响应。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipCommandEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布 SIP 命令发送成功事件
     *
     * @param source 事件源
     */
    public void publishSipCommandSentEvent(SipCommandSentEventSource source) {
        log.debug("发布SIP命令发送事件: type={}, deviceIdentification={}, callId={}",
                source.getCommandType(), source.getDeviceIdentification(), source.getCallId());
        eventPublisher.publishEvent(new SipCommandSentEvent(source));
    }

}
