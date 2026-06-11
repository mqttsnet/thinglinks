package com.mqttsnet.thinglinks.bridge.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 桥接执行步骤类型(落 {@code bridge_execution_step.step_type})。
 * 链路回放 UI 按 stepType 派发不同子卡片渲染。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Getter
@AllArgsConstructor
@Schema(title = "BridgeStepType", description = "桥接执行步骤类型枚举")
public enum BridgeStepType {

    /**
     * mqs → rule 消息接入。
     */
    INGEST("INGEST", "数据接入"),
    /**
     * matcher 命中条件细节。
     */
    RULE_MATCH("RULE_MATCH", "规则匹配"),
    /**
     * 规则级 QPS 限流。
     */
    RATE_LIMIT("RATE_LIMIT", "限流"),
    /**
     * Groovy / JS / 模板渲染。
     */
    TRANSFORM("TRANSFORM", "脚本转换"),
    /**
     * Sink.send() 出站。
     */
    SINK_SEND("SINK_SEND", "投递"),
    /**
     * 超过 retryMaxTimes 后投死信。
     */
    DEAD_LETTER("DEAD_LETTER", "死信"),
    /**
     * 第三方消息 → 平台 ProtocolHandler。
     */
    INBOUND_FORWARD("INBOUND_FORWARD", "入站还原");

    private final String value;
    /**
     * 中文名(步骤卡片显示)。
     */
    private final String desc;
}
