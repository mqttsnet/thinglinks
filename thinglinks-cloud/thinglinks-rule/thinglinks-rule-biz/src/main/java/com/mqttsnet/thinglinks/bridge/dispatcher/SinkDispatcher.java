package com.mqttsnet.thinglinks.bridge.dispatcher;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.databridge.model.ConnectorConfig;
import com.mqttsnet.basic.databridge.model.ConnectorPayload;
import com.mqttsnet.basic.databridge.model.ConnectorType;
import com.mqttsnet.basic.databridge.model.SendResult;
import com.mqttsnet.basic.databridge.registry.ConnectorRegistry;
import com.mqttsnet.basic.databridge.spi.Sink;
import com.mqttsnet.basic.groovy.constants.ExecutionStatus;
import com.mqttsnet.basic.groovy.entity.EngineExecutorResult;
import com.mqttsnet.basic.groovy.entity.ExecuteParams;
import com.mqttsnet.basic.groovy.entity.ScriptEntry;
import com.mqttsnet.basic.groovy.executor.EngineExecutor;
import com.mqttsnet.basic.groovy.loader.ScriptLoader;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleDeletedEvent;
import com.mqttsnet.thinglinks.bridge.policy.BridgeRetryPolicy;
import com.mqttsnet.thinglinks.bridge.policy.BridgeRetryPolicyResolver;
import com.mqttsnet.thinglinks.bridge.trace.BridgeStepType;
import com.mqttsnet.thinglinks.bridge.trace.BridgeTraceBuilder;
import com.mqttsnet.thinglinks.bridge.trace.RuleMatchConditions;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataSourceCacheVO;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.service.script.RuleGroovyScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 桥接出站分发器(Chain of Responsibility)。
 *
 * <h3>流水线</h3>
 * 取数据源 → 解析策略 → 规则级 RateLimiter 限流 → Groovy 转换 → 构造 payload/config →
 * Sink 发送 + 指数退避重试 → 失败死信(可选) → 累积 trace → 异步落库。
 *
 * <h3>线程模型</h3>
 * 调用方已在 RocketMQ 消费线程或 dynamictp 池上;本类内部<b>同步</b>执行重试链,
 * 避免无限层异步嵌套。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SinkDispatcher {

    private static final String SERIALIZATION_DEFAULT = "JSON";

    private final ConnectorRegistry connectorRegistry;
    private final DataSourceService dataSourceService;
    private final BridgeRetryPolicyResolver retryPolicyResolver;
    private final ApplicationEventPublisher eventPublisher;
    private final RuleGroovyScriptService ruleGroovyScriptService;
    private final ScriptLoader scriptLoader;
    private final EngineExecutor engineExecutor;
    private final RuleCacheDataHelper ruleCacheDataHelper;

    /**
     * 规则级 RateLimiter 缓存(本地 Caffeine,不上 Redis)。
     * Guava {@link RateLimiter} 持令牌桶可变状态,per-JVM 语义,多节点天然独立。
     * Key = {@code tenantId:ruleId};规则变更通过事件 invalidate,QPS 通过 setRate 自适应。
     */
    private final Cache<String, RateLimiter> rateLimiterCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(Duration.ofMinutes(30))
        .build();

    /**
     * 默认派发(无上游匹配延迟;replay / testSink 场景用)。
     */
    public void dispatch(DataBridgeCacheVO rule, BridgeMessageEnvelope envelope) {
        dispatch(rule, envelope, 0L);
    }

    /**
     * 派发一条桥接消息到下游。内部<b>不抛异常</b>:全部捕获记入 trace,保护上游 Consumer 批处理不被单条拖垮。
     *
     * @param matchLatencyMs 上游 matcher 耗时;0 = 无 matcher 阶段(如 replay)
     */
    public void dispatch(DataBridgeCacheVO rule, BridgeMessageEnvelope envelope, long matchLatencyMs) {
        // 回填 traceId,确保后续 header 与 Sink 端日志串联同一 traceId
        if (StrUtil.isBlank(envelope.getTraceId())) {
            envelope.setTraceId(ContextUtil.getLogTraceId());
        }
        BridgeTraceBuilder trace = BridgeTraceBuilder.start(envelope, rule);
        try {
            recordIngestStep(trace, envelope);
            recordMatchStep(trace, rule, envelope, matchLatencyMs);

            DataSourceCacheVO dataSource = loadDataSourceCached(rule.getDataSourceId());
            if (dataSource == null) {
                failTrace(trace, "关联数据源不存在 dsId=" + rule.getDataSourceId());
                return;
            }
            if (!Boolean.TRUE.equals(dataSource.getEnable())) {
                String err = "关联数据源未启用 dsId=" + dataSource.getId();
                trace.addSkippedStep(BridgeStepType.SINK_SEND, "投递", err);
                trace.endWithError(err);
                return;
            }

            BridgeRetryPolicy policy = retryPolicyResolver.resolve(rule, dataSource);

            // 限流命中是"主动丢弃",trace 用 STATUS_PARTIAL 表达"该轮跳过"
            if (policy.getRateLimitQps() > 0
                && !tryAcquireRateLimit(envelope.getTenantId(), rule.getId(), policy)) {
                String reason = "规则级限流命中 ruleId=" + rule.getId()
                    + " configuredQps=" + policy.getRateLimitQps();
                trace.addSkippedStep(BridgeStepType.RATE_LIMIT, "限流", reason);
                trace.end(BridgeTraceBuilder.STATUS_PARTIAL, reason);
                return;
            }

            TransformOutcome to = applyTransformIfConfigured(rule, envelope, trace);
            if (to.shouldDrop) {
                trace.end(BridgeTraceBuilder.STATUS_PARTIAL, "转换脚本失败 + 策略 DROP,已丢弃");
                return;
            }
            BridgeMessageEnvelope finalEnvelope = to.envelope;

            ConnectorType type = parseConnectorType(dataSource.getSourceType());
            if (type == null) {
                failTrace(trace, "未知的 sourceType=" + dataSource.getSourceType());
                return;
            }
            Sink sink = connectorRegistry.getSink(type);
            if (sink == null) {
                failTrace(trace, "未注册 Sink 实现 type=" + type);
                return;
            }

            ConnectorPayload payload = toConnectorPayload(finalEnvelope);
            ConnectorConfig config = toConnectorConfig(dataSource, finalEnvelope);
            SendResult last = sendWithRetry(sink, payload, config, policy, type, trace);

            if (last != null && last.isSuccess()) {
                trace.end(BridgeTraceBuilder.STATUS_SUCCESS,
                    "投递成功 messageId=" + StrUtil.nullToDefault(last.getMessageId(), "-"));
            } else {
                String errorMsg = last == null ? "send returned null" : last.errorMessage();
                tryDeadLetter(rule, finalEnvelope, policy, errorMsg, trace);
                trace.end(
                    policy.getDeadLetterDataSourceId() != null
                        ? BridgeTraceBuilder.STATUS_DEAD_LETTER
                        : BridgeTraceBuilder.STATUS_FAILED,
                    policy.getDeadLetterDataSourceId() != null
                        ? "重试耗尽,已投死信 dlqDsId=" + policy.getDeadLetterDataSourceId()
                        : "重试耗尽 lastError=" + errorMsg);
            }
        } catch (Exception ex) {
            log.error("[SinkDispatcher] unexpected dispatch error ruleId={} traceId={} clientId={} action={} topic={}",
                rule.getId(), trace.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), envelope.getTopic(), ex);
            trace.endWithError("dispatch error: " + ex.getMessage());
        } finally {
            publishTrace(trace);
        }
    }

    // ============================== Trace 记账辅助 ==============================

    private void recordIngestStep(BridgeTraceBuilder trace, BridgeMessageEnvelope envelope) {
        // envelope.ts 由 mqs 旁路 producer 端打,这里测整个 RocketMQ 链路 + 消费排队 + 反序列化 + 上下文恢复
        long latency = Optional.ofNullable(envelope.getTs())
            .map(ts -> Math.max(0L, System.currentTimeMillis() - ts))
            .orElse(0L);
        trace.addSuccessStep(BridgeStepType.INGEST, "数据接入",
            BridgeTraceBuilder.truncate(envelope.getRawMessage()), null, latency, null);
    }

    /**
     * 写 RULE_MATCH step ── 前端"链路回放"详情抽屉按 {@code extensionJson.matchedConditions}
     * 渲染条件芯片栏 + 明细列表;{@code rawConfig} 展示完整规则配置供运维核对。
     * <p>{@code matchType / branch / totalConditions} 是历史前端兼容字段,保留。
     */
    private void recordMatchStep(BridgeTraceBuilder trace, DataBridgeCacheVO rule,
                                 BridgeMessageEnvelope envelope, long latencyMs) {
        Map<String, Object> input = buildMatchStepInput(envelope);
        List<Map<String, Object>> matched = RuleMatchConditions.collect(rule, envelope);

        String output = String.format("命中规则 %s (id=%s, 条件数=%d)",
            StrUtil.nullToDefault(rule.getRuleName(), "-"),
            rule.getId(), matched.size());

        Map<String, Object> ext = MapUtil.<String, Object>builder(new LinkedHashMap<String, Object>())
            .put("ruleId", rule.getId())
            .put("ruleCode", rule.getRuleCode())
            .put("ruleName", rule.getRuleName())
            .put("priority", rule.getPriority())
            .put("dataSourceId", rule.getDataSourceId())
            .put("matchType", "VISUAL")
            .put("branch", "HIT")
            .put("matchedConditions", matched)
            .put("totalConditions", matched.size())
            .put("rawConfig", BridgeTraceBuilder.truncate(rule.getMatchConfigJson()))
            .build();

        trace.addSuccessStep(BridgeStepType.RULE_MATCH, "规则匹配",
            JsonUtil.toJson(input), output, latencyMs, ext);
    }

    /**
     * RULE_MATCH step 的 input 摘要 ── 仅取 envelope 用于匹配的关键字段,
     * 避免 raw payload 巨长污染抽屉视图(详细 payload 看 INGEST step.inputSummary)。
     */
    private static Map<String, Object> buildMatchStepInput(BridgeMessageEnvelope envelope) {
        if (envelope == null) {
            return Collections.emptyMap();
        }
        return MapUtil.<String, Object>builder(new LinkedHashMap<String, Object>())
            .put("actionType", envelope.getActionType())
            .put(CommonIotConstants.TOPIC, envelope.getTopic())
            .put("productIdentification", envelope.getProductIdentification())
            .put("deviceIdentification", envelope.getDeviceIdentification())
            .put(CommonIotConstants.CLIENT_ID, envelope.getClientId())
            .put(CommonIotConstants.TENANT_ID, envelope.getTenantId())
            .build();
    }

    private void failTrace(BridgeTraceBuilder trace, String err) {
        trace.addFailedStep(BridgeStepType.SINK_SEND, "投递", null, err, 0L, null);
        trace.endWithError(err);
    }

    // ============================== 重试 ==============================

    /**
     * 指数退避重试,首次 + 最多 retryMaxTimes 次。成功立即返回;全失败返最后一次。
     */
    private SendResult sendWithRetry(Sink sink, ConnectorPayload payload, ConnectorConfig config,
                                     BridgeRetryPolicy policy, ConnectorType type,
                                     BridgeTraceBuilder trace) {
        int max = Math.max(0, policy.getRetryMaxTimes());
        long backoff = policy.getRetryBackoffMs();
        SendResult last = null;
        for (int attempt = 0; attempt <= max; attempt++) {
            long t0 = System.currentTimeMillis();
            SendResult r;
            try {
                r = sink.send(payload, config);
            } catch (Throwable t) {
                r = SendResult.fail(t, System.currentTimeMillis() - t0);
            }
            last = r;
            Map<String, Object> ext = MapUtil.<String, Object>builder()
                .put("attempt", attempt + 1)
                .put("maxAttempts", max + 1)
                .put("sinkType", type.name())
                .put("messageId", r.getMessageId())
                .build();
            String stepName = "投递" + (attempt > 0 ? "(重试 " + attempt + ")" : "");
            if (r.isSuccess()) {
                trace.addSuccessStep(BridgeStepType.SINK_SEND, stepName,
                    BridgeTraceBuilder.truncate(asJson(payload)),
                    BridgeTraceBuilder.truncate(asJson(r.getAttributes())),
                    r.getLatencyMs(), ext);
                return r;
            }
            trace.addFailedStep(BridgeStepType.SINK_SEND, stepName,
                BridgeTraceBuilder.truncate(asJson(payload)),
                StrUtil.nullToDefault(r.errorMessage(), "unknown"),
                r.getLatencyMs(), ext);
            if (attempt < max && !sleepBackoff(backoff, attempt)) {
                return r;  // interrupted → 提前退出
            }
        }
        return last;
    }

    /**
     * 指数退避 sleep,上限 60s;被中断返 false 告知调用方提前退出。
     */
    private boolean sleepBackoff(long base, int attempt) {
        long sleep = Math.min(base << attempt, 60_000L);
        try {
            Thread.sleep(sleep);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[SinkDispatcher] retry sleep interrupted, abort retries");
            return false;
        }
    }

    // ============================== 死信 ==============================

    /**
     * 投递死信(配置了 dead_letter_data_source_id 时);仅试投一次,失败仅告警。
     */
    private void tryDeadLetter(DataBridgeCacheVO rule, BridgeMessageEnvelope envelope,
                               BridgeRetryPolicy policy, String reason, BridgeTraceBuilder trace) {
        Long dlqDsId = policy.getDeadLetterDataSourceId();
        if (dlqDsId == null) {
            return;
        }
        Map<String, Object> ext = MapUtil.<String, Object>builder()
            .put("dlqDsId", dlqDsId)
            .put("originReason", reason)
            .build();

        DataSourceCacheVO dlqDs = loadDataSourceCached(dlqDsId);
        if (dlqDs == null || !Boolean.TRUE.equals(dlqDs.getEnable())) {
            String err = "死信数据源不存在或未启用 dlqDsId=" + dlqDsId;
            trace.addFailedStep(BridgeStepType.DEAD_LETTER, "死信投递", null, err, 0L, ext);
            log.error("[SinkDispatcher] DLQ unavailable, message LOST ruleId={} traceId={} clientId={} action={} dlqDsId={}",
                rule.getId(), trace.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), dlqDsId);
            return;
        }
        ConnectorType dlqType = parseConnectorType(dlqDs.getSourceType());
        Sink dlqSink = Optional.ofNullable(dlqType).map(connectorRegistry::getSink).orElse(null);
        if (dlqSink == null) {
            String err = "死信 Sink 未注册 type=" + dlqDs.getSourceType();
            trace.addFailedStep(BridgeStepType.DEAD_LETTER, "死信投递", null, err, 0L, ext);
            log.error("[SinkDispatcher] DLQ Sink not registered, message LOST ruleId={} traceId={} clientId={} action={} type={}",
                rule.getId(), trace.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), dlqDs.getSourceType());
            return;
        }
        try {
            ConnectorPayload payload = toConnectorPayload(envelope);
            ConnectorConfig config = toConnectorConfig(dlqDs, envelope);
            long t0 = System.currentTimeMillis();
            SendResult r = dlqSink.send(payload, config);
            ext.put("dlqMessageId", r.getMessageId());
            if (r.isSuccess()) {
                trace.addSuccessStep(BridgeStepType.DEAD_LETTER, "死信投递",
                    BridgeTraceBuilder.truncate(asJson(payload)),
                    BridgeTraceBuilder.truncate(asJson(r.getAttributes())),
                    r.getLatencyMs(), ext);
            } else {
                trace.addFailedStep(BridgeStepType.DEAD_LETTER, "死信投递",
                    BridgeTraceBuilder.truncate(asJson(payload)),
                    StrUtil.nullToDefault(r.errorMessage(), "unknown"),
                    System.currentTimeMillis() - t0, ext);
                log.error("[SinkDispatcher] DLQ send FAILED, message likely LOST ruleId={} traceId={} clientId={} action={} dlqDsId={} err={}",
                    rule.getId(), trace.getTraceId(),
                    envelope.getClientId(), envelope.getActionType(),
                    dlqDsId, r.errorMessage());
            }
        } catch (Exception e) {
            trace.addFailedStep(BridgeStepType.DEAD_LETTER, "死信投递", null,
                "DLQ 异常 " + e.getMessage(), 0L, ext);
            log.error("[SinkDispatcher] DLQ EXCEPTION, message likely LOST ruleId={} traceId={} clientId={} action={} dlqDsId={}",
                rule.getId(), trace.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), dlqDsId, e);
        }
    }

    // ============================== 数据组装 ==============================

    /**
     * BridgeMessageEnvelope → 通用 ConnectorPayload(纯透传,body = rawMessage.getBytes(UTF-8))。
     * 元数据走 headers;encoding 信息由消费者自行从 rawMessage 解析。
     */
    private ConnectorPayload toConnectorPayload(BridgeMessageEnvelope envelope) {
        Map<String, String> headers = MapUtil.<String, String>builder()
            .put("X-Thinglinks-Trace", StrUtil.nullToEmpty(envelope.getTraceId()))
            .put("X-Thinglinks-Tenant", StrUtil.nullToEmpty(envelope.getTenantId()))
            .build();
        putIfNotBlank(headers, "X-Thinglinks-AppId", envelope.getAppId());
        putIfNotBlank(headers, "X-Thinglinks-Product", envelope.getProductIdentification());
        putIfNotBlank(headers, "X-Thinglinks-Device", envelope.getDeviceIdentification());
        putIfNotBlank(headers, "X-Thinglinks-ClientId", envelope.getClientId());
        putIfNotBlank(headers, "X-Thinglinks-Action", envelope.getActionType());
        putIfNotBlank(headers, "X-Thinglinks-Protocol", envelope.getProtocolType());
        putIfNotBlank(headers, "X-Thinglinks-Topic", envelope.getTopic());
        return ConnectorPayload.builder()
            .body(Optional.ofNullable(envelope.getRawMessage())
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .orElseGet(() -> new byte[0]))
            .headers(headers)
            .routingKey(StrUtil.nullToDefault(envelope.getDeviceIdentification(),
                envelope.getProductIdentification()))
            .ts(Optional.ofNullable(envelope.getTs()).orElseGet(System::currentTimeMillis))
            .build();
    }

    /**
     * DataSourceCacheVO → ConnectorConfig(identifier = {@code tenantId-dsId} 多租户连接池隔离)。
     * <p>分隔符用 {@code -} 而非 {@code :},兼容 RocketMQ group/topic 字符白名单 {@code ^[%|a-zA-Z0-9_-]+$}。
     * connectionJson / credentialJson 在 entity → ResultVO 转换时已解密为明文。
     */
    private ConnectorConfig toConnectorConfig(DataSourceCacheVO ds, BridgeMessageEnvelope envelope) {
        String identifier = StrUtil.nullToDefault(envelope.getTenantId(), "_") + "-" + ds.getId();
        return ConnectorConfig.builder()
            .type(parseConnectorType(ds.getSourceType()))
            .identifier(identifier)
            .connectionJson(ds.getConnectionJson())
            .credentialJson(ds.getCredentialJson())
            .extraConfigJson(ds.getExtendParams())
            .serialization(StrUtil.nullToDefault(ds.getSerialization(), SERIALIZATION_DEFAULT))
            .build();
    }

    /**
     * 非空才塞入 map(避免空 header 污染)。
     */
    private static void putIfNotBlank(Map<String, String> map, String key, String value) {
        if (StrUtil.isNotBlank(value)) {
            map.put(key, value);
        }
    }

    // ============================== 限流 ==============================

    /**
     * 非阻塞 tryAcquire,0 等待。
     */
    private boolean tryAcquireRateLimit(String tenantId, Long ruleId, BridgeRetryPolicy policy) {
        String key = StrUtil.nullToDefault(tenantId, "_") + ":" + ruleId;
        RateLimiter limiter = rateLimiterCache.get(key, k -> RateLimiter.create(policy.getRateLimitQps()));
        return limiter == null || limiter.tryAcquire();  // 创建失败放行,避免限流器影响主链路
    }

    /**
     * 失效指定规则的 RateLimiter(因 key=tenantId:ruleId,单 ruleId 可能命中多 tenant 条目)。
     */
    public void invalidateRateLimiter(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        String suffix = ":" + ruleId;
        rateLimiterCache.asMap().keySet().removeIf(k -> k != null && k.endsWith(suffix));
    }

    /**
     * 失效全部 RateLimiter(批量改限流策略时用)。
     */
    public void invalidateAllRateLimiters() {
        rateLimiterCache.invalidateAll();
    }

    @EventListener
    public void onRuleChanged(BridgeRuleChangedEvent event) {
        Optional.ofNullable(event)
            .map(BridgeRuleChangedEvent::getEventSource)
            .map(s -> s.getRuleId())
            .ifPresentOrElse(this::invalidateRateLimiter, this::invalidateAllRateLimiters);
    }

    @EventListener
    public void onRuleDeleted(BridgeRuleDeletedEvent event) {
        Optional.ofNullable(event)
            .map(BridgeRuleDeletedEvent::getEventSource)
            .map(s -> s.getRuleId())
            .ifPresent(this::invalidateRateLimiter);
    }

    /**
     * 取数据源(走 Redis 缓存,cache miss 自动回源 DB)。
     */
    private DataSourceCacheVO loadDataSourceCached(Long dataSourceId) {
        return ruleCacheDataHelper.getDataSource(dataSourceId, dataSourceService::getDataSourceVO).orElse(null);
    }

    // ============================== Groovy 转换 ==============================

    /**
     * 应用 actionConfigJson.transformScript 配置的 Groovy 转换。
     * 失败策略:DROP(默认,丢弃)/ FORWARD(继续用原 envelope)。
     */
    private TransformOutcome applyTransformIfConfigured(DataBridgeCacheVO rule,
                                                        BridgeMessageEnvelope envelope,
                                                        BridgeTraceBuilder trace) {
        TransformScriptConfig cfg = parseTransformScript(rule.getActionConfigJson());
        if (cfg == null || !"GROOVY".equals(cfg.type) || cfg.scriptId == null) {
            return TransformOutcome.unchanged(envelope);
        }
        boolean dropOnFail = !"FORWARD".equals(cfg.failurePolicy);
        long start = System.currentTimeMillis();
        String originalView = envelope.getRawMessage();
        try {
            RuleGroovyScript script = ruleGroovyScriptService.getById(cfg.scriptId);
            if (script == null || StrUtil.isBlank(script.getScriptContent())) {
                recordTransformFailure(trace, originalView,
                    "脚本不存在或为空 scriptId=" + cfg.scriptId,
                    System.currentTimeMillis() - start, cfg.scriptId, null, dropOnFail);
                return new TransformOutcome(envelope, dropOnFail);
            }
            ScriptEntry entry = scriptLoader.compileScript(script.getScriptContent());
            ExecuteParams params = buildScriptParams(envelope, originalView);

            EngineExecutorResult result = engineExecutor.execute(entry, params);
            long latency = System.currentTimeMillis() - start;
            if (result.getExecutionStatus() == ExecutionStatus.SUCCESS) {
                String transformed = Optional.ofNullable(result.getContext())
                    .map(String::valueOf)
                    .orElse(originalView);
                BridgeMessageEnvelope tEnv = envelope.toBuilder().rawMessage(transformed).build();
                trace.addSuccessStep(BridgeStepType.TRANSFORM, "Groovy 转换",
                    BridgeTraceBuilder.truncate(originalView),
                    BridgeTraceBuilder.truncate(transformed),
                    latency,
                    MapUtil.<String, Object>builder()
                        .put("scriptId", cfg.scriptId)
                        .put("scriptName", script.getName())
                        .build());
                return TransformOutcome.unchanged(tEnv);
            }
            // 失败:记 step + 按 failurePolicy 决定丢弃 / 继续
            String errMsg = Optional.ofNullable(result.getErrorMessage())
                .or(() -> Optional.ofNullable(result.getException()).map(Throwable::getMessage))
                .orElse("unknown");
            recordTransformFailure(trace, originalView, errMsg, latency,
                cfg.scriptId, script.getName(), dropOnFail);
            return new TransformOutcome(envelope, dropOnFail);
        } catch (Exception e) {
            log.warn("[SinkDispatcher] Groovy transform error scriptId={}", cfg.scriptId, e);
            recordTransformFailure(trace, envelope.getRawMessage(),
                "执行异常 " + e.getMessage(),
                System.currentTimeMillis() - start, cfg.scriptId, null, dropOnFail);
            return new TransformOutcome(envelope, dropOnFail);
        }
    }

    private ExecuteParams buildScriptParams(BridgeMessageEnvelope envelope, String payloadView) {
        ExecuteParams params = new ExecuteParams();
        params.put("envelope", envelope);
        params.put("payload", payloadView);
        params.put("tenantId", envelope.getTenantId());
        params.put("productId", envelope.getProductIdentification());
        params.put("deviceId", envelope.getDeviceIdentification());
        params.put("topic", envelope.getTopic());
        params.put("actionType", envelope.getActionType());
        return params;
    }

    private void recordTransformFailure(BridgeTraceBuilder trace, String input, String err, long latency,
                                        Long scriptId, String scriptName, boolean dropOnFail) {
        Map<String, Object> ext = MapUtil.<String, Object>builder()
            .put("scriptId", scriptId)
            .put("failurePolicy", dropOnFail ? "DROP" : "FORWARD")
            .build();
        if (scriptName != null) {
            ext.put("scriptName", scriptName);
        }
        trace.addFailedStep(BridgeStepType.TRANSFORM, "Groovy 转换",
            BridgeTraceBuilder.truncate(input), err, latency, ext);
    }

    /**
     * TransformOutcome:envelope + 是否丢弃。
     */
    private static final class TransformOutcome {
        final BridgeMessageEnvelope envelope;
        final boolean shouldDrop;

        TransformOutcome(BridgeMessageEnvelope envelope, boolean shouldDrop) {
            this.envelope = envelope;
            this.shouldDrop = shouldDrop;
        }

        static TransformOutcome unchanged(BridgeMessageEnvelope env) {
            return new TransformOutcome(env, false);
        }
    }

    /**
     * 转换脚本子配置(actionConfigJson 已 EncryptTypeHandler 解密为明文)。
     */
    private static class TransformScriptConfig {
        String type;
        Long scriptId;
        /**
         * DROP(默认,安全优先)/ FORWARD(继续转发原 envelope)。
         */
        String failurePolicy;
    }

    @SuppressWarnings("unchecked")
    private TransformScriptConfig parseTransformScript(String actionConfigJson) {
        if (StrUtil.isBlank(actionConfigJson)) {
            return null;
        }
        try {
            Map<String, Object> ac = JsonUtil.parse(actionConfigJson, Map.class);
            if (ac == null || !(ac.get("transformScript") instanceof Map<?, ?> tsMap)) {
                return null;
            }
            TransformScriptConfig cfg = new TransformScriptConfig();
            cfg.type = MapUtil.getStr((Map<String, Object>) tsMap, "type");
            cfg.scriptId = Optional.ofNullable(tsMap.get("scriptId"))
                .map(String::valueOf)
                .map(Long::valueOf)
                .orElse(null);
            cfg.failurePolicy = MapUtil.getStr((Map<String, Object>) tsMap, "failurePolicy");
            return cfg;
        } catch (Exception e) {
            log.warn("[SinkDispatcher] parseTransformScript failed: {}", e.getMessage());
            return null;
        }
    }

    // ============================== 工具 ==============================

    /**
     * sourceType → ConnectorType,未知返 null(fail-closed)。
     */
    private ConnectorType parseConnectorType(String sourceType) {
        if (StrUtil.isBlank(sourceType)) {
            return null;
        }
        try {
            return ConnectorType.valueOf(sourceType.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Object → JSON;容错返 null。
     *
     * <p>{@link ConnectorPayload} 不直接 JSON 化 ── 它含 byte[] body 会被序列化为冗长的
     * Base64 数字数组,反而把 trace 撑爆。改为结构化 view:headers / routingKey / size / ts +
     * body(UTF-8 字符串)。
     * <p>长度截断由调用方 {@link BridgeTraceBuilder#truncate(String)} 4KB 统一兜底,本方法不再二次截。
     */
    private String asJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof ConnectorPayload payload) {
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("headers", payload.safeHeaders());
                view.put("routingKey", payload.getRoutingKey());
                view.put("size", payload.size());
                view.put("ts", payload.getTs());
                view.put("body", StrUtil.utf8Str(payload.getBody()));
                return JsonUtil.toJson(view);
            }
            return JsonUtil.toJson(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发布 trace 完成事件(异步落库)。
     */
    private void publishTrace(BridgeTraceBuilder trace) {
        try {
            eventPublisher.publishEvent(trace.toCompletedEvent());
        } catch (Exception e) {
            log.warn("[SinkDispatcher] publishTrace failed traceId={}", trace.getTraceId(), e);
        }
    }
}
