package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 桥接执行整体状态（rule_bridge_execution_trace.status 字段）。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeMessageStatusEnum", description = "桥接执行整体状态枚举")
public enum BridgeMessageStatusEnum {

    SUCCESS("00", "成功"),
    FAILED("01", "失败"),
    PARTIAL("02", "部分成功"),
    DEAD_LETTER("03", "死信");

    private String value;
    private String desc;

    public static Optional<BridgeMessageStatusEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeMessageStatusEnum.values())
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
