package com.mqttsnet.thinglinks.device.event;

import com.mqttsnet.thinglinks.device.event.source.DeviceDeletedEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 * 设备删除事件。
 * <p>设备实体被物理/逻辑删除后由 {@link com.mqttsnet.thinglinks.device.service.DeviceService} 发布，
 * 用于触发下游各模块清理对该设备的残留引用，避免出现"设备已删除但分组/规则/告警等仍持有指针"的孤儿数据。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-27
 */
@Getter
public class DeviceDeletedEvent extends ApplicationEvent {
    private final DeviceDeletedEventSource eventSource;

    public DeviceDeletedEvent(DeviceDeletedEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
