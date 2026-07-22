package com.mqttsnet.thinglinks.video.dto.gb28181.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * MESSAGE 消息订阅缓存对象。
 * <p>
 * 存储在 Redis 中，记录 SIP MESSAGE 事务的状态和结果。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgSubscribeCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String cmdType;

    private String sn;

    private String deviceIdentification;

    /**
     * 结果码
     */
    private Integer resultCode;

    /**
     * 结果消息
     */
    private String resultMsg;

    /**
     * 创建时间戳
     */
    private Long createdAt;
}
