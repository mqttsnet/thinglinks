package com.mqttsnet.thinglinks.video.media.event.source;

import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 流媒体服务器注册事件源。
 * 携带流媒体服务器注册时的完整上下文信息。
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
public class MediaServerRegisteredEventSource extends Entity<Long> implements Serializable {
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
     * 地址(IP/域名)
     */
    private String host;

    /**
     * HTTP 端口
     */
    private Integer httpPort;
}
