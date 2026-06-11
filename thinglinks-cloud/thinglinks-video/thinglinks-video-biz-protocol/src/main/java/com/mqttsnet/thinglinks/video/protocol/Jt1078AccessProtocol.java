package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.jt1078.cmd.Jt1078ControlCommander;
import com.mqttsnet.thinglinks.video.jt1078.cmd.Jt1078PlayCommander;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description:
 * JT/T 1078 协议接入实现。
 *
 * <p>实现 {@link DeviceAccessProtocol} 统一接入接口，
 * 内部委托给 {@link Jt1078PlayCommander} 和 {@link Jt1078ControlCommander} 完成指令发送。
 *
 * <p>JT/T 1078 协议特点：
 * <ul>
 *     <li>支持实时音视频（supportsRealPlay = true）</li>
 *     <li>支持历史视频回放（supportsPlayback = true）</li>
 *     <li>不支持 PTZ 控制（supportsPtz = false），车载终端无云台</li>
 *     <li>不支持语音广播（supportsBroadcast = false）</li>
 * </ul>
 *
 * <p>设备标识使用 SIM 卡号，通道标识使用逻辑通道号的字符串形式。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see DeviceAccessProtocol
 * @see AccessProtocolEnum#JT1078
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Jt1078AccessProtocol implements DeviceAccessProtocol {

    private final Jt1078PlayCommander playCommander;
    private final Jt1078ControlCommander controlCommander;

    @Override
    public AccessProtocolEnum getProtocolType() {
        return AccessProtocolEnum.JT1078;
    }

    @Override
    public StreamInfo startRealPlay(String deviceIdentification, String channelIdentification) {
        log.info("[JT1078] startRealPlay: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        Integer channelNo = Integer.parseInt(channelIdentification);
        // TODO: 从流媒体服务器获取实际的 IP/Port，这里暂时传空，后续集成时补充
        playCommander.startRealPlay(deviceIdentification, channelNo, "MAIN", null, null);
        // 返回占位 StreamInfo，实际流信息需等流就绪事件后填充
        return new StreamInfo();
    }

    @Override
    public void stopRealPlay(String deviceIdentification, String channelIdentification) {
        log.info("[JT1078] stopRealPlay: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        Integer channelNo = Integer.parseInt(channelIdentification);
        playCommander.stopRealPlay(deviceIdentification, channelNo);
    }

    @Override
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        log.debug("[JT1078] getStreamInfo: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        // TODO: 从流信息缓存中获取，后续集成时补充
        return Optional.empty();
    }

    @Override
    public StreamInfo startPlayback(String deviceIdentification, String channelIdentification,
                                    LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[JT1078] startPlayback: simNumber={}, channelNo={}, startTime={}, endTime={}",
                deviceIdentification, channelIdentification, startTime, endTime);
        Integer channelNo = Integer.parseInt(channelIdentification);
        playCommander.startPlayback(deviceIdentification, channelNo, startTime, endTime, null, null);
        return new StreamInfo();
    }

    @Override
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        log.info("[JT1078] stopPlayback: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        Integer channelNo = Integer.parseInt(channelIdentification);
        playCommander.stopPlayback(deviceIdentification, channelNo);
    }

    @Override
    public void pausePlayback(String deviceIdentification, String channelIdentification) {
        log.info("[JT1078] pausePlayback: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        Integer channelNo = Integer.parseInt(channelIdentification);
        // controlType=1 表示暂停
        controlCommander.playbackControl(deviceIdentification, channelNo, 1, 0);
    }

    @Override
    public void resumePlayback(String deviceIdentification, String channelIdentification) {
        log.info("[JT1078] resumePlayback: simNumber={}, channelNo={}", deviceIdentification, channelIdentification);
        Integer channelNo = Integer.parseInt(channelIdentification);
        // controlType=0 表示正常播放
        controlCommander.playbackControl(deviceIdentification, channelNo, 0, 0);
    }

    @Override
    public void setPlaybackSpeed(String deviceIdentification, String channelIdentification, Double speed) {
        log.info("[JT1078] setPlaybackSpeed: simNumber={}, channelNo={}, speed={}",
                deviceIdentification, channelIdentification, speed);
        Integer channelNo = Integer.parseInt(channelIdentification);
        // controlType=3 表示快进
        controlCommander.playbackControl(deviceIdentification, channelNo, 3, speed.intValue());
    }

    @Override
    public boolean supportsRealPlay() {
        return true;
    }

    @Override
    public boolean supportsPlayback() {
        return true;
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
