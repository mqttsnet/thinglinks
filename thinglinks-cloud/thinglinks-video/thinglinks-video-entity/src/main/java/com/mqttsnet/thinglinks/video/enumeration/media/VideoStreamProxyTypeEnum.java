package com.mqttsnet.thinglinks.video.empowerment.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * -----------------------------------------------------------------------------
 * File Name: VideoStreamProxyTypeEnum
 * -----------------------------------------------------------------------------
 * Description:
 * 流代理类型
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/10       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/10 15:50
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "VideoStreamProxyTypeEnum", description = "流代理类型")
public enum VideoStreamProxyTypeEnum {

    /**
     * Default proxy type
     */
    DEFAULT("default", "default"),

    /**
     * FFMpeg proxy type
     */
    FFMPEG("ffmpeg", "ffmpeg");

    private String value;
    private String desc;

    /**
     * 根据 proxyType 获取对应的枚举
     *
     * @param proxyType 流代理类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<VideoStreamProxyTypeEnum> fromValue(String proxyType) {
        return Stream.of(VideoStreamProxyTypeEnum.values())
                .filter(type -> type.getValue().equals(proxyType))
                .findFirst();
    }
}