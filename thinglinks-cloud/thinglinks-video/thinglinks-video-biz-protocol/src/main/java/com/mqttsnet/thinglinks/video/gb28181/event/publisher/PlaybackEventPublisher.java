package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.PlaybackControlEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.PlaybackRequestedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.RecordQueryCompletedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackControlEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlaybackRequestedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RecordQueryCompletedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 回放/下载事件发布器。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaybackEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishPlaybackRequestedEvent(PlaybackRequestedEventSource source) {
        log.debug("发布回放请求事件: deviceIdentification={}, channelIdentification={}", source.getDeviceIdentification(), source.getChannelIdentification());
        eventPublisher.publishEvent(new PlaybackRequestedEvent(source));
    }

    public void publishPlaybackControlEvent(PlaybackControlEventSource source) {
        log.debug("发布回放控制事件: deviceIdentification={}, controlType={}", source.getDeviceIdentification(), source.getControlType());
        eventPublisher.publishEvent(new PlaybackControlEvent(source));
    }

    public void publishRecordQueryCompletedEvent(RecordQueryCompletedEventSource source) {
        log.debug("发布录像查询完成事件: deviceIdentification={}, channelIdentification={}, count={}",
                source.getDeviceIdentification(), source.getChannelIdentification(), source.getRecordCount());
        eventPublisher.publishEvent(new RecordQueryCompletedEvent(source));
    }

}
