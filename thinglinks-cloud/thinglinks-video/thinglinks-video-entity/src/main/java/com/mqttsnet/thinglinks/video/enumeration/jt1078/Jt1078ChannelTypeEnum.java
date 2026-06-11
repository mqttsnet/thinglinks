package com.mqttsnet.thinglinks.video.enumeration.jt1078;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * JT/T 1078 逻辑通道类型枚举。
 * 定义车载终端各摄像头通道的类型，
 * 通道号与安装位置对应关系参照 JT/T 1076-2016 表2。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Jt1078ChannelTypeEnum", description = "JT/T 1078逻辑通道类型枚举")
public enum Jt1078ChannelTypeEnum {

    /**
     * 前置摄像头
     */
    FRONT_CAMERA(1, "前置摄像头"),

    /**
     * 驾驶员摄像头
     */
    DRIVER_CAMERA(2, "驾驶员摄像头"),

    /**
     * 客舱摄像头
     */
    PASSENGER_CAMERA(3, "客舱摄像头"),

    /**
     * 车门摄像头
     */
    DOOR_CAMERA(4, "车门摄像头"),

    /**
     * 车外前方
     */
    OUTSIDE_FRONT(5, "车外前方"),

    /**
     * 车外后方
     */
    OUTSIDE_REAR(6, "车外后方"),

    /**
     * 其他
     */
    OTHER(0, "其他"),
    ;

    /**
     * 通道类型编号
     */
    private Integer value;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 根据通道类型编号查找枚举。
     *
     * @param value 通道类型编号
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<Jt1078ChannelTypeEnum> fromValue(Integer value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }
}
