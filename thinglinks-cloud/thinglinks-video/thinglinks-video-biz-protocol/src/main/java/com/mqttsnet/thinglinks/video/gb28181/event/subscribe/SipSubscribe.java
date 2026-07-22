package com.mqttsnet.thinglinks.video.gb28181.event.subscribe;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.dto.gb28181.event.DeviceNotFoundEvent;
import com.mqttsnet.thinglinks.video.manager.sip.SipSubscribeManager;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.DialogTerminatedEvent;
import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.WarningHeader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SIP 事务订阅管理器（Redis + CompletableFuture 混合模式）。
 * <p>
 * Redis 存储可序列化的事务状态（SipSubscribeCache），提供分布式可观测性；
 * 本地 JVM 通过 CompletableFuture 同步等待结果（SIP 响应在同一 JVM 到达）。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipSubscribe {

    private final SipSubscribeManager sipSubscribeManager;

    /**
     * 本地 CompletableFuture 注册表（key = callId + cSeqNumber）
     */
    private final Map<String, CompletableFuture<EventResult<?>>> futures = new ConcurrentHashMap<>();

    /**
     * 注册订阅并返回 CompletableFuture。
     * <p>
     * 写入 PENDING 状态到 Redis，注册本地 Future 等待结果。
     * 超时由 CompletableFuture.orTimeout 处理，超时后自动清理。
     *
     * @param key       订阅 key（callId + cSeqNumber）
     * @param timeoutMs 超时毫秒数
     * @return 可等待结果的 CompletableFuture
     */
    public CompletableFuture<EventResult<?>> subscribe(String key, long timeoutMs) {
        // 如果存在旧的订阅，先取消
        cancel(key);

        sipSubscribeManager.writePending(key);

        // 创建本地 Future
        CompletableFuture<EventResult<?>> future = new CompletableFuture<>();
        future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .whenComplete((result, ex) -> {
                    futures.remove(key);
                    if (ex instanceof TimeoutException) {
                        log.debug("[SIP 订阅超时] key={}", key);
                        sipSubscribeManager.writeResult(key, EventResultType.timeout.name(), -1024, "消息超时未回复", null);
                    }
                });
        futures.put(key, future);
        log.info("[SIP 订阅注册] key={}, mapSize={}, timeoutMs={}", key, futures.size(), timeoutMs);
        return future;
    }

    /**
     * 完成订阅（收到 SIP 响应时调用）。
     * <p>
     * 顺序至关重要：<b>必须先 remove map + complete Future，再写 Redis</b>。
     * 理由：writeResult 调用远端 Redis 可能阻塞几百毫秒，期间 JAIN-SIP 会对同一事务触发
     * {@code TransactionTerminatedEvent}；如果 Redis 在前、remove 在后，
     * 那么 futures map 仍包含 key → processTransactionTerminated 看到 hasSubscribe=true
     * → 抢先用 "事务已结束"(-1024) complete Future，业务拿到的是错误结果而不是真实的 200 OK。
     * 改为先 remove+complete，futures 瞬间就没有这个 key，并发的事务终止事件不会干扰；
     * Redis 慢点写不影响业务路径。
     */
    public void complete(String key, EventResult<?> eventResult) {
        // 1. 先把本地 Future 移出 map 并 complete，业务线程立即解封
        CompletableFuture<EventResult<?>> future = futures.remove(key);
        boolean willComplete = future != null && !future.isDone();
        log.info("[SIP 订阅完成] key={}, futureFound={}, willComplete={}, type={}, statusCode={}, mapSize={}",
                key, future != null, willComplete,
                eventResult != null ? eventResult.type : null,
                eventResult != null ? eventResult.statusCode : null,
                futures.size());
        if (willComplete) {
            future.complete(eventResult);
        }
        // 2. 最后写 Redis（仅用于分布式可观测，慢不阻塞业务）
        sipSubscribeManager.writeResult(
                key,
                eventResult.type != null ? eventResult.type.name() : null,
                eventResult.statusCode,
                eventResult.msg,
                eventResult.callId);
    }

    /**
     * 取消订阅（发送异常时调用）
     */
    public void cancel(String key) {
        if (StrUtil.isBlank(key)) {
            return;
        }
        CompletableFuture<EventResult<?>> future = futures.remove(key);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        sipSubscribeManager.delete(key);
    }

    /**
     * 检查是否有活跃订阅
     */
    public boolean hasSubscribe(String key) {
        return futures.containsKey(key);
    }

    public boolean isEmpty() {
        return futures.isEmpty();
    }

    public int size() {
        return futures.size();
    }

    // ==================== 内部类型 ====================

    public enum EventResultType {
        timeout,
        response,
        transactionTerminated,
        dialogTerminated,
        deviceNotFoundEvent,
        cmdSendFailEvent,
        failedToGetPort,
        failedResult
    }

    public static class EventResult<T> {
        public int statusCode;
        public EventResultType type;
        public String msg;
        public String callId;
        public T event;

        public EventResult() {
        }

        public EventResult(T event) {
            this.event = event;
            if (event instanceof ResponseEvent responseEvent) {
                SIPResponse response = (SIPResponse) responseEvent.getResponse();
                this.type = EventResultType.response;
                // 注意：JAIN-SIP SIPResponse.equals(null) 实现有 bug（会 NPE），
                // Hutool ObjectUtil.isNotNull 内部用 Objects.equals 触发 equals，因此此处必须用原生 != null
                if (response != null) {
                    WarningHeader warningHeader = (WarningHeader) response.getHeader(WarningHeader.NAME);
                    if (warningHeader != null && StrUtil.isNotBlank(warningHeader.getText())) {
                        StringBuilder msgBuilder = new StringBuilder();
                        if (warningHeader.getCode() > 0) {
                            msgBuilder.append(warningHeader.getCode()).append(':');
                        }
                        if (StrUtil.isNotBlank(warningHeader.getAgent())) {
                            msgBuilder.append(warningHeader.getAgent()).append(':');
                        }
                        msgBuilder.append(warningHeader.getText());
                        this.msg = msgBuilder.toString();
                    } else {
                        this.msg = response.getReasonPhrase();
                    }
                    this.statusCode = response.getStatusCode();
                    this.callId = response.getCallIdHeader().getCallId();
                }
            } else if (event instanceof TimeoutEvent timeoutEvent) {
                this.type = EventResultType.timeout;
                this.msg = "消息超时未回复";
                this.statusCode = -1024;
                if (timeoutEvent.isServerTransaction()) {
                    this.callId = ((SIPRequest) timeoutEvent.getServerTransaction().getRequest()).getCallIdHeader().getCallId();
                } else {
                    this.callId = ((SIPRequest) timeoutEvent.getClientTransaction().getRequest()).getCallIdHeader().getCallId();
                }
            } else if (event instanceof TransactionTerminatedEvent transactionTerminatedEvent) {
                this.type = EventResultType.transactionTerminated;
                this.msg = "事务已结束";
                this.statusCode = -1024;
                if (transactionTerminatedEvent.isServerTransaction()) {
                    this.callId = ((SIPRequest) transactionTerminatedEvent.getServerTransaction().getRequest()).getCallIdHeader().getCallId();
                } else {
                    this.callId = ((SIPRequest) transactionTerminatedEvent.getClientTransaction().getRequest()).getCallIdHeader().getCallId();
                }
            } else if (event instanceof DialogTerminatedEvent dialogTerminatedEvent) {
                this.type = EventResultType.dialogTerminated;
                this.msg = "会话已结束";
                this.statusCode = -1024;
                this.callId = dialogTerminatedEvent.getDialog().getCallId().getCallId();
            } else if (event instanceof DeviceNotFoundEvent deviceNotFoundEvent) {
                this.type = EventResultType.deviceNotFoundEvent;
                this.msg = "设备未找到";
                this.statusCode = -1024;
                this.callId = deviceNotFoundEvent.getCallId();
            }
        }
    }
}
