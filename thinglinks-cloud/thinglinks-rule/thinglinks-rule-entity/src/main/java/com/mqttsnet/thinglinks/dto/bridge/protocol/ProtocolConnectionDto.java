package com.mqttsnet.thinglinks.dto.bridge.protocol;

/**
 * 协议级连接参数 DTO 标记接口（business 层契约）。
 *
 * <p>所有协议（Kafka / Redis / RocketMQ / RabbitMQ / MySQL / HTTP / WebHook / MQTT
 * / TDengine / ClickHouse / InfluxDB / IoTDB / PostgreSQL / MongoDB / Pulsar）的
 * {@code connection_json} 反序列化目标 DTO 都实现本接口，提供给 Service / VO / 校验层
 * 做泛型类型约束，<b>每协议独立 DTO，改 Kafka 字段不影响其它协议</b>（OCP）。
 *
 * <h3>命名规约</h3>
 * <ul>
 *   <li>协议名 + ConnectionDto 后缀：{@code KafkaConnectionDto} / {@code TDengineConnectionDto}</li>
 *   <li>包按协议分目录：{@code dto.bridge.protocol.kafka} / {@code dto.bridge.protocol.tdengine}</li>
 *   <li>字段名 = 前端 ProtocolModule.connectionFields() 的 field 名 = JSON key（单一真相）</li>
 *   <li>纯 POJO + public 字段（不用 Lombok）：避免 Jackson 反序列化干扰</li>
 *   <li>协议级校验由 Sink.testConnection 负责；DTO 层只描述形状</li>
 * </ul>
 *
 * <h3>与 util-pro Sink 内部 POJO 的关系</h3>
 * <p>util-pro 的每个 {@code Sink} 实现自带内部 POJO 用于 JSON 解析（Sink 封装细节，业务层不关心）。
 * 本 entity DTO 是 <b>service / 前端表单 / 校验</b> 共享的<b>canonical 类型</b>，与 Sink 内部 POJO
 * 字段名严格对齐（相当于"同一份契约的两个副本"）。修改字段必须前后端一起改。
 *
 * @author mqttsnet
 */
public interface ProtocolConnectionDto {
}
