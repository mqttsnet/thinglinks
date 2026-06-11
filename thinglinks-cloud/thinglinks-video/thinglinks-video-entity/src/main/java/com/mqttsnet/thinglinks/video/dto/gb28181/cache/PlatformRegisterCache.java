package com.mqttsnet.thinglinks.video.dto.gb28181.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台级联注册缓存对象。
 * <p>
 * 存储上级平台注册状态的可序列化子集，用于 Redis 驱动的续期和心跳调度。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRegisterCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long platformId;

    private Long tenantId;

    private String serverGbId;

    private String serverGbDomain;

    private String serverIp;

    private Integer serverPort;

    private String deviceGbId;

    private String transport;

    private String sipIp;

    private Integer sipPort;

    private Integer expires;

    private Integer registerExpires;

    private String username;

    private String password;

    private String callId;

    private Long registeredAt;
}
