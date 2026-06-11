package com.mqttsnet.thinglinks.video.enumeration.isup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * ISUP 告警类型枚举。
 * 定义海康 ISUP 协议支持的告警事件类型，
 * 包括移动侦测、视频丢失、越界检测、人脸检测等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "IsupAlarmTypeEnum", description = "ISUP告警类型枚举")
public enum IsupAlarmTypeEnum {

    /**
     * 移动侦测
     */
    MOTION_DETECTION("MOTION_DETECTION", "移动侦测"),

    /**
     * 视频丢失
     */
    VIDEO_LOSS("VIDEO_LOSS", "视频丢失"),

    /**
     * 视频遮挡
     */
    VIDEO_TAMPERING("VIDEO_TAMPERING", "视频遮挡"),

    /**
     * 越界检测
     */
    LINE_CROSSING("LINE_CROSSING", "越界检测"),

    /**
     * 区域入侵
     */
    INTRUSION("INTRUSION", "区域入侵"),

    /**
     * 人脸检测
     */
    FACE_DETECTION("FACE_DETECTION", "人脸检测"),

    /**
     * IO报警
     */
    IO_ALARM("IO_ALARM", "IO报警"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他"),
    ;

    /**
     * 告警类型标识
     */
    private String value;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 根据 value 查找枚举（忽略大小写）。
     *
     * @param value 告警类型标识值
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<IsupAlarmTypeEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
