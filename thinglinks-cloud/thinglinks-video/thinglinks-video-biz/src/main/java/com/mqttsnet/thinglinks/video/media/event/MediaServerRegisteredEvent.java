package com.mqttsnet.thinglinks.video.media.event;

import com.mqttsnet.thinglinks.video.media.event.source.MediaServerRegisteredEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 流媒体服务器注册事件。
 * 当流媒体服务器（ZLM/ABL）首次注册或重新上线时发布此事件，
 * 下游监听器可据此初始化服务器配置、启动能力检测等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
public class MediaServerRegisteredEvent extends ApplicationEvent {

    private final MediaServerRegisteredEventSource eventSource;

    public MediaServerRegisteredEvent(MediaServerRegisteredEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
