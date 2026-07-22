package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 桥接方向枚举（rule_data_source.direction / rule_data_bridge.direction 字段）
 * </p>
 *
 * <ul>
 *   <li>{@link #OUTBOUND}（10）：平台 → 第三方</li>
 *   <li>{@link #INBOUND}（20）：第三方 → 平台</li>
 *   <li>{@link #BIDIRECTIONAL}（30）：双向，既可作出站 sink 也可作入站 source</li>
 * </ul>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeDirectionEnum", description = "桥接方向枚举")
public enum BridgeDirectionEnum {

    OUTBOUND("10", "出站"),
    INBOUND("20", "入站"),
    BIDIRECTIONAL("30", "双向");

    private String value;
    private String desc;

    public static Optional<BridgeDirectionEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeDirectionEnum.values())
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
