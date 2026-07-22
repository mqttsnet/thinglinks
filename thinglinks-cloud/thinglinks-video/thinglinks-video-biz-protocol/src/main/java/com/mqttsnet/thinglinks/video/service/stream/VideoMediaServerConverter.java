package com.mqttsnet.thinglinks.video.service.stream;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;

/**
 * {@link VideoMediaServerResultDTO} → {@link VideoMediaServer} 转换工具。
 * <p>
 * PlayService / PlaybackService / DownloadService / BroadcastService 之前各自内部维护
 * 一份 ~24 行的 DTO→Entity 复制函数，改动字段时需同步四处，容易漏改。统一抽到此处。
 *
 * @author mqttsnet
 */
public final class VideoMediaServerConverter {

    private VideoMediaServerConverter() {
    }

    public static VideoMediaServer toEntity(VideoMediaServerResultDTO dto) {
        VideoMediaServer entity = new VideoMediaServer();
        entity.setId(dto.getId());
        entity.setMediaIdentification(dto.getMediaIdentification());
        entity.setHost(dto.getHost());
        entity.setHttpPort(dto.getHttpPort());
        entity.setHttpSslPort(dto.getHttpSslPort());
        entity.setSecret(dto.getSecret());
        entity.setType(dto.getType());
        entity.setRtpEnable(dto.getRtpEnable());
        entity.setRtpPortRange(dto.getRtpPortRange());
        entity.setRtpProxyPort(dto.getRtpProxyPort());
        entity.setHookHost(dto.getHookHost());
        entity.setSdpHost(dto.getSdpHost());
        entity.setStreamHost(dto.getStreamHost());
        entity.setRtmpPort(dto.getRtmpPort());
        entity.setRtmpSslPort(dto.getRtmpSslPort());
        entity.setRtspPort(dto.getRtspPort());
        entity.setRtspSslPort(dto.getRtspSslPort());
        entity.setFlvPort(dto.getFlvPort());
        entity.setFlvSslPort(dto.getFlvSslPort());
        entity.setWsFlvPort(dto.getWsFlvPort());
        entity.setWsFlvSslPort(dto.getWsFlvSslPort());
        return entity;
    }
}
