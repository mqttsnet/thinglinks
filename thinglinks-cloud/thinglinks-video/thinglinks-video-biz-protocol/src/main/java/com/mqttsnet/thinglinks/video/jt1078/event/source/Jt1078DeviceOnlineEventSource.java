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
 * JT/T 1078 终端上线事件源。
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
public class Jt1078DeviceOnlineEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 终端SIM卡号
     */
    private String simNumber;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 地址(IP/域名)
     */
    private String host;

    /**
     * 终端端口
     */
    private Integer port;

    /**
     * 通道数
     */
    private Integer channelCount;
}
