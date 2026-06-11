package com.mqttsnet.thinglinks.bus.adapter;

import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;

/**
 * 协议适配器 SPI,把原始报文归一化为 {@link DeviceProtocolEvent}。
 * 实现须无状态线程安全 + 幂等;失败抛异常由 Dispatcher 捕获。
 *
 * <p>加新协议(如 CoAP)= 加一个 {@code @Component} 实现,Registry 自动发现,
 * Dispatcher / Stage / Hook 全部 0 改动。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface ProtocolEdgeAdapter {

    /**
     * 本适配器支持的协议类型,Registry 据此索引。
     */
    ProtocolTypeEnum supports();

    /**
     * 归一化原始报文为统一事件;不可返 null,失败抛异常。
     */
    DeviceProtocolEvent canonicalize(Object rawSource);
}
