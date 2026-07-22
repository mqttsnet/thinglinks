package com.mqttsnet.thinglinks.dto.bridge.protocol.rocketmq;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * RocketMQ 连接参数 DTO（自建 Apache + 阿里云通过 access-channel 切换）。
 *
 * @author mqttsnet
 */
public class RocketmqConnectionDto implements ProtocolConnectionDto {

    /**
     * Name server 地址，多个用 ; 分隔（如 host1:9876;host2:9876）。必填
     */
    public String nameServer;

    /**
     * 目标 topic 名称。必填
     */
    public String topic;

    /**
     * Tag，支持模板 ${actionType}。可选
     */
    public String tag;

    /**
     * Producer Group。空则使用 PG_THINGLINKS_BRIDGE_${dsId}
     */
    public String producerGroup;

    /**
     * Access channel：LOCAL（自建 Apache）/ CLOUD（阿里云）。默认 LOCAL
     */
    public String accessChannel;

    /**
     * 阿里云专用 namespace（instanceId）。CLOUD 模式必填
     */
    public String namespace;
}
