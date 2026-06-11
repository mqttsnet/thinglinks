package com.mqttsnet.thinglinks.video.jt1078.sdk;

import com.mqttsnet.thinglinks.video.jt1078.config.Jt1078Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Description:
 * JT/T 1078 网关适配器 Mock 实现。
 * 用于开发和测试阶段，当未接入真实 JT808 网关时，
 * 所有方法仅记录日志并返回成功。
 *
 * <p>通过配置 {@code thinglinks.video.jt1078.mock-gateway=true} 激活。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "thinglinks.video.jt1078.mock-gateway", havingValue = "true", matchIfMissing = true)
public class MockJt1078GatewayAdapter implements Jt1078GatewayAdapter {

    private Jt1078DeviceCallback callback;

    @Override
    public boolean init(Jt1078Config config) {
        log.info("[JT1078-Mock] 初始化Mock网关适配器, gatewayIp={}, gatewayPort={}",
                config.getGatewayIp(), config.getGatewayPort());
        return true;
    }

    @Override
    public void destroy() {
        log.info("[JT1078-Mock] 销毁Mock网关适配器");
    }

    @Override
    public boolean sendRealPlayRequest(String simNumber, Integer channelNo, String streamType,
                                       String mediaServerIp, Integer mediaServerPort) {
        log.info("[JT1078-Mock] 发送实时音视频请求: simNumber={}, channelNo={}, streamType={}, mediaServerIp={}, mediaServerPort={}",
                simNumber, channelNo, streamType, mediaServerIp, mediaServerPort);
        return true;
    }

    @Override
    public boolean sendRealPlayControl(String simNumber, Integer channelNo, Integer controlType) {
        log.info("[JT1078-Mock] 发送实时音视频控制: simNumber={}, channelNo={}, controlType={}",
                simNumber, channelNo, controlType);
        return true;
    }

    @Override
    public boolean sendPlaybackRequest(String simNumber, Integer channelNo,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       String mediaServerIp, Integer mediaServerPort) {
        log.info("[JT1078-Mock] 发送历史视频回放请求: simNumber={}, channelNo={}, startTime={}, endTime={}, mediaServerIp={}, mediaServerPort={}",
                simNumber, channelNo, startTime, endTime, mediaServerIp, mediaServerPort);
        return true;
    }

    @Override
    public boolean sendPlaybackControl(String simNumber, Integer channelNo, Integer controlType, Integer speed) {
        log.info("[JT1078-Mock] 发送历史视频回放控制: simNumber={}, channelNo={}, controlType={}, speed={}",
                simNumber, channelNo, controlType, speed);
        return true;
    }

    @Override
    public boolean sendTalkRequest(String simNumber, Integer channelNo,
                                   String mediaServerIp, Integer mediaServerPort) {
        log.info("[JT1078-Mock] 发送语音对讲请求: simNumber={}, channelNo={}, mediaServerIp={}, mediaServerPort={}",
                simNumber, channelNo, mediaServerIp, mediaServerPort);
        return true;
    }

    @Override
    public boolean sendCaptureRequest(String simNumber, Integer channelNo, Integer count) {
        log.info("[JT1078-Mock] 发送远程抓拍请求: simNumber={}, channelNo={}, count={}",
                simNumber, channelNo, count);
        return true;
    }

    @Override
    public boolean queryResourceList(String simNumber) {
        log.info("[JT1078-Mock] 查询终端资源列表: simNumber={}", simNumber);
        return true;
    }

    @Override
    public void setDeviceCallback(Jt1078DeviceCallback callback) {
        log.info("[JT1078-Mock] 设置设备回调");
        this.callback = callback;
    }
}
