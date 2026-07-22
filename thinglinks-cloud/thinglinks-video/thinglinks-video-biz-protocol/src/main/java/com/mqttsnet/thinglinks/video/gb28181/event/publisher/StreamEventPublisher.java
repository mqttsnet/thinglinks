package com.mqttsnet.thinglinks.video.gb28181.event.publisher;

import com.mqttsnet.thinglinks.video.gb28181.event.PlayFailedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.PlayRequestedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.RtpPortAllocatedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.RtpPortReleasedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.StreamClosedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.StreamReadyEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayFailedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PlayRequestedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortAllocatedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.RtpPortReleasedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamClosedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamReadyEventSource;
import com.mqttsnet.basic.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 流事件发布器。
 * 发布播放请求、流就绪、流关闭、RTP 端口分配/释放、播放失败等事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布播放请求事件
     *
     * @param source 事件源
     */
    public void publishPlayRequestedEvent(PlayRequestedEventSource source) {
        log.debug("发布播放请求事件: deviceIdentification={}, channelIdentification={}", source.getDeviceIdentification(), source.getChannelIdentification());
        eventPublisher.publishEvent(new PlayRequestedEvent(source));
    }

    /**
     * 发布流就绪事件
     *
     * @param source 事件源
     */
    public void publishStreamReadyEvent(StreamReadyEventSource source) {
        log.debug("发布流就绪事件: deviceIdentification={}, channelIdentification={}, app={}, stream={}",
                source.getDeviceIdentification(), source.getChannelIdentification(), source.getApp(), source.getStream());
        eventPublisher.publishEvent(new StreamReadyEvent(source));
    }

    /**
     * 发布流关闭事件。
     * <p>调用方未显式 setTenantId 时自动从 {@link ContextUtil} 取当前线程的租户 ID 回填，
     * 让异步监听器（如自动恢复）通过 {@link com.mqttsnet.thinglinks.context.ContextAwareExecutor}
     * 接力时仍能拿到正确的租户上下文，避免读到错误租户的 Redis Key。
     *
     * @param source 事件源
     */
    public void publishStreamClosedEvent(StreamClosedEventSource source) {
        if (source.getTenantId() == null) {
            Long tenantId = ContextUtil.getTenantId();
            if (tenantId != null) {
                source.setTenantId(tenantId);
            }
        }
        log.debug("发布流关闭事件: deviceIdentification={}, channelIdentification={}, reason={}, userInitiated={}",
                source.getDeviceIdentification(), source.getChannelIdentification(),
                source.getCloseReason(), source.isUserInitiated());
        eventPublisher.publishEvent(new StreamClosedEvent(source));
    }

    /**
     * 发布 RTP 端口分配事件
     *
     * @param source 事件源
     */
    public void publishRtpPortAllocatedEvent(RtpPortAllocatedEventSource source) {
        log.debug("发布RTP端口分配事件: mediaIdentification={}, port={}", source.getMediaIdentification(), source.getPort());
        eventPublisher.publishEvent(new RtpPortAllocatedEvent(source));
    }

    /**
     * 发布 RTP 端口释放事件
     *
     * @param source 事件源
     */
    public void publishRtpPortReleasedEvent(RtpPortReleasedEventSource source) {
        log.debug("发布RTP端口释放事件: mediaIdentification={}, port={}", source.getMediaIdentification(), source.getPort());
        eventPublisher.publishEvent(new RtpPortReleasedEvent(source));
    }

    /**
     * 发布播放失败事件
     *
     * @param source 事件源
     */
    public void publishPlayFailedEvent(PlayFailedEventSource source) {
        log.warn("发布播放失败事件: deviceIdentification={}, channelIdentification={}, reason={}",
                source.getDeviceIdentification(), source.getChannelIdentification(), source.getFailureReason());
        eventPublisher.publishEvent(new PlayFailedEvent(source));
    }
}
