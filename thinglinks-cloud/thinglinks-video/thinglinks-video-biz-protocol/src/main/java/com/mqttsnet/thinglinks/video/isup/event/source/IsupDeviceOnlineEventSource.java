package com.mqttsnet.thinglinks.video.isup.event.source;

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
 * ISUP 设备上线事件源。
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
public class IsupDeviceOnlineEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备序列号
     */
    private String deviceSerial;

    /**
     * 地址(IP/域名)
     */
    private String host;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 通道数
     */
    private Integer channelCount;
}
