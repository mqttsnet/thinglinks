package com.mqttsnet.thinglinks.video.dto.device.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 通道基础事件对象（Base Event）
 *
 * @param <T> 事件源类型
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-17
 */
@Getter
public class ChannelInfoBaseEventAbstract<T> extends ApplicationEvent {
    private final T source;
    /** 租户ID（通用字段，监听方可直接恢复上下文） */
    private Long tenantId;

    public ChannelInfoBaseEventAbstract(T source) {
        super(source);
        this.source = source;
    }

    public ChannelInfoBaseEventAbstract(T source, Long tenantId) {
        super(source);
        this.source = source;
        this.tenantId = tenantId;
    }
}
