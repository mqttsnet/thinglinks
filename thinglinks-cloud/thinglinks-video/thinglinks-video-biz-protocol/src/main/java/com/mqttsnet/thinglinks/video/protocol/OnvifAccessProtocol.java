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
 * ONVIF 协议接入实现。
 *
 * <p>ONVIF 标准 = SOAP 控制 + RTSP 取流。设备发现 / Profile 选择 / PTZ 等控制走 SOAP，
 * 实时拉流复用 RTSP 路径——所以 startRealPlay 直接委托给 {@link RtspPlayService}，
 * 前提是设备入库时把 {@code GetStreamUri} 拿到的 RTSP URL 写入 {@code protocol_config.streamSource.url}
 * （由 {@code OnvifService.importDevice} 负责）。
 *
 * <p>PTZ 等扩展能力的协议层调用预留 TODO，当前阶段先打通"发现 → 拉流 → 播放"主链路。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnvifAccessProtocol implements DeviceAccessProtocol {

    private final RtspPlayService rtspPlayService;

    @Override
    public AccessProtocolEnum getProtocolType() {
        return AccessProtocolEnum.ONVIF;
    }

    @Override
    public StreamInfo startRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("ONVIF startRealPlay: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
        return rtspPlayService.play(deviceIdentification, channelIdentification);
    }

    @Override
    public void stopRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("ONVIF stopRealPlay: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
        rtspPlayService.stop(deviceIdentification, channelIdentification);
    }

    @Override
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        return rtspPlayService.getStreamInfo(deviceIdentification, channelIdentification);
    }

    @Override
    public StreamInfo startPlayback(String deviceIdentification, String channelIdentification,
                                    LocalDateTime startTime, LocalDateTime endTime) {
        // ONVIF Profile G 支持回放，本期未集成，留 TODO
        throw new UnsupportedOperationException("ONVIF 录像回放暂未实现");
    }

    @Override
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        log.warn("ONVIF stopPlayback 未实现: deviceIdentification={}, channelIdentification={}",
                deviceIdentification, channelIdentification);
    }

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
        // PTZ SOAP 调用通路有，业务侧未对接，返回 false 避免上层误用
        return false;
    }

    @Override
    public boolean supportsBroadcast() {
        return false;
    }
}
