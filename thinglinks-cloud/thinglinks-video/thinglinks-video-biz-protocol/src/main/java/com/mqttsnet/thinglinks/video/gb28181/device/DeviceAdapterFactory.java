package com.mqttsnet.thinglinks.video.gb28181.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description:
 * 设备厂商适配器工厂。
 * 根据设备的 manufacturer 字段或 SIP 注册时的 User-Agent 自动选择
 * 合适的厂商适配器。适配器通过 {@link org.springframework.core.annotation.Order}
 * 注解控制匹配优先级，默认适配器优先级最低作为兜底。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class DeviceAdapterFactory {

    private final List<DeviceAdapter> adapters;

    public DeviceAdapterFactory(List<DeviceAdapter> adapters) {
        this.adapters = adapters;
        log.info("注册设备厂商适配器 {} 个: {}", adapters.size(),
                adapters.stream().map(DeviceAdapter::getManufacturer).toList());
    }

    /**
     * 根据厂商名称和 User-Agent 选择适配器。
     * 按 {@link org.springframework.core.annotation.Order} 优先级顺序匹配，
     * 第一个匹配成功的适配器将被返回。
     *
     * @param manufacturer 设备上报的厂商名称（可为空）
     * @param userAgent    SIP 注册时的 User-Agent 头（可为空）
     * @return 匹配的设备适配器（始终非 null，至少返回 DefaultDeviceAdapter）
     */
    public DeviceAdapter getAdapter(String manufacturer, String userAgent) {
        for (DeviceAdapter adapter : adapters) {
            if (adapter.matches(manufacturer, userAgent)) {
                log.debug("设备厂商匹配: manufacturer={}, userAgent={}, adapter={}",
                        manufacturer, userAgent, adapter.getManufacturer());
                return adapter;
            }
        }
        // 不应到达此处，因为 DefaultDeviceAdapter 始终匹配
        log.warn("未找到匹配的设备适配器，返回第一个适配器: manufacturer={}, userAgent={}", manufacturer, userAgent);
        return adapters.get(0);
    }
}
