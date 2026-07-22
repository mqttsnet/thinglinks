package com.mqttsnet.thinglinks.video.gb28181.event.source;

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
 * SSRC 池耗尽事件源。
 * 当 SSRC 池中无可用 SSRC 时发布，用于告警和监控。
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
public class SsrcPoolExhaustedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String mediaIdentification;
    private int poolSize;
}
