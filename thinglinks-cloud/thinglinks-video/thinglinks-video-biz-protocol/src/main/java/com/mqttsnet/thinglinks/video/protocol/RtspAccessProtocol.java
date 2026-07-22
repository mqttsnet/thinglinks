package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.service.stream.RtspPlayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * RTSP 协议接入实现。
 *
 * <p>RTSP 属于"平台主动拉流"模型——设备暴露 RTSP URL，平台通过 ZLMediaKit 的 addStreamProxy
 * 代理拉流，落地为 RTMP / HTTP-FLV / HLS / WebRTC 等多协议供浏览器播放。
 *
 * <p>支持能力：
 * <ul>
 *   <li>实时预览（startRealPlay / stopRealPlay）</li>
 *   <li>不支持录像回放：需要设备端支持 RTSP {@code PLAY Range} 且平台未实现</li>
 *   <li>不支持 PTZ：RTSP 本身不定义 PTZ 控制（ONVIF 是其扩展）</li>
 *   <li>不支持语音广播：需要 RTSP {@code ANNOUNCE + RECORD}，少有设备支持</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-25
 * @see RtspPlayService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RtspAccessProtocol implements DeviceAccessProtocol {

    private final RtspPlayService rtspPlayService;

    @Override
    public AccessProtocolEnum getProtocolType() {
        return AccessProtocolEnum.RTSP;
    }

    // ========== 实时预览 ==========

    @Override
    public StreamInfo startRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("RTSP startRealPlay: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
        return rtspPlayService.play(deviceIdentification, channelIdentification);
    }

    @Override
    public void stopRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("RTSP stopRealPlay: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
        rtspPlayService.stop(deviceIdentification, channelIdentification);
    }

    @Override
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        return rtspPlayService.getStreamInfo(deviceIdentification, channelIdentification);
    }

    // ========== 录像回放（RTSP 协议本身支持 Range 回放，但目前平台未实现）==========

    @Override
    public StreamInfo startPlayback(String deviceIdentification, String channelIdentification,
                                    LocalDateTime startTime, LocalDateTime endTime) {
        throw new UnsupportedOperationException("RTSP 协议目前不支持录像回放");
    }

    @Override
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        log.warn("RTSP stopPlayback 未实现: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
    }

    // ========== 能力查询 ==========

    @Override
    public boolean supportsRealPlay() {
        return true;
    }

    @Override
    public boolean supportsPlayback() {
        return false;
    }

    @Override
    public boolean supportsPtz() {
        return false;
    }

    @Override
    public boolean supportsBroadcast() {
        return false;
    }
}
