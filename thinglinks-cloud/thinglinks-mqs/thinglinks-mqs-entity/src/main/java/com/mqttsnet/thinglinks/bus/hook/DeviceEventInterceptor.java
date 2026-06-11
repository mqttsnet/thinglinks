package com.mqttsnet.thinglinks.bus.hook;

import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;

/**
 * 事件拦截器 SPI,canonicalize 前后拦截(黑白名单 / IP 限流 / 全量报文转储)。
 * 任意实现抛 {@link DeviceEventDropException} → 终止 pipeline。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface DeviceEventInterceptor {

    /**
     * canonicalize 之前,可抛 {@link DeviceEventDropException} 整体丢弃。
     */
    default void beforeCanonicalize(String sourceTopic, Object rawSource) {
    }

    /**
     * canonicalize 之后,可二次校验 / 富化。
     */
    default void afterCanonicalize(DeviceProtocolEvent event) {
    }
}
