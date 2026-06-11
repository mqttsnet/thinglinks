package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.gb28181.event.PlayFailedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.PlayRequestedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.RtpPortAllocatedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.RtpPortReleasedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.StreamClosedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.StreamReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 流事件监听器。
 * <p>
 * 使用 ContextAwareExecutor + videoDefaultExecutor 异步执行，自动传递租户上下文。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamEventListener {

    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;

    @EventListener
    public void onPlayRequested(PlayRequestedEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.info("收到播放请求事件: deviceIdentification={}, channelIdentification={}, mediaIdentification={}, ssrc={}, rtpPort={}",
                    source.getDeviceIdentification(), source.getChannelIdentification(),
                    source.getMediaIdentification(), source.getSsrc(), source.getRtpPort());
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onStreamReady(StreamReadyEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.info("收到流就绪事件: deviceIdentification={}, channelIdentification={}, app={}, stream={}",
                    source.getDeviceIdentification(), source.getChannelIdentification(),
                    source.getApp(), source.getStream());
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onStreamClosed(StreamClosedEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.info("收到流关闭事件: deviceIdentification={}, channelIdentification={}, reason={}",
                    source.getDeviceIdentification(), source.getChannelIdentification(), source.getCloseReason());
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onRtpPortAllocated(RtpPortAllocatedEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.debug("收到RTP端口分配事件: mediaIdentification={}, port={}, deviceIdentification={}, channelIdentification={}",
                    source.getMediaIdentification(), source.getPort(),
                    source.getDeviceIdentification(), source.getChannelIdentification());
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onRtpPortReleased(RtpPortReleasedEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.debug("收到RTP端口释放事件: mediaIdentification={}, port={}",
                    source.getMediaIdentification(), source.getPort());
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onPlayFailed(PlayFailedEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            var source = event.getEventSource();
            log.warn("收到播放失败事件: deviceIdentification={}, channelIdentification={}, reason={}, error={}",
                    source.getDeviceIdentification(), source.getChannelIdentification(),
                    source.getFailureReason(), source.getErrorMessage());
            return null;
        }, videoDefaultExecutor);
    }
}
