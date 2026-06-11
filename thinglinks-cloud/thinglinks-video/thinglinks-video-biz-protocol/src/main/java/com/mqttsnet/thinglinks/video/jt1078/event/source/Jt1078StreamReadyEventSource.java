package com.mqttsnet.thinglinks.video.jt1078.event.source;

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
 * JT/T 1078 流就绪事件源。
 * 当车载终端的音视频流到达流媒体服务器并可播放时发布。
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
public class Jt1078StreamReadyEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 终端SIM卡号
     */
    private String simNumber;

    /**
     * 逻辑通道号
     */
    private Integer channelNo;

    /**
     * 流媒体服务器标识
     */
    private String mediaIdentification;

    /**
     * 应用名
     */
    private String app;

    /**
     * 流名称
     */
    private String stream;
}
