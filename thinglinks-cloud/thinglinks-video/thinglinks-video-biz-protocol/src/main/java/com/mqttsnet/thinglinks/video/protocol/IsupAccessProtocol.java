package com.mqttsnet.thinglinks.video.protocol;

import cn.hutool.core.util.NumberUtil;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.device.AccessProtocolEnum;
import com.mqttsnet.thinglinks.video.isup.cmd.IsupAlarmCommander;
import com.mqttsnet.thinglinks.video.isup.cmd.IsupPlayCommander;
import com.mqttsnet.thinglinks.video.isup.cmd.IsupPtzCommander;
import com.mqttsnet.thinglinks.video.dto.isup.IsupConnectionInfo;
import com.mqttsnet.thinglinks.video.manager.isup.IsupConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description:
 * ISUP 协议接入实现。
 * 实现 {@link DeviceAccessProtocol} 接口，将 ISUP 协议的设备操作
 * 委托给各命令执行器，与 GB28181、JT1078 完全隔离。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see DeviceAccessProtocol
 * @see IsupPlayCommander
 * @see IsupPtzCommander
 * @see IsupAlarmCommander
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupAccessProtocol implements DeviceAccessProtocol {

    private final IsupPlayCommander isupPlayCommander;
    private final IsupPtzCommander isupPtzCommander;
    private final IsupAlarmCommander isupAlarmCommander;
    private final IsupConnectionManager isupConnectionManager;

    @Override
    public AccessProtocolEnum getProtocolType() {
        return AccessProtocolEnum.ISUP;
    }

    @Override
    public StreamInfo startRealPlay(String deviceIdentification, String channelIdentification) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        Integer playHandle = isupPlayCommander.startRealPlay(conn.userId(), channelNo, "main");
        if (playHandle == null) {
            throw new UnsupportedOperationException("ISUP实时预览启动失败: deviceIdentification=" + deviceIdentification);
        }
        log.info("ISUP实时预览已启动: deviceIdentification={}, channelIdentification={}, playHandle={}", deviceIdentification, channelIdentification, playHandle);
        return new StreamInfo();
    }

    @Override
    public void stopRealPlay(String deviceIdentification, String channelIdentification) {
        log.info("ISUP停止实时预览: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        // 实际场景需要通过会话管理获取 playHandle，此处为框架占位
    }

    @Override
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        // 流信息管理后续完善
        return Optional.empty();
    }

    @Override
    public StreamInfo startPlayback(String deviceIdentification, String channelIdentification, LocalDateTime startTime, LocalDateTime endTime) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        Integer playbackHandle = isupPlayCommander.startPlayback(conn.userId(), channelNo, startTime, endTime);
        if (playbackHandle == null) {
            throw new UnsupportedOperationException("ISUP录像回放启动失败: deviceIdentification=" + deviceIdentification);
        }
        log.info("ISUP录像回放已启动: deviceIdentification={}, channelIdentification={}, playbackHandle={}", deviceIdentification, channelIdentification, playbackHandle);
        return new StreamInfo();
    }

    @Override
    public void stopPlayback(String deviceIdentification, String channelIdentification) {
        log.info("ISUP停止录像回放: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
    }

    @Override
    public void ptzDirection(String deviceIdentification, String channelIdentification, String direction, Integer speed) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        isupPtzCommander.directionControl(conn.userId(), channelNo, direction, speed);
    }

    @Override
    public void ptzStop(String deviceIdentification, String channelIdentification) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        isupPtzCommander.ptzStop(conn.userId(), channelNo);
    }

    @Override
    public void ptzZoom(String deviceIdentification, String channelIdentification, String zoomType, Integer speed) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        isupPtzCommander.zoomControl(conn.userId(), channelNo, zoomType, speed);
    }

    @Override
    public void ptzPreset(String deviceIdentification, String channelIdentification, String command, Integer presetId) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        Integer channelNo = parseChannelNo(channelIdentification);
        switch (command.toUpperCase()) {
            case "SET" -> isupPtzCommander.presetSet(conn.userId(), channelNo, presetId);
            case "CALL" -> isupPtzCommander.presetCall(conn.userId(), channelNo, presetId);
            case "DELETE" -> isupPtzCommander.presetDelete(conn.userId(), channelNo, presetId);
            default -> throw new IllegalArgumentException("不支持的预置位命令: " + command);
        }
    }

    @Override
    public void subscribeAlarm(String deviceIdentification) {
        IsupConnectionInfo conn = getConnectionOrThrow(deviceIdentification);
        isupAlarmCommander.subscribeAlarm(conn.userId());
    }

    @Override
    public StreamInfo startBroadcast(String deviceIdentification, String channelIdentification) {
        log.info("ISUP语音对讲: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        return new StreamInfo();
    }

    @Override
    public void stopBroadcast(String deviceIdentification, String channelIdentification) {
        log.info("ISUP停止语音对讲: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
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
        return true;
    }

    @Override
    public boolean supportsBroadcast() {
        return true;
    }

    /**
     * 获取设备连接信息，不存在时抛出异常。
     *
     * @param deviceSerial 设备序列号
     * @return 连接信息
     */
    private IsupConnectionInfo getConnectionOrThrow(String deviceSerial) {
        return isupConnectionManager.getConnection(deviceSerial)
                .orElseThrow(() -> new IllegalStateException("ISUP设备未连接: " + deviceSerial));
    }

    /**
     * 解析通道号。
     *
     * @param channelIdentification 通道标识
     * @return 通道号
     */
    private Integer parseChannelNo(String channelIdentification) {
        Integer channelNo = NumberUtil.parseInt(channelIdentification, null);
        if (channelNo == null) {
            throw new IllegalArgumentException("无效的通道号: " + channelIdentification);
        }
        return channelNo;
    }
}
