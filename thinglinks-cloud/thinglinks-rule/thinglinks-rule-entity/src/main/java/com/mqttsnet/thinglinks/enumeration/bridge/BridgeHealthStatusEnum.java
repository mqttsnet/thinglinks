package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 数据源健康状态（rule_data_source.health_status 字段）。
 * 由 HealthCheckScheduler 每 5min 探活后更新。前端列表页用绿/黄/红圆点展示。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeHealthStatusEnum", description = "数据源健康状态枚举")
public enum BridgeHealthStatusEnum {

    /**
     * 健康 ── 探活成功。
     */
    HEALTHY("HEALTHY", "健康"),

    /**
     * 降级 ── 探活成功但有警告（如响应超过阈值）。
     */
    DEGRADED("DEGRADED", "降级"),

    /**
     * 不可用 ── 探活失败。
     */
    DOWN("DOWN", "不可用"),

    /**
     * 未知 ── 尚未探活 / 数据源刚创建。
     */
    UNKNOWN("UNKNOWN", "未知");

    private String value;
    private String desc;

    public static Optional<BridgeHealthStatusEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeHealthStatusEnum.values())
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
