package com.mqttsnet.thinglinks.video.enumeration.jt1078;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * JT/T 1078 码流类型枚举。
 * 车载终端支持主码流和子码流两种传输模式。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Jt1078StreamTypeEnum", description = "JT/T 1078码流类型枚举")
public enum Jt1078StreamTypeEnum {

    /**
     * 主码流
     */
    MAIN("MAIN", "主码流"),

    /**
     * 子码流
     */
    SUB("SUB", "子码流"),
    ;

    /**
     * 码流类型标识
     */
    private String value;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 根据 value 查找枚举（忽略大小写）。
     *
     * @param value 码流类型标识
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<Jt1078StreamTypeEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
