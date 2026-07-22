package com.mqttsnet.thinglinks.video.dto.gb28181.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * SIP 事务订阅缓存对象。
 * <p>
 * 存储在 Redis 中，记录 SIP 事务的状态和结果。
 * 本地 JVM 通过 CompletableFuture 等待结果，Redis 提供分布式可观测性。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipSubscribeCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 订阅 key（callId + cSeqNumber）
     */
    private String key;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 事件结果类型（timeout / response / transactionTerminated 等）
     */
    private String type;

    /**
     * 消息描述
     */
    private String msg;

    /**
     * SIP Call-ID
     */
    private String callId;

    /**
     * 创建时间戳
     */
    private Long createdAt;
}
