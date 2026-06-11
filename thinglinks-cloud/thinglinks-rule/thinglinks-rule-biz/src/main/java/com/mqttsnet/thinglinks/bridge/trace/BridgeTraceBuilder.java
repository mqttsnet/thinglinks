package com.mqttsnet.thinglinks.bridge.trace;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 桥接 trace 上下文构造器,一次 dispatch 累积元信息 + N 个 step,完成后发事件异步落库。
 * 非线程安全:一个 trace 对象归属一个线程,每次 dispatch 新建实例。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
public class BridgeTraceBuilder {

    public static final String STATUS_SUCCESS = "00";
    public static final String STATUS_FAILED = "01";
    /**
     * 部分成功(多 sink / 限流跳过等场景)。
     */
    public static final String STATUS_PARTIAL = "02";
    public static final String STATUS_DEAD_LETTER = "03";

    public static final String STEP_STATUS_SUCCESS = "00";
    public static final String STEP_STATUS_FAILED = "01";
    public static final String STEP_STATUS_SKIPPED = "02";

    public static final String TRIGGER_DEVICE_DATA = "DEVICE_DATA";
    public static final String TRIGGER_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String TRIGGER_TEST_SINK = "TEST_SINK";
    public static final String TRIGGER_REPLAY = "REPLAY";

    /**
     * step.input/output 摘要字段最大长度 4KB。注意 trace.source_payload_summary 是 longtext 存完整 envelope 原文,不走本截断。
     */
    public static final int SUMMARY_MAX_LENGTH = 4096;

    /**
     * 错误信息最大长度,4000 字符完整保留堆栈型错误。
     */
    public static final int ERROR_MSG_MAX_LENGTH = 4000;

    /**
     * trace.result_summary 最大长度(摘要级)。
     */
    public static final int RESULT_SUMMARY_MAX_LENGTH = 2000;

    @Getter
    private final BridgeExecutionTrace trace;

    @Getter
    private final List<BridgeExecutionStep> steps = new ArrayList<>();

    private final long startNanos;

    private int stepCounter = 0;

    private BridgeTraceBuilder(BridgeExecutionTrace trace, long startNanos) {
        this.trace = trace;
        this.startNanos = startNanos;
    }

    /**
     * 启动 trace 上下文。traceId 优先级:ContextUtil → envelope.traceId → snowflake 兜底。
     * 同一 envelope 命中多条规则时 traceId 相同,由 trace 表联合唯一索引 (trace_id, bridge_rule_id) 区分。
     *
     * @param envelope 桥接消息封装
     * @param rule     命中的数据桥接规则缓存(可为 null)
     * @return 新建的 trace 构造器
     */
    public static BridgeTraceBuilder start(BridgeMessageEnvelope envelope, DataBridgeCacheVO rule) {
        BridgeExecutionTrace trace = new BridgeExecutionTrace();
        trace.setTraceId(resolveTraceId(envelope));
        trace.setStartTime(LocalDateTime.now());
        trace.setTriggerSource(TRIGGER_DEVICE_DATA);
        trace.setTenantId(envelope.getTenantId());
        trace.setProductIdentification(envelope.getProductIdentification());
        trace.setDeviceIdentification(envelope.getDeviceIdentification());
        trace.setActionType(envelope.getActionType());
        trace.setTopic(envelope.getTopic());
        // source_payload_summary 是 longtext,存完整 envelope 原文(排查 + 死信回放需要全量)
        trace.setSourcePayloadSummary(envelope.getRawMessage());
        if (rule != null) {
            trace.setBridgeRuleId(rule.getId());
            trace.setDirection(rule.getDirection());
            trace.setDataSourceId(rule.getDataSourceId());
        }
        return new BridgeTraceBuilder(trace, System.nanoTime());
    }

    /**
     * 非设备数据触发的 trace(test / replay 等)。
     *
     * @param envelope      桥接消息封装
     * @param rule          命中的数据桥接规则缓存(可为 null)
     * @param triggerSource 触发来源标识
     * @return 新建的 trace 构造器
     */
    public static BridgeTraceBuilder startManual(BridgeMessageEnvelope envelope, DataBridgeCacheVO rule,
                                                 String triggerSource) {
        BridgeTraceBuilder b = start(envelope, rule);
        b.trace.setTriggerSource(triggerSource);
        return b;
    }

