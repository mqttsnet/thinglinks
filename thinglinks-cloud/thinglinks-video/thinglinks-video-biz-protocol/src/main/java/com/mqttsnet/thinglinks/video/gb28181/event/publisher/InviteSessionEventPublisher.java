package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.InviteSessionChangedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.InviteSessionClosedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.InviteSessionCreatedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionChangedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionClosedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.InviteSessionCreatedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Invite 会话事件发布器。
 * 发布会话相关事件：创建、状态变更、关闭。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InviteSessionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布会话创建事件
     *
     * @param source 事件源
     */
    public void publishInviteSessionCreatedEvent(InviteSessionCreatedEventSource source) {
        log.info("发布Invite会话创建事件: deviceIdentification={}, callId={}, type={}",
                source.getDeviceIdentification(), source.getCallId(), source.getSessionType());
        eventPublisher.publishEvent(new InviteSessionCreatedEvent(source));
    }

    /**
     * 发布会话状态变更事件
     *
     * @param source 事件源
     */
    public void publishInviteSessionChangedEvent(InviteSessionChangedEventSource source) {
        log.info("发布Invite会话状态变更事件: deviceIdentification={}, callId={}, {} → {}",
                source.getDeviceIdentification(), source.getCallId(),
                source.getOldStatus().getDesc(), source.getNewStatus().getDesc());
        eventPublisher.publishEvent(new InviteSessionChangedEvent(source));
    }

    /**
     * 发布会话关闭事件
     *
     * @param source 事件源
     */
    public void publishInviteSessionClosedEvent(InviteSessionClosedEventSource source) {
        log.info("发布Invite会话关闭事件: deviceIdentification={}, callId={}, reason={}",
                source.getDeviceIdentification(), source.getCallId(), source.getCloseReason());
        eventPublisher.publishEvent(new InviteSessionClosedEvent(source));
    }
}
