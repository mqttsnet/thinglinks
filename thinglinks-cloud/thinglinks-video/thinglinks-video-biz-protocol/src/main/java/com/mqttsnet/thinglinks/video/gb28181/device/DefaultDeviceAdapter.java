package com.mqttsnet.thinglinks.video.gb28181.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 默认设备适配器，处理标准 GB28181 协议的设备。
 * 当设备的厂商标识无法匹配到特定适配器时，使用此默认实现。
 * 所有方法返回标准 GB28181 规范的默认值。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE)
public class DefaultDeviceAdapter implements DeviceAdapter {

    @Override
    public String getManufacturer() {
        return "default";
    }

    /**
     * 默认适配器始终匹配，作为兜底策略。
     * 通过 {@link Order} 注解确保优先级最低，仅在无其他适配器匹配时生效。
     */
    @Override
    public boolean matches(String manufacturer, String userAgent) {
        return true;
    }
}
