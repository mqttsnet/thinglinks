package com.mqttsnet.thinglinks.dto.bridge.protocol.rabbitmq;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * RabbitMQ 连接参数 DTO。
 *
 * <p>⚠ 字段名 {@code exchangeName}（不是 {@code exchange}）── 与 util-pro
 * {@code RabbitmqSink.RmqConnConfig.exchangeName} 严格对齐。
 * 前端旧 schema 用 {@code exchange} 是 bug，重构后的 RabbitmqProtocol 模块统一用 exchangeName。
 *
 * @author mqttsnet
 */
public class RabbitmqConnectionDto implements ProtocolConnectionDto {

    /**
     * AMQP host。必填
     */
    public String host;

    /**
     * AMQP 端口（TLS 通常 5671，明文 5672）。默认 5672
     */
    public Integer port;

    /**
     * Virtual host（多 vhost 部署用）。默认 "/"
     */
    public String virtualHost;

    /**
     * Exchange 名称。必填。⚠ 字段名 exchangeName 不是 exchange
     */
    public String exchangeName;

    /**
     * Exchange 类型：direct / fanout / topic / headers。默认 topic
     */
    public String exchangeType;

    /**
     * Routing key 模板，支持 ${routingKey} ${ts} ${header.X} 占位符。必填
     */
    public String routingKey;

    /**
     * 是否启用 TLS。默认 false
     */
    public Boolean useTls;
}
