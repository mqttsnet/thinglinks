package com.mqttsnet.thinglinks.video.empowerment.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * -----------------------------------------------------------------------------
 * File Name: VideoMediaServerTypeEnum
 * -----------------------------------------------------------------------------
 * Description:
 * 流媒体服务类型
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/6       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/6 16:16
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "VideoMediaServerTypeEnum", description = "流媒体服务类型")
public enum VideoMediaServerTypeEnum {

    /**
     * ZLMediaKit
     */
    ZLM("zlm", "ZLMediaKit"),

    /**
     * ABLMediaServer
     */
    ABL("abl", "ABLMediaServer");

    private String value;
    private String desc;


    /**
     * 根据code获取对应的枚举
     *
     * @param value 流媒体服务类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<VideoMediaServerTypeEnum> fromValue(String value) {
        return Stream.of(VideoMediaServerTypeEnum.values())
                .filter(type -> type.getValue().equals(value))
                .findFirst();
    }
}
