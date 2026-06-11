package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.gb28181.event.StreamClosedEvent;
import com.mqttsnet.thinglinks.video.gb28181.event.source.StreamClosedEventSource;
import com.mqttsnet.thinglinks.video.notify.NotifyRequest;
import com.mqttsnet.thinglinks.video.notify.VideoNotifyDispatcher;
import com.mqttsnet.thinglinks.video.manager.stream.StreamRecoveryCounterManager;
import com.mqttsnet.thinglinks.video.service.stream.PlayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 流断流自动恢复监听器。
 * <p>
 * 使用 Redis 存储重试计数（替代内存 ConcurrentHashMap），支持多实例共享重试状态。
 * 使用 ContextAwareExecutor + videoDefaultExecutor 异步执行，自动传递租户上下文。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamAutoRecoveryListener {

    private static final int MAX_RETRY = 3;
    private static final long[] RETRY_DELAYS_MS = {5000, 10000, 20000};

    private final VideoCacheDataHelper videoCacheDataHelper;
    private final PlayService playService;
    private final VideoNotifyDispatcher notifyDispatcher;
    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;
    private final StreamRecoveryCounterManager streamRecoveryCounterManager;

    @EventListener
    public void onStreamClosed(StreamClosedEvent event) {
        StreamClosedEventSource source = event.getEventSource();
        if (ObjectUtil.isNull(source)) {
            return;
        }

        contextAwareExecutor.executeWithContext(() -> {
            if (source.getTenantId() != null) {
                ContextUtil.setTenantId(String.valueOf(source.getTenantId()));
            }
            doRecovery(source);
            return null;
        }, videoDefaultExecutor);
    }

    private void doRecovery(StreamClosedEventSource source) {
        String deviceId = source.getDeviceIdentification();
        String channelId = source.getChannelIdentification();
        String key = buildKey(source);

        // 用户主动关闭：以 source.userInitiated 为唯一权威信号，不要再去 closeReason 字符串里猜中英文。
        // 早期版本仅判 "user_stop"/"bye"/"manual"，"用户主动停止" 直接漏掉，结果用户点了停止
        // 5s 后又被恢复链路拿同一 SSRC/streamId 抢着 INVITE，撞上 ZLM "stream already exists"。
        if (source.isUserInitiated()) {
            streamRecoveryCounterManager.delete(key);
            log.debug("[流恢复] 用户主动关闭，跳过恢复: deviceId={}, channelId={}", deviceId, channelId);
            return;
        }

        // 检查设备是否在线
        Optional<VideoDeviceCacheVO> deviceCache = Optional.ofNullable(videoCacheDataHelper.getDeviceInfo(deviceId));
        if (deviceCache.isEmpty() || !Boolean.TRUE.equals(deviceCache.get().getOnlineStatus())) {
            log.info("[流恢复] 设备离线, 跳过恢复: deviceId={}, channelId={}", deviceId, channelId);
            streamRecoveryCounterManager.delete(key);
            return;
        }

        int currentRetry = streamRecoveryCounterManager.get(key);
        if (currentRetry >= MAX_RETRY) {
            log.warn("[流恢复] 已达最大重试次数({}), 放弃恢复: deviceId={}, channelId={}", MAX_RETRY, deviceId, channelId);
            streamRecoveryCounterManager.delete(key);
            publishRecoveryFailedEvent(source);
            return;
        }

        // 指数退避等待
        long delayMs = RETRY_DELAYS_MS[Math.min(currentRetry, RETRY_DELAYS_MS.length - 1)];
        log.info("[流恢复] 异常断流, 第{}次重试, 等待{}ms: deviceId={}, channelId={}, reason={}",
                currentRetry + 1, delayMs, deviceId, channelId, StrUtil.nullToDefault(source.getCloseReason(), ""));

        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // 重试播放
        try {
            streamRecoveryCounterManager.set(key, currentRetry + 1);
            playService.play(deviceId, channelId);
            log.info("[流恢复] 重试成功: deviceId={}, channelId={}, 第{}次", deviceId, channelId, currentRetry + 1);
            streamRecoveryCounterManager.delete(key);
        } catch (Exception e) {
            log.warn("[流恢复] 重试失败: deviceId={}, channelId={}, 第{}次, error={}",
                    deviceId, channelId, currentRetry + 1, e.getMessage());
        }
    }

    private String buildKey(StreamClosedEventSource source) {
        return source.getDeviceIdentification() + ":" + source.getChannelIdentification();
    }

    private void publishRecoveryFailedEvent(StreamClosedEventSource source) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("deviceIdentification", StrUtil.nullToDefault(source.getDeviceIdentification(), ""));
            variables.put("channelIdentification", StrUtil.nullToDefault(source.getChannelIdentification(), ""));
            variables.put("streamUrl", StrUtil.nullToDefault(source.getApp() + "/" + source.getStream(), ""));
            variables.put("eventTime", LocalDateTime.now().toString());
            variables.put("bizType", "VIDEO_STREAM_RECOVERY_FAILED");
            variables.put("bizId", StrUtil.nullToDefault(source.getCallId(), ""));

            notifyDispatcher.dispatchSync(NotifyRequest.builder()
                    .eventType("STREAM_CLOSE")
                    .title("[流恢复失败] " + source.getDeviceIdentification() + "/" + source.getChannelIdentification())
                    .variables(variables)
                    .bizType("VIDEO_STREAM_RECOVERY_FAILED")
                    .bizId(StrUtil.nullToDefault(source.getCallId(), ""))
                    .build());
        } catch (Exception e) {
            log.warn("[流恢复] 发布恢复失败通知异常: {}", e.getMessage());
        }
    }
}
