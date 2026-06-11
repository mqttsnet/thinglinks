package com.mqttsnet.thinglinks.mqs.bus.inbound.kafka;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.dto.bus.DeviceEventOutcome;
import com.mqttsnet.thinglinks.enumeration.bus.PipelineStatusEnum;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.BusPipelineDispatcher;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.SourceTopicHolder;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;

/**
 * 协议 Kafka 入站消费者基类。每条 record 独立 traceId / tenantId,失败精准 DLT。
 * 子类只需提供 @KafkaListener 方法 + {@link #protocolName()}。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractProtocolKafkaInboundConsumer {

    /**
     * Kafka header:全链路 traceId(与  AbstractEventProcessor 对齐)。
     */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    /**
     * Kafka header:租户 ID(下游 @DS(BASE_TENANT) 切库依赖)。
     */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private static final String STATS_DIM_CONSUME_FAIL = "kafka_consume_fail";

    private final BusPipelineDispatcher dispatcher;
    private final BusStatsService statsService;

    /**
     * 从 Kafka header 提取 UTF-8 值,缺失返 null。
     */
    private static String headerValue(ConsumerRecord<?, ?> record, String header) {
        if (record == null || record.headers() == null) {
            return null;
        }
        var hit = record.headers().lastHeader(header);
        return (hit == null || hit.value() == null) ? null : new String(hit.value(), StandardCharsets.UTF_8);
    }

    /**
     * 协议名,用于日志 / 指标 label。
     */
    protected abstract String protocolName();

    /**
     * 处理一批 Kafka 消息;单条失败抛 {@link BatchListenerFailedException} 触发容器重试 / DLT。
     */
    protected final void handleBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (CollUtil.isEmpty(records)) {
            Optional.ofNullable(ack).ifPresent(Acknowledgment::acknowledge);
            return;
        }
        try {
            int total = records.size();
            // 整批逐条处理;任一条抛 BatchListenerFailedException 会中断并触发该条重试/DLT,不会走到下面 ack
            IntStream.range(0, total).forEach(i -> processRecord(records.get(i), i, total));
            // 全部成功(含 CORE-FAILED 但已告警 ack 的)才提交 offset ── 真异常不提交,靠重试兜底
            Optional.ofNullable(ack).ifPresent(Acknowledgment::acknowledge);
        } finally {
            SourceTopicHolder.clear();
            ContextUtil.remove();
        }
    }

    /**
     * 单 record:重置 ContextUtil → 设 traceId/tenantId → 日志 → dispatch → 失败抛 BatchListenerFailedException。
     */
    private void processRecord(ConsumerRecord<String, String> record, int index, int total) {
        ContextUtil.remove();
        ContextUtil.setLogTraceId(
            Optional.ofNullable(headerValue(record, HEADER_TRACE_ID))
                .filter(StrUtil::isNotBlank)
                .orElseGet(IdUtil::fastSimpleUUID));
        Optional.ofNullable(headerValue(record, HEADER_TENANT_ID))
            .filter(StrUtil::isNotBlank)
            .ifPresent(ContextUtil::setTenantId);
        SourceTopicHolder.set(record.topic());
        log.info("{} protocol={} topic={} partition={} offset={} index={}/{}", BusConstants.Log.KAFKA_RECEIVE, protocolName(), record.topic(), record.partition(), record.offset(), index + 1, total);
        try {
            DeviceEventOutcome outcome = dispatcher.dispatch(record.topic(), record.value());
            // PRE/CORE 失败但未抛异常 ── 仅告警 + stats 让问题可观测,维持 ack 不重试(具体问题看日志再定位)
            if (outcome != null && outcome.getStatus() == PipelineStatusEnum.FAILED) {
                statsService.increment(STATS_DIM_CONSUME_FAIL, protocolName() + ":" + StrUtil.nullToDefault(record.topic(), "_"));
                log.error("[bus.kafka.consume] pipeline FAILED (acked,no-retry) protocol={} topic={} offset={} failedStage={}",
                    protocolName(), record.topic(), record.offset(), outcome.firstFailedStage().orElse("unknown"));
            }
        } catch (Exception e) {
            statsService.increment(STATS_DIM_CONSUME_FAIL, protocolName() + ":" + StrUtil.nullToDefault(record.topic(), "_"));
            log.error("[bus.kafka.consume] record failed protocol={} topic={} offset={} err={}", protocolName(), record.topic(), record.offset(), e.getMessage());
            throw new BatchListenerFailedException("consume failed at index=" + index + " topic=" + record.topic() + " offset=" + record.offset() + " cause=" + e.getMessage(), e, index);
        }
    }
}
