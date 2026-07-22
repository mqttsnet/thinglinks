package com.mqttsnet.thinglinks.broker.downlink;

/**
 * 下行协议常量 ── 取值与 {@code com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum#getValue()} 对齐。
 *
 * <p>broker-biz 不依赖 link 的协议枚举,以字符串常量承载;调用方传 {@code protocolTypeEnum.getValue()}。
 *
 * @author mqttsnet
 */
public final class DownlinkProtocols {

    private DownlinkProtocols() {
    }

    /**
     * MQTT 协议(经 BifroMQ,按 topic 路由)。
     */
    public static final String MQTT = "MQTT";

    /**
     * WebSocket 协议(按 clientId 经 RocketMQ 广播到持有 session 的节点投递)。
     */
    public static final String WEBSOCKET = "WEBSOCKET";

    /**
     * TCP 协议(下行通道待接入)。
     */
    public static final String TCP = "TCP";
}
