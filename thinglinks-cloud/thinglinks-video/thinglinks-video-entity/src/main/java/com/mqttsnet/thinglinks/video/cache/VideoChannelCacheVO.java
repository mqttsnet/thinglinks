package com.mqttsnet.thinglinks.video.cache;

import com.mqttsnet.thinglinks.video.dto.device.config.VideoChannelConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 通道缓存 VO
 * 用于 Redis 缓存中存储通道信息
 *
 * @author mqttsnet
 * @date 2026-04-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoChannelCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceIdentification;
    private String channelIdentification;
    private Integer channelNo;
    private Integer channelType;
    private String channelName;
    private String streamIdentification;
    private String streamType;
    private String manufacturer;
    private String model;
    private Boolean onlineStatus;
    private String host;
    private Integer port;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String fullAddress;
    private String provinceCode;
    private String cityCode;
    private String regionCode;
    private Boolean hasAudio;
    private Integer ptzType;
    private Boolean ptzCapability;
    private Boolean talkCapability;
    private Integer secrecy;
    private VideoChannelConfig channelConfig;
    private String extendParams;
    private String remark;
    private Long createdOrgId;
}
