package com.mqttsnet.thinglinks.video.gb28181.event.source;

import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * Invite 会话创建事件源。
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
public class InviteSessionCreatedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceIdentification;
    private String channelIdentification;
    private String callId;
    private String ssrc;
    private InviteSessionTypeEnum sessionType;
    /** 租户ID */
    private Long tenantId;
}
