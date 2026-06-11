package com.mqttsnet.thinglinks.device.event.listener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.device.event.DeviceInfoUpdatedEvent;
import com.mqttsnet.thinglinks.device.event.handler.DeviceInfoUpdatedCacheHandler;
import com.mqttsnet.thinglinks.device.event.source.DeviceInfoUpdatedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 设备信息更新事件监听器 ── 异步刷设备缓存。
 *
 * <p>用 AFTER_COMMIT 而非 @EventListener:主事务回滚时不刷缓存,避免写入从未持久化的中间状态;
 * fallbackExecution=true 兼容无事务调用方(如 Job)。异步走具名 linkDefaultExecutor 而非
 * ForkJoinPool.commonPool()。</p>
 *
 * @author mqttsnet
 * @since 2025/6/5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceInfoUpdatedEventListener {

    private final DeviceInfoUpdatedCacheHandler deviceInfoUpdatedCacheHandler;
    @Qualifier("linkDefaultExecutor")
    private final ThreadPoolExecutor linkDefaultExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public CompletableFuture<Boolean> handleDeviceInfoUpdatedEvent(DeviceInfoUpdatedEvent event) {
        Map<String, String> localMap = ContextUtil.getLocalMap();
        return CompletableFuture.supplyAsync(() -> {
            try {
                ContextUtil.setLocalMap(localMap);
                if (event.getSource() instanceof DeviceInfoUpdatedEventSource source) {
                    return deviceInfoUpdatedCacheHandler.handleDeviceInfoUpdatedCache(source.getDeviceIdentificationList());
                }
                log.warn("无效的事件源类型: {}", event.getSource().getClass());
                return false;
            } finally {
                ContextUtil.remove();
            }
        }, linkDefaultExecutor).exceptionally(ex -> {
            log.error("处理设备更新事件失败", ex);
            return false;
        });
    }
}
