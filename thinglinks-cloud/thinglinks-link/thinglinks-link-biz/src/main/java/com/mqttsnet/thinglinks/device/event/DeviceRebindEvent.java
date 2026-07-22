package com.mqttsnet.thinglinks.device.event;

import com.mqttsnet.thinglinks.device.event.source.DeviceRebindEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 设备改绑版本事件。
 *
 * <p>设备 boundProductVersionNo 变更(发布全量改绑 / 灰度白名单 / 回滚)后发布,
 * 由 DeviceCacheEvictListener 在事务提交后失效设备缓存,
 * 否则数据上报链路仍按旧版本号路由到旧数据表。载荷见 {@link DeviceRebindEventSource}。</p>
 *
 * @author mqttsnet
 */
@Getter
public class DeviceRebindEvent extends ApplicationEvent {

    private final transient DeviceRebindEventSource eventSource;

    public DeviceRebindEvent(DeviceRebindEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
