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
 * 播放失败事件源。
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
public class PlayFailedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceIdentification;
    private String channelIdentification;
    private String mediaIdentification;
    private String failureReason;
    private String errorMessage;
    /** 租户ID */
    private Long tenantId;
}
