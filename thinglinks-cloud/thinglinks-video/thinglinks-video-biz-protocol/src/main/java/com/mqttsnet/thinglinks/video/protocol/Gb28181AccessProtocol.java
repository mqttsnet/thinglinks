package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.service.stream.PlayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description:
 * GB/T 28181 协议接入实现。
 *
 * <p>委托给现有的 {@link PlayService} 等服务完成具体操作，
 * 本类只做协议路由层，不包含业务逻辑。
 *
 * <p>此实现完全独立于 ISUP 和 JT1078，修改此类不影响其他协议。
 * GB28181 协议支持全部能力：实时预览、录像回放、PTZ 控制、语音广播等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see DeviceAccessProtocol
 * @see DeviceAccessProtocolFactory
 * @see PlayService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Gb28181AccessProtocol implements DeviceAccessProtocol {

    private final PlayService playService;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessProtocolEnum getProtocolType() {
        return AccessProtocolEnum.GB28181;
    }

    // ========== 实时预览 ==========

    /**
     * {@inheritDoc}
     * <p>委托给 {@link PlayService#play(String, String)} 执行。
     */
    @Override
    public StreamInfo startRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("GB28181 startRealPlay: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        return playService.play(deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     * <p>委托给 {@link PlayService#stop(String, String)} 执行。
     */
    @Override
    public void stopRealPlay(String deviceIdentification, String channelIdentification) {
        log.debug("GB28181 stopRealPlay: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        playService.stop(deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     * <p>委托给 {@link PlayService#getStreamInfo(String, String)} 执行。
     */
    @Override
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        return playService.getStreamInfo(deviceIdentification, channelIdentification);
    }

    // ========== 录像回放 ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamInfo startPlayback(String deviceIdentification, String channelIdentification, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: delegate to PlaybackService when available
        throw new UnsupportedOperationException("PlaybackService integration pending");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        // TODO: delegate to PlaybackService when available
        log.warn("GB28181 stopPlayback: PlaybackService integration pending, deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pausePlayback(String deviceIdentification, String channelIdentification) {
        // TODO: delegate to PlaybackService when available
        log.warn("GB28181 pausePlayback: PlaybackService integration pending, deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resumePlayback(String deviceIdentification, String channelIdentification) {
        // TODO: delegate to PlaybackService when available
        log.warn("GB28181 resumePlayback: PlaybackService integration pending, deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlaybackSpeed(String deviceIdentification, String channelIdentification, Double speed) {
        // TODO: delegate to PlaybackService when available
        log.warn("GB28181 setPlaybackSpeed: PlaybackService integration pending, deviceIdentification={}, channelIdentification={}, speed={}", deviceIdentification, channelIdentification, speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekPlayback(String deviceIdentification, String channelIdentification, Long seekSeconds) {
        // TODO: delegate to PlaybackService when available
        log.warn("GB28181 seekPlayback: PlaybackService integration pending, deviceIdentification={}, channelIdentification={}, seekSeconds={}", deviceIdentification, channelIdentification, seekSeconds);
    }

    // ========== PTZ 控制 ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public void ptzDirection(String deviceIdentification, String channelIdentification, String direction, Integer speed) {
        // TODO: delegate to PtzService when available
        log.warn("GB28181 ptzDirection: PtzService integration pending, deviceIdentification={}, channelIdentification={}, direction={}, speed={}", deviceIdentification, channelIdentification, direction, speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ptzStop(String deviceIdentification, String channelIdentification) {
        // TODO: delegate to PtzService when available
        log.warn("GB28181 ptzStop: PtzService integration pending, deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ptzZoom(String deviceIdentification, String channelIdentification, String zoomType, Integer speed) {
        // TODO: delegate to PtzService when available
        log.warn("GB28181 ptzZoom: PtzService integration pending, deviceIdentification={}, channelIdentification={}, zoomType={}, speed={}", deviceIdentification, channelIdentification, zoomType, speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ptzPreset(String deviceIdentification, String channelIdentification, String command, Integer presetId) {
        // TODO: delegate to PtzService when available
        log.warn("GB28181 ptzPreset: PtzService integration pending, deviceIdentification={}, channelIdentification={}, command={}, presetId={}", deviceIdentification, channelIdentification, command, presetId);
    }

    // ========== 设备控制 ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public void reboot(String deviceIdentification) {
        // TODO: delegate to DeviceControlService when available
        log.warn("GB28181 reboot: DeviceControlService integration pending, deviceIdentification={}", deviceIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void guard(String deviceIdentification, Boolean enable) {
        // TODO: delegate to DeviceControlService when available
        log.warn("GB28181 guard: DeviceControlService integration pending, deviceIdentification={}, enable={}", deviceIdentification, enable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordControl(String deviceIdentification, String channelIdentification, Boolean start) {
        // TODO: delegate to DeviceControlService when available
        log.warn("GB28181 recordControl: DeviceControlService integration pending, deviceIdentification={}, channelIdentification={}, start={}", deviceIdentification, channelIdentification, start);
    }

    // ========== 设备查询 ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public void queryCatalog(String deviceIdentification) {
        // TODO: delegate to QueryCommander when available
        log.warn("GB28181 queryCatalog: QueryCommander integration pending, deviceIdentification={}", deviceIdentification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queryDeviceInfo(String deviceIdentification) {
        // TODO: delegate to QueryCommander when available
        log.warn("GB28181 queryDeviceInfo: QueryCommander integration pending, deviceIdentification={}", deviceIdentification);
    }

    // ========== 告警 ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeAlarm(String deviceIdentification) {
        // TODO: delegate to SubscribeCommander when available
        log.warn("GB28181 subscribeAlarm: SubscribeCommander integration pending, deviceIdentification={}", deviceIdentification);
    }

    // ========== 能力查询 ==========

    /**
     * {@inheritDoc}
     *
     * @return {@code true}，GB28181 支持实时播放
     */
    @Override
    public boolean supportsRealPlay() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}，GB28181 支持录像回放
     */
    @Override
    public boolean supportsPlayback() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}，GB28181 支持 PTZ 控制
     */
    @Override
    public boolean supportsPtz() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}，GB28181 支持语音广播
     */
    @Override
    public boolean supportsBroadcast() {
        return true;
    }
}