    public BridgeTraceBuilder addSuccessStep(BridgeStepType type, String name,
                                             String input, String output, long latency,
                                             Map<String, Object> ext) {
        return appendStep(type, name, STEP_STATUS_SUCCESS, input, output, null, latency, ext);
    }

    public BridgeTraceBuilder addFailedStep(BridgeStepType type, String name,
                                            String input, String errorMsg, long latency,
                                            Map<String, Object> ext) {
        return appendStep(type, name, STEP_STATUS_FAILED, input, null, errorMsg, latency, ext);
    }

    /**
     * reason 写入 errorMsg 字段供前端展示。
     *
     * @param type   步骤类型
     * @param name   步骤名称
     * @param reason 跳过原因(写入 errorMsg 字段)
     * @return 当前 trace 构造器
     */
    public BridgeTraceBuilder addSkippedStep(BridgeStepType type, String name, String reason) {
        return appendStep(type, name, STEP_STATUS_SKIPPED, null, null, reason, 0L, null);
    }

    /**
     * 标记 trace 完成。
     *
     * @param status        trace 状态码
     * @param resultSummary 结果摘要
     */
    public void end(String status, String resultSummary) {
        trace.setEndTime(LocalDateTime.now());
        trace.setTotalLatencyMs((int) ((System.nanoTime() - startNanos) / 1_000_000L));
        trace.setStatus(status);
        trace.setResultSummary(StrUtil.maxLength(resultSummary, RESULT_SUMMARY_MAX_LENGTH));
        trace.setStepCount(steps.size());
    }

    /**
     * 标记 trace 失败 + 写错误(透传下游 raw 错误,按 ERROR_MSG_MAX_LENGTH 截尾)。
     *
     * @param errorMsg 错误信息
     */
    public void endWithError(String errorMsg) {
        end(STATUS_FAILED, "执行失败");
        trace.setErrorMsg(StrUtil.maxLength(errorMsg, ERROR_MSG_MAX_LENGTH));
    }

    public BridgeTraceCompletedEvent toCompletedEvent() {
        return new BridgeTraceCompletedEvent(trace.getTraceId(), trace, steps);
    }

    public String getTraceId() {
        return trace.getTraceId();
    }

    /**
     * 截断到 SUMMARY_MAX_LENGTH,null 安全。
     *
     * @param s 待截断字符串
     * @return 截断后的字符串
     */
    public static String truncate(String s) {
        return StrUtil.maxLength(s, SUMMARY_MAX_LENGTH);
    }

    // ============================== 内部 ==============================

    private BridgeTraceBuilder appendStep(BridgeStepType type, String name,
                                          String status, String input, String output, String errorMsg,
                                          long latency, Map<String, Object> ext) {
        BridgeExecutionStep step = new BridgeExecutionStep();
        step.setTraceId(trace.getTraceId());
        step.setBridgeRuleId(trace.getBridgeRuleId());
        step.setStepNo(++stepCounter);
        step.setStepType(type.getValue());
        step.setStepName(StrUtil.isBlank(name) ? type.getDesc() : name);
        step.setStatus(status);
        step.setInputSummary(truncate(input));
        step.setOutputSummary(truncate(output));
        step.setErrorMsg(StrUtil.maxLength(errorMsg, ERROR_MSG_MAX_LENGTH));
        step.setLatencyMs((int) latency);
        step.setStartedAt(LocalDateTime.now());
        if (ext != null && !ext.isEmpty()) {
            step.setExtendParams(JsonUtil.toJson(ext));
        }
        steps.add(step);
        return this;
    }

    /**
     * ContextUtil(优先) → envelope → snowflake 兜底;正常路径不应到兜底,触发表示入口漏配 ContextUtil。
     *
     * @param envelope 桥接消息封装
     * @return 解析出的 traceId
     */
    private static String resolveTraceId(BridgeMessageEnvelope envelope) {
        return Optional.ofNullable(ContextUtil.getLogTraceId())
                .filter(StrUtil::isNotBlank)
                .or(() -> Optional.ofNullable(envelope)
                        .map(BridgeMessageEnvelope::getTraceId)
                        .filter(StrUtil::isNotBlank))
                .orElseGet(SnowflakeIdUtil::nextId);
    }
}
