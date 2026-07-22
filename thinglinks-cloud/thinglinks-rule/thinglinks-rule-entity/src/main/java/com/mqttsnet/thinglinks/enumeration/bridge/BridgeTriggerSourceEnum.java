package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 桥接 trace 触发来源（rule_bridge_execution_trace.trigger_source 字段）。
 * 用于在日志页过滤"哪种来源的桥接执行"，便于排查不同 ingress 路径。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeTriggerSourceEnum", description = "桥接触发来源枚举")
public enum BridgeTriggerSourceEnum {

    /**
     * 设备数据 ── mqs 旁路投递的设备事件触发的桥接。
     */
    DEVICE_DATA("DEVICE_DATA", "设备数据"),

    /**
     * 订阅源 ── 入站订阅源拉到的消息触发的桥接。
     */
    SUBSCRIPTION("SUBSCRIPTION", "订阅源"),

    /**
     * 测试发送 ── 编辑表单测试按钮触发的发送。
     */
    TEST_SINK("TEST_SINK", "测试发送"),

    /**
     * 死信回放 ── 日志页死信重试按钮触发。
     */
    REPLAY("REPLAY", "死信回放");

    private String value;
    private String desc;

    public static Optional<BridgeTriggerSourceEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeTriggerSourceEnum.values())
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
