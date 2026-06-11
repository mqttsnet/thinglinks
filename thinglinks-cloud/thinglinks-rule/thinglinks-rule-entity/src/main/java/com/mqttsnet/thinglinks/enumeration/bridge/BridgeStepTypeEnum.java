package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 桥接执行步骤类型（rule_bridge_execution_step.step_type 字段）。
 * 一次桥接执行（trace）按以下步骤展开：
 * </p>
 *
 * <pre>
 * 出站 (出站规则触发后)：
 *   INGEST → RULE_MATCH → RATE_LIMIT → TRANSFORM → SINK_SEND → DEAD_LETTER (失败兜底)
 *
 * 入站 (订阅源拉到消息后)：
 *   INGEST → INBOUND_FORWARD → SINK_SEND (写 DeviceAction 等) → DEAD_LETTER (失败兜底)
 * </pre>
 *
 * 前端"链路回放"详情抽屉按 stepType 渲染不同的子卡片（输入/输出/扩展信息样式）。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeStepTypeEnum", description = "桥接执行步骤类型枚举")
public enum BridgeStepTypeEnum {

    /**
     * 数据接入 ── 收到消息后第一步：解码 envelope / 校验字段。
     */
    INGEST("INGEST", "设备数据接入"),

    /**
     * 规则匹配 ── 命中桥接规则的过滤条件（productId / topic / payload 内容等）。
     */
    RULE_MATCH("RULE_MATCH", "规则匹配"),

    /**
     * 限流 ── 按规则 / 数据源级 QPS 配置；超额返回 status=02-跳过。
     */
    RATE_LIMIT("RATE_LIMIT", "限流"),

    /**
     * 脚本转换 ── 调 Groovy 脚本做 payload 转换 / 字段映射。
     */
    TRANSFORM("TRANSFORM", "Groovy 转换"),

    /**
     * Sink 投递 ── 调用具体 Sink 实现（KafkaSink/HttpSink 等）发送到下游。
     */
    SINK_SEND("SINK_SEND", "Sink 投递"),

    /**
     * 死信投递 ── 超过重试次数后投递到死信数据源。
     */
    DEAD_LETTER("DEAD_LETTER", "死信"),

    /**
     * 入站还原 ── 入站消息按 target_handler 还原成平台事件（伪装设备 publish 等）。
     */
    INBOUND_FORWARD("INBOUND_FORWARD", "入站还原");

    private String value;
    private String desc;

    public static Optional<BridgeStepTypeEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeStepTypeEnum.values())
                        .filter(e -> e.getValue().equals(v))
                        .findFirst());
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
