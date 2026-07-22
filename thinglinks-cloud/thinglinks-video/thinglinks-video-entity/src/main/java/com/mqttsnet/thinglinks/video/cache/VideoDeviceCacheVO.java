package com.mqttsnet.thinglinks.video.cache;

import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.gb28181.SipTransactionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 设备缓存 VO
 * 用于 Redis 缓存中存储设备信息
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDeviceCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceIdentification;
    private String accessProtocol;
    private String deviceName;
    private String customName;
    private String manufacturer;
    private String model;
    private String firmware;
    private String host;
    private Integer port;
    private String wanHost;
    private String lanHost;
    private String accessEndpoint;
    private String sdpHost;
    private String localHost;
    private String transport;
    private String streamMode;
    private Boolean onlineStatus;
    private String registerTime;
    private String lastKeepaliveTime;
    private Integer expires;
    private Integer keepaliveInterval;
    private Integer keepaliveTimeoutCount;
    private String authType;
    private String authSecret;
    private String mediaIdentification;
    private Integer channelCount;
    private String ability;
    private VideoDeviceProtocolConfig protocolConfig;
    private String extendParams;
    private String remark;
    private Long createdOrgId;

    /**
     * SIP 事务信息（仅用于缓存，非 DB 字段）
     */
    private SipTransactionInfo sipTransactionInfo;
}
