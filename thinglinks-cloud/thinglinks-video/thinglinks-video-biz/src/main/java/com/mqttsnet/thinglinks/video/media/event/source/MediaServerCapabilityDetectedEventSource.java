package com.mqttsnet.thinglinks.video.media.event.source;

import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.thinglinks.video.enumeration.media.MediaServerCapabilityEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * Description:
 * 流媒体服务器能力检测完成事件源。
 * 携带服务器支持的能力集信息。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaServerCapabilityDetectedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流媒体服务器标识
     */
    private String mediaIdentification;

    /**
     * 服务器类型（zlm/abl）
     */
    private String serverType;

    /**
     * 支持的能力集
     */
    private Set<MediaServerCapabilityEnum> capabilities;
}
