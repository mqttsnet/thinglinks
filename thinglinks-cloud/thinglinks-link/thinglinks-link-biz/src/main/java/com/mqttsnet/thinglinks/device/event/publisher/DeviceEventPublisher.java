package com.mqttsnet.thinglinks.device.event.publisher;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.device.event.DeviceDeletedEvent;
import com.mqttsnet.thinglinks.device.event.DeviceInfoUpdatedEvent;
import com.mqttsnet.thinglinks.device.event.DeviceRebindEvent;
import com.mqttsnet.thinglinks.device.event.source.DeviceDeletedEventSource;
import com.mqttsnet.thinglinks.device.event.source.DeviceInfoUpdatedEventSource;
import com.mqttsnet.thinglinks.device.event.source.DeviceRebindEventSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 设备事件发布器
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/6/5
 */
@Component
@Slf4j
public class DeviceEventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource source) {
        log.info("Publishing Device Info Updated event:{}", JSON.toJSONString(source));
        eventPublisher.publishEvent(new DeviceInfoUpdatedEvent(source));
    }

    /**
     * 发布设备删除事件。
     * <p>由 {@link com.mqttsnet.thinglinks.device.service.DeviceService#deleteDevice(Long)} 等
     * 删除入口在数据删除成功后调用，触发下游各模块清理对该设备的残留引用。
     *
     * @param source 设备删除事件源
     */
    public void publishDeviceDeletedEvent(DeviceDeletedEventSource source) {
        log.info("Publishing Device Deleted event:{}", JSON.toJSONString(source));
        eventPublisher.publishEvent(new DeviceDeletedEvent(source));
    }

    /**
     * 发布设备改绑版本事件(全量 / 灰度 / 回滚改绑后用,触发设备缓存失效)。
     *
     * @param source 设备改绑事件源
     */
    public void publishDeviceRebindEvent(DeviceRebindEventSource source) {
        log.info("Publishing Device Rebind event:{}", JSON.toJSONString(source));
        eventPublisher.publishEvent(new DeviceRebindEvent(source));
    }
}
