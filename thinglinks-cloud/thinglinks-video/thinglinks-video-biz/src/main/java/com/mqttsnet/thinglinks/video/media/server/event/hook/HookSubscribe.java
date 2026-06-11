package com.mqttsnet.thinglinks.video.media.server.event.hook;

import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.dto.media.zlm.hook.Hook;
import com.mqttsnet.thinglinks.video.enumeration.hook.HookTypeEnum;
import com.mqttsnet.thinglinks.video.manager.hook.HookSubscribeManager;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaArrivalEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaDepartureEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaPublishEvent;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaRecordMp4Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZLM Hook 事件订阅管理（双层模式）。
 * <p>
 * Redis 存储 Hook 元数据（TTL=5min，自动过期），提供分布式可见性。
 * 本地 Map 保留 Event 回调（lambda 不可序列化）。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookSubscribe {

    private final ContextAwareExecutor contextAwareExecutor;
    private final HookSubscribeManager hookSubscribeManager;
    @Qualifier("videoDefaultExecutor")
    private final ThreadPoolExecutor videoDefaultExecutor;

    /**
     * 本地回调注册表（key = Hook.toString()，value = 回调函数）
     * 回调是 lambda，无法序列化到 Redis，必须保留在本地。
     */
    private final Map<String, Event> allSubscribes = new ConcurrentHashMap<>();

    @EventListener
    public void onApplicationEvent(MediaArrivalEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            if (event.getSchema() == null || "rtsp".equals(event.getSchema())) {
                sendNotify(HookTypeEnum.on_media_arrival, event);
            }
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onApplicationEvent(MediaDepartureEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            if (event.getSchema() == null || "rtsp".equals(event.getSchema())) {
                sendNotify(HookTypeEnum.on_media_departure, event);
            }
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onApplicationEvent(MediaPublishEvent event) {
        contextAwareExecutor.executeWithContext(() -> {
            sendNotify(HookTypeEnum.on_publish, event);
            return null;
        }, videoDefaultExecutor);
    }

    @EventListener
    public void onApplicationEvent(MediaRecordMp4Event event) {
        contextAwareExecutor.executeWithContext(() -> {
            sendNotify(HookTypeEnum.on_record_mp4, event);
            return null;
        }, videoDefaultExecutor);
    }

    private void sendNotify(HookTypeEnum hookType, MediaEvent event) {
        Hook paramHook = Hook.getInstance(hookType, event.getApp(), event.getStream());
        Event hookSubscribeEvent = allSubscribes.get(paramHook.toString());
        if (hookSubscribeEvent != null) {
            HookData data = HookData.getInstance(event);
            hookSubscribeEvent.response(data);
        }
    }

    public void addSubscribe(Hook hook, Event event) {
        if (hook.getExpireTime() == null) {
            hook.setExpireTime(System.currentTimeMillis() + 5 * 60 * 1000);
        }
        // 本地保存回调
        allSubscribes.put(hook.toString(), event);
        // Redis 保存 Hook 元数据（TTL=5min，自动过期无需 @Scheduled 清理）
        hookSubscribeManager.put(hook);
    }

    public void removeSubscribe(Hook hook) {
        allSubscribes.remove(hook.toString());
        hookSubscribeManager.remove(hook);
    }

    /**
     * 定期清理本地回调注册表中已过期的订阅（Redis key 已被 TTL 自动删除）。
     * 防止本地 ConcurrentHashMap 内存泄漏。
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanExpiredLocalSubscribes() {
        for (String hookKey : allSubscribes.keySet()) {
            if (!hookSubscribeManager.exists(hookKey)) {
                allSubscribes.remove(hookKey);
                log.debug("[HookSubscribe] 清理过期本地订阅: hookKey={}", hookKey);
            }
        }
    }

    /**
     * 获取所有 Hook 订阅（从本地回调注册表推导，而非 Redis SCAN）。
     * 本地有回调的才是有效订阅。
     */
    public List<Hook> getAll() {
        List<Hook> hooks = new ArrayList<>();
        for (String hookKey : allSubscribes.keySet()) {
            Hook hook = hookSubscribeManager.get(hookKey);
            if (hook != null) {
                hooks.add(hook);
            }
        }
        return hooks;
    }

    @FunctionalInterface
    public interface Event {
        void response(HookData data);
    }
}
