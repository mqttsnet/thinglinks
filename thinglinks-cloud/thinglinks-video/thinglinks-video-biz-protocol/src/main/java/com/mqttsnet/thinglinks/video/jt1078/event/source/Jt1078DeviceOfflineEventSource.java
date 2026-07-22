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
 * JT/T 1078 终端离线事件源。
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
public class Jt1078DeviceOfflineEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 终端SIM卡号
     */
    private String simNumber;

    /**
     * 离线原因
     */
    private String reason;
}
