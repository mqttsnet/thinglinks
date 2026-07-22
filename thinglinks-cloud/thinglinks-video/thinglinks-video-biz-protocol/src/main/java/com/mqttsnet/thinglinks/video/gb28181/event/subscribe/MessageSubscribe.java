package com.mqttsnet.thinglinks.video.gb28181.event.subscribe;

import com.mqttsnet.thinglinks.video.manager.sip.MessageSubscribeManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MESSAGE 消息订阅管理器（Redis + CompletableFuture 混合模式）。
 * <p>
 * Redis 存储事务状态（MsgSubscribeCache），本地 JVM 通过 CompletableFuture 等待结果。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSubscribe {

    private final MessageSubscribeManager messageSubscribeManager;

    private final Map<String, CompletableFuture<MsgResult>> futures = new ConcurrentHashMap<>();

    /**
     * 注册消息订阅并返回 CompletableFuture。
     */
    public CompletableFuture<MsgResult> subscribe(String cmdType, String sn, String deviceId, long timeoutMs) {
        String key = cmdType + sn;
        cancel(key);

        messageSubscribeManager.writePending(key, cmdType, sn, deviceId);

        CompletableFuture<MsgResult> future = new CompletableFuture<>();
        future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .whenComplete((result, ex) -> {
                    futures.remove(key);
                    if (ex instanceof TimeoutException) {
                        log.debug("[MESSAGE 订阅超时] key={}", key);
                        messageSubscribeManager.writeResult(key, -1, "消息超时未回复");
                    }
                });
        futures.put(key, future);
        return future;
    }

    /**
     * 完成消息订阅（收到响应时调用）。
     */
    public void complete(String key, int code, String msg, Object data) {
        messageSubscribeManager.writeResult(key, code, msg);

        CompletableFuture<MsgResult> future = futures.remove(key);
        if (future != null && !future.isDone()) {
            future.complete(new MsgResult(code, msg, data));
        }
    }

    /**
     * 取消订阅
     */
    public void cancel(String key) {
        if (key == null) {
            return;
        }
        CompletableFuture<MsgResult> future = futures.remove(key);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        messageSubscribeManager.delete(key);
    }

    public boolean hasSubscribe(String key) {
        return futures.containsKey(key);
    }

    public boolean isEmpty() {
        return futures.isEmpty();
    }

    public int size() {
        return futures.size();
    }

    // ==================== 结果类型 ====================

    @Getter
    @AllArgsConstructor
    public static class MsgResult {
        private final int code;
        private final String msg;
        private final Object data;
    }
}
