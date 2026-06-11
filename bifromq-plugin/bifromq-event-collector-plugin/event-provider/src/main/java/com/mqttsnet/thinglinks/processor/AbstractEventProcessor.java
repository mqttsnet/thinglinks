/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks.processor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baidu.bifromq.plugin.eventcollector.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.enumeration.EventTypeEnum;
import com.mqttsnet.thinglinks.sender.KafkaMessageSender;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象事件处理器
 * 提供通用的事件处理逻辑和辅助方法
 *
 * <h3>跨服务上下文透传</h3>
 * 每条事件自动生成 {@code traceId}(Hutool 雪花 simple UUID,小写无横线),并把
 * {@code traceId} + {@code tenantId} 塞 Kafka header,与下游 mqs / rule 等服务的
 * Kafka consumer 拉取后 {@link ContextUtil#setLogTraceId} / {@code setTenantId} 对齐,
 * 不必再去 JSON body 兜底解析。
 *
 * @author mqttsnet
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractEventProcessor implements EventProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Kafka header key:全链路 traceId(与下游 mqs {@code AbstractProtocolKafkaInboundConsumer.HEADER_TRACE_ID} 对齐)。
     */
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * Kafka header key:租户 ID(与下游 mqs {@code AbstractProtocolKafkaInboundConsumer.HEADER_TENANT_ID} 对齐)。
     */
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * Kafka header key:JSON body 内 traceId 字段名;同时在 body 内塞一份,方便日志 / 排查直接看 message 即可。
     */
    private static final String FIELD_TRACE_ID = "traceId";

    @Override
    public void process(Event<?> event, String topic, KafkaMessageSender sender,
                        long eventHlc, long eventUtc) {
        log.info("Processing event type={} hlc={} utc={}", event.type(), eventHlc, eventUtc);
        try {
            Map<String, Object> eventData = buildEventData(event);
            // ① 生成 traceId(小写无横线 UUID)── 单条消息全链路追踪锚点
            String traceId = IdUtil.fastSimpleUUID();
            enrichEventData(eventData, event, traceId, eventHlc, eventUtc);

            // ② 构建 Kafka header:traceId 必传,tenantId 有则透传(下游 @DS 切库依赖)
            Map<String, String> headers = buildHeaders(eventData, traceId);

            // ③ partition key 用 clientId ── 同设备事件落同一 partition,缓解 partition 倾斜 + 便于按设备排查;
            //    顺序保序由下游 HLC CAS 单调写承担,partition key 不是为顺序而设
            String partitionKey = (String) eventData.get(CommonConstants.CLIENT_ID);

            String message = OBJECT_MAPPER.writeValueAsString(eventData);
            sender.send(topic, partitionKey, message, headers);
            if (log.isDebugEnabled()) {
                log.debug("Sent event topic={} key={} traceId={} tenantId={} hlc={}",
                    topic, partitionKey, traceId, headers.get(HEADER_TENANT_ID), eventHlc);
            } else {
                log.info("Sent event topic={} traceId={} hlc={}", topic, traceId, eventHlc);
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", event.type(), e);
        }
    }

    /**
     * 构建事件数据
     *
     * @param event 事件对象
     * @return 事件数据映射
     */
    protected abstract Map<String, Object> buildEventData(Event<?> event);

    /**
     * 丰富事件数据 ── 通用元字段(3 个时间字段语义分离).
     * <p>
     * <ul>
     *   <li>{@code eventTime} ── plugin enrichEventData 调用瞬间的 wall clock,plugin 处理调度时间</li>
     *   <li>{@code eventUtc}  ── {@code Event.utc()},事件发生瞬间(物理 UTC ms)</li>
     *   <li>{@code eventHlc}  ── {@code Event.hlc()},HLC 因果时钟(下游单调写权威键)</li>
     * </ul>
     *
     * @param eventData 事件数据
     * @param event     事件对象
     * @param traceId   本条消息的全链路追踪 ID
     * @param eventHlc  broker 内核 HLC(report 入口同步抓取)
     * @param eventUtc  broker 内核 UTC ms
     */
    private void enrichEventData(Map<String, Object> eventData, Event<?> event, String traceId,
                                 long eventHlc, long eventUtc) {
        // 事件类型
        eventData.put(CommonConstants.EVENT_TYPE,
            EventTypeEnum.byValue(event.type().name()).get().getBusinessSystemEventType());

        // plugin 处理时刻(worker 取值瞬间)
        long processTime = System.currentTimeMillis();
        eventData.put(CommonConstants.EVENT_TIME, processTime);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(processTime),
            ZoneId.systemDefault());
        eventData.put(CommonConstants.EVENT_TIME_STR, dateTime.format(DATE_TIME_FORMATTER));

        // ⭐ broker 内核因果时钟(下游单调写权威 key)+ 物理 UTC 锚点
        // mqs 下游 DeviceHeartbeatStage / DevicePayloadDecodeStage / BridgeRelayStage 统一用 eventUtc 作物理时间.
        eventData.put(CommonConstants.EVENT_HLC, eventHlc);
        eventData.put(CommonConstants.EVENT_UTC, eventUtc);

        eventData.put(CommonConstants.SUCCESS, CommonConstants.SUCCESS);

        // traceId 双写(header + body),方便排查时 grep message 直接看到
        eventData.put(FIELD_TRACE_ID, traceId);
    }

    /**
     * 构建 Kafka header ── traceId 必传,tenantId 有则透传。
     *
     * <p>下游 mqs {@code AbstractProtocolKafkaInboundConsumer.handleOne} 从这两个 header 恢复
     * {@link com.mqttsnet.basic.context.ContextUtil} 上下文,后续 {@code @DS(BASE_TENANT)} 切库依赖此。
     *
     * @param eventData 事件数据(用于提取 tenantId)
     * @param traceId   本条消息的 traceId
     * @return Kafka header map(永不 null)
     */
    private Map<String, String> buildHeaders(Map<String, Object> eventData, String traceId) {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HEADER_TRACE_ID, traceId);
        Object tenantIdObj = eventData.get(CommonConstants.TENANT_ID);
        if (tenantIdObj != null) {
            String tenantId = String.valueOf(tenantIdObj);
            if (CharSequenceUtil.isNotBlank(tenantId)) {
                headers.put(HEADER_TENANT_ID, tenantId);
            }
        }
        return headers;
    }

    /**
     * 安全地获取值，避免空指针
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return 值或默认值
     */
    protected <T> T getSafeValue(T value, T defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

}
