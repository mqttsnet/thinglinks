package com.mqttsnet.thinglinks.dto.ws.cache;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * ============================================================================
 * Description:
 * WebSocket Session Cache
 * ============================================================================
 *
 * @author mqttsnet
 * @version 1.0.0
 * @email
 * @date 2024/3/ 11:42
 */
@Data
@Builder
public class WebSocketSessionCacheDTO {

    private String sessionId;

    private String clientId;

    private Long tenantId;

    private String protocolVersion;

    private LocalDateTime connectTime;
}
