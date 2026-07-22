package com.mqttsnet.thinglinks.video.media.event;

import com.mqttsnet.thinglinks.video.media.event.source.MediaServerCapabilityDetectedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 流媒体服务器能力检测完成事件。
 * 当服务器注册后完成能力探测时发布此事件，
 * 下游监听器可据此更新服务器能力缓存、调整业务策略。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class MediaServerCapabilityDetectedEvent extends ApplicationEvent {

    private final MediaServerCapabilityDetectedEventSource eventSource;

    public MediaServerCapabilityDetectedEvent(MediaServerCapabilityDetectedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
