package com.mqttsnet.thinglinks.video.dto.gb28181.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * SIP Subscribe 订阅缓存对象。
 * <p>
 * SubscribeInfo 的可序列化子集（排除 SIPRequest/SIPResponse）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeHolderCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private int expires;

    private String eventId;

    private String eventType;

    private String sn;

    private int gpsInterval;

    private String simulatedFromTag;

    private String simulatedToTag;

    private String simulatedCallId;
}
