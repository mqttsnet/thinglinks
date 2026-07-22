package com.mqttsnet.thinglinks.video.isup.sdk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * ISUP SDK Mock 实现。
 * 用于无海康 HCNetSDK native 库的开发和测试环境，
 * 所有方法仅记录日志并返回模拟的成功值。
 *
 * <p>通过配置 {@code thinglinks.video.isup.mock-sdk=true} 激活（默认激活）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "thinglinks.video.isup", name = "mock-sdk", havingValue = "true", matchIfMissing = true)
public class MockIsupSdkAdapter implements IsupSdkAdapter {

    private final AtomicInteger handleCounter = new AtomicInteger(1000);

    private IsupDeviceCallback deviceCallback;
    private IsupAlarmCallback alarmCallback;

    @Override
    public boolean init(String sdkPath) {
        log.info("[MockIsupSdk] 初始化SDK, sdkPath={}", sdkPath);
        return true;
    }

    @Override
    public void destroy() {
        log.info("[MockIsupSdk] 销毁SDK");
    }

    @Override
    public boolean startListening(String ip, int port) {
        log.info("[MockIsupSdk] 启动监听, ip={}, port={}", ip, port);
        return true;
    }

    @Override
    public void stopListening() {
        log.info("[MockIsupSdk] 停止监听");
    }

    @Override
    public Integer login(String deviceSerial, String ip, int port, String username, String password) {
        int handle = handleCounter.incrementAndGet();
        log.info("[MockIsupSdk] 登录设备, deviceSerial={}, ip={}, port={}, handle={}", deviceSerial, ip, port, handle);
        return handle;
    }

    @Override
    public boolean logout(Integer userId) {
        log.info("[MockIsupSdk] 登出设备, userId={}", userId);
        return true;
    }

    @Override
    public Integer startRealPlay(Integer userId, Integer channelNo, String streamType) {
        int handle = handleCounter.incrementAndGet();
        log.info("[MockIsupSdk] 开始实时预览, userId={}, channelNo={}, streamType={}, playHandle={}",
                userId, channelNo, streamType, handle);
        return handle;
    }

    @Override
    public boolean stopRealPlay(Integer playHandle) {
        log.info("[MockIsupSdk] 停止实时预览, playHandle={}", playHandle);
        return true;
    }

    @Override
    public Integer startPlayback(Integer userId, Integer channelNo, LocalDateTime startTime, LocalDateTime endTime) {
        int handle = handleCounter.incrementAndGet();
        log.info("[MockIsupSdk] 开始录像回放, userId={}, channelNo={}, startTime={}, endTime={}, playbackHandle={}",
                userId, channelNo, startTime, endTime, handle);
        return handle;
    }

    @Override
    public boolean stopPlayback(Integer playbackHandle) {
        log.info("[MockIsupSdk] 停止录像回放, playbackHandle={}", playbackHandle);
        return true;
    }

    @Override
    public boolean ptzControl(Integer userId, Integer channelNo, Integer command, Integer stop, Integer speed) {
        log.info("[MockIsupSdk] PTZ控制, userId={}, channelNo={}, command={}, stop={}, speed={}",
                userId, channelNo, command, stop, speed);
        return true;
    }

    @Override
    public boolean ptzPreset(Integer userId, Integer channelNo, Integer command, Integer presetIndex) {
        log.info("[MockIsupSdk] 预置位操作, userId={}, channelNo={}, command={}, presetIndex={}",
                userId, channelNo, command, presetIndex);
        return true;
    }

    @Override
    public Integer setupAlarmChan(Integer userId) {
        int handle = handleCounter.incrementAndGet();
        log.info("[MockIsupSdk] 建立告警通道, userId={}, alarmHandle={}", userId, handle);
        return handle;
    }

    @Override
    public boolean closeAlarmChan(Integer alarmHandle) {
        log.info("[MockIsupSdk] 关闭告警通道, alarmHandle={}", alarmHandle);
        return true;
    }

    @Override
    public Integer startVoiceTalk(Integer userId, Integer channelNo) {
        int handle = handleCounter.incrementAndGet();
        log.info("[MockIsupSdk] 开始语音对讲, userId={}, channelNo={}, voiceHandle={}", userId, channelNo, handle);
        return handle;
    }

    @Override
    public boolean stopVoiceTalk(Integer voiceHandle) {
        log.info("[MockIsupSdk] 停止语音对讲, voiceHandle={}", voiceHandle);
        return true;
    }

    @Override
    public boolean capture(Integer userId, Integer channelNo, String savePath) {
        log.info("[MockIsupSdk] 抓图, userId={}, channelNo={}, savePath={}", userId, channelNo, savePath);
        return true;
    }

    @Override
    public void setDeviceCallback(IsupDeviceCallback callback) {
        log.info("[MockIsupSdk] 注册设备事件回调");
        this.deviceCallback = callback;
    }

    @Override
    public void setAlarmCallback(IsupAlarmCallback callback) {
        log.info("[MockIsupSdk] 注册告警事件回调");
        this.alarmCallback = callback;
    }
}
