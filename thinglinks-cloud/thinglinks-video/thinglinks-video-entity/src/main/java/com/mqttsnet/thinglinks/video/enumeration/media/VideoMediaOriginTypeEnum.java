package com.mqttsnet.thinglinks.video.empowerment.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * -----------------------------------------------------------------------------
 * File Name: VideoMediaOriginTypeEnum
 * -----------------------------------------------------------------------------
 * Description:
 * 多媒体产生源类型
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/7       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/7 14:52
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "VideoMediaOriginTypeEnum", description = "产生源类型")
public enum VideoMediaOriginTypeEnum {


    UNKNOWN(0, "unknown"),
    RTMP_PUSH(1, "rtmp_push"),
    RTSP_PUSH(2, "rtsp_push"),
    RTP_PUSH(3, "rtp_push"),
    PULL(4, "pull"),
    FFMPEG_PULL(5, "ffmpeg_pull"),
    MP4_VOD(6, "mp4_vod"),
    DEVICE_CHN(7, "device_chn");

    private Integer value;
    private String desc;

    /**
     * 根据value获取对应的枚举
     *
     * @param value 产生源类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<VideoMediaOriginTypeEnum> fromValue(Integer value) {
        return Stream.of(VideoMediaOriginTypeEnum.values())
                .filter(type -> Objects.equals(type.getValue(), value))
                .findFirst();
    }

}
