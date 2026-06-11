package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * SSRC 前缀枚举。
 * GB28181 中 SSRC 为 10 位十进制数，第 1 位用于区分实时流和历史流：
 * - 实时流（Play）：首位为 0
 * - 历史流（Playback/Download）：首位为 1
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SsrcPrefixEnum", description = "SSRC前缀枚举")
public enum SsrcPrefixEnum {

    /**
     * 实时流前缀
     */
    PLAY("0", "实时流"),

    /**
     * 历史流前缀（回放/下载）
     */
    PLAYBACK("1", "历史流"),
    ;

    private String prefix;
    private String desc;

    /**
     * 根据前缀值查找枚举
     *
     * @param prefix 前缀值
     * @return 匹配的枚举实例
     */
    public static Optional<SsrcPrefixEnum> fromPrefix(String prefix) {
        return Stream.of(values())
                .filter(e -> e.getPrefix().equals(prefix))
                .findFirst();
    }
}
