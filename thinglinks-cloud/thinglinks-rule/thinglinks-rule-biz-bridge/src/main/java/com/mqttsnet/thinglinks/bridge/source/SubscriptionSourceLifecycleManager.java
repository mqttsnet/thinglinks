package com.mqttsnet.thinglinks.bridge.source;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.databridge.model.ConnectorConfig;
import com.mqttsnet.basic.databridge.model.ConnectorType;
import com.mqttsnet.basic.databridge.model.SourceMessage;
import com.mqttsnet.basic.databridge.registry.ConnectorRegistry;
import com.mqttsnet.basic.databridge.spi.Source;
import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.bridge.event.SubscriptionSourceChangedEvent;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.service.bridge.SubscriptionSourceService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 订阅源生命周期管理器(入站桥接核心):扫描启用源建连、{@link SourceMessage} 映射为 {@link BridgeMessageEnvelope} 投 RocketMQ、{@link SubscriptionSourceChangedEvent} 联动 start/stop、关闭时优雅释放。
 * handler 回调运行在 starter 线程,无 ContextUtil 上下文,本类手动注入。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
public class SubscriptionSourceLifecycleManager {

    private static final long OFFSET_WRITE_INTERVAL_MS = 60_000L;

    private final SubscriptionSourceService subscriptionSourceService;
    private final DataSourceService dataSourceService;
    private final ConnectorRegistry connectorRegistry;
    private final RocketmqTemplate rocketmqTemplate;

    /**
     * 已启动 source 跟踪表:sourceCode → 协议类型(stop 时找正确的 Source 实现)。
     */
    private final Map<String, ConnectorType> running = new ConcurrentHashMap<>();

    /**
     * offset 节流写表(per-sourceCode 60s 最多写一次)。
     */
    private final Map<String, Long> lastOffsetWriteAt = new ConcurrentHashMap<>();

    /**
     * 启动扫描的租户 ID 列表(逗号分隔)。
     */
    @Value("${thinglinks.bridge.bootstrap.tenant-ids:1}")
    private String bootstrapTenantIdsRaw;

    /**
     * 启动恢复扫描。必须用 ApplicationReadyEvent 而非 @PostConstruct:后者执行时 dynamic-datasource AOP 可能未就绪,@DS 会 fallback 到默认库导致表找不到。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initOnApplicationReady() {
        List<Long> tenantIds = parseTenantIds();
        log.info("[SubscriptionSourceLifecycle] application ready, scanning tenants={}", tenantIds);
        tenantIds.forEach(this::scanOneTenant);
    }

    /**
     * 单租户启动扫描:切上下文 → 拉启用源 → 逐个 start → finally remove 防泄漏。
     *
     * @param tenantId 租户 ID
     */
    private void scanOneTenant(Long tenantId) {
        try {
            ContextUtil.setTenantId(tenantId);
            List<SubscriptionSource> all = subscriptionSourceService.list(
                Wrappers.<SubscriptionSource>lambdaQuery()
                    .eq(SubscriptionSource::getEnable, Boolean.TRUE));
            log.info("[SubscriptionSourceLifecycle] tenant={} found {} enabled sources",
                tenantId, all.size());
            for (SubscriptionSource src : all) {
                try {
                    startOne(src);
                } catch (Exception e) {
                    log.warn("[SubscriptionSourceLifecycle] startOne failed tenant={} sourceCode={}",
                        tenantId, src.getSourceCode(), e);
                }
            }
        } catch (Exception e) {
            log.error("[SubscriptionSourceLifecycle] startup scan failed tenant={} (non-blocking)",
                tenantId, e);
        } finally {
            ContextUtil.remove();
        }
    }

    /**
     * 逗号分隔 → Long 列表;空 / 全非法兜底 [1]。
     *
     * @return 租户 ID 列表;空 / 全非法返回 [1]
     */
    private List<Long> parseTenantIds() {
        if (StrUtil.isBlank(bootstrapTenantIdsRaw)) {
            return List.of(1L);
        }
        List<Long> ids = Arrays.stream(bootstrapTenantIdsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(this::parseLongSafe)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .distinct()
            .toList();
        return ids.isEmpty() ? List.of(1L) : ids;
    }

    private Optional<Long> parseLongSafe(String s) {
        try {
            return Optional.of(Long.valueOf(s));
        } catch (NumberFormatException e) {
            log.warn("[SubscriptionSourceLifecycle] invalid tenantId in config: {}", s);
            return Optional.empty();
        }
    }

    @PreDestroy
    public void stopAllOnShutdown() {
        Set<String> codes = new HashSet<>(running.keySet());
        log.info("[SubscriptionSourceLifecycle] shutdown stopping {} sources", codes.size());
        for (String code : codes) {
            try {
                stopOne(code);
            } catch (Exception e) {
                log.warn("[SubscriptionSourceLifecycle] stop failed sourceCode={}", code, e);
            }
        }
    }

    /**
     * 启动单订阅源(联动 SubscriptionSourceChangedEvent ENABLE / UPDATE)。幂等:已运行的先 stop。
     *
     * @param src 订阅源
     */
    public void startOne(SubscriptionSource src) {
        if (src == null || StrUtil.isBlank(src.getSourceCode())) {
            log.warn("[SubscriptionSourceLifecycle] startOne skipped: invalid src");
            return;
        }
        if (!Boolean.TRUE.equals(src.getEnable())) {
            log.debug("[SubscriptionSourceLifecycle] startOne skipped (not enabled) sourceCode={}",
                src.getSourceCode());
            return;
        }
        if (running.containsKey(src.getSourceCode())) {
            stopOne(src.getSourceCode());
        }

        DataSource ds = dataSourceService.getById(src.getDataSourceId());
        if (ds == null) {
            log.error("[SubscriptionSourceLifecycle] linked data source not found dsId={} sourceCode={}",
                src.getDataSourceId(), src.getSourceCode());
            return;
        }
        if (!Boolean.TRUE.equals(ds.getEnable())) {
            log.warn("[SubscriptionSourceLifecycle] linked data source disabled sourceCode={}",
                src.getSourceCode());
            return;
        }
        ConnectorType type = parseConnectorType(ds.getSourceType());
        if (type == null) {
            log.error("[SubscriptionSourceLifecycle] unknown sourceType={} sourceCode={}",
                ds.getSourceType(), src.getSourceCode());
            return;
        }
        Source source = connectorRegistry.getSource(type);
        if (source == null) {
            log.error("[SubscriptionSourceLifecycle] no Source impl for type={} sourceCode={}",
                type, src.getSourceCode());
            return;
        }

        try {
            source.start(toConnectorConfig(ds, src), msg -> handleSourceMessage(src, msg));
            running.put(src.getSourceCode(), type);
            log.info("[SubscriptionSourceLifecycle] started sourceCode={} type={} dsId={}",
                src.getSourceCode(), type, ds.getId());
        } catch (Exception e) {
            log.error("[SubscriptionSourceLifecycle] start failed sourceCode={}", src.getSourceCode(), e);
        }
    }

    /**
     * 停止单订阅源(联动 DISABLE / DELETE)。
     *
     * @param sourceCode 订阅源编码
     */
    public void stopOne(String sourceCode) {
        if (StrUtil.isBlank(sourceCode)) {
            return;
        }
        ConnectorType type = running.remove(sourceCode);
        if (type == null) {
            return;
        }
        Source source = connectorRegistry.getSource(type);
        if (source == null) {
            log.warn("[SubscriptionSourceLifecycle] stop: Source impl gone for type={} sourceCode={}",
                type, sourceCode);
            return;
        }
        try {
            source.stop(sourceCode);
            log.info("[SubscriptionSourceLifecycle] stopped sourceCode={} type={}", sourceCode, type);
        } catch (Exception e) {
            log.warn("[SubscriptionSourceLifecycle] stop failed sourceCode={}", sourceCode, e);
        }
    }

    @EventListener
    public void onSubscriptionSourceChanged(SubscriptionSourceChangedEvent event) {
        if (event == null || event.getEventSource() == null) {
            return;
        }
        var source = event.getEventSource();
        String op = StrUtil.nullToEmpty(source.getOperation());
        log.info("[SubscriptionSourceLifecycle] event op={} sourceCode={}", op, source.getSourceCode());
        switch (op) {
            case "CREATE", "UPDATE", "ENABLE" -> Optional.ofNullable(source.getSnapshot()).ifPresent(this::startOne);
            case "DISABLE", "DELETE" -> stopOne(source.getSourceCode());
            default -> log.warn("[SubscriptionSourceLifecycle] unknown event op={}", op);
        }
    }

    // ============================== 回调处理 ==============================

    /**
     * Source 消息回调:标准化 → 注入 ContextUtil → 投 RocketMQ INGRESS topic。
     * 本方法在 starter 内部线程运行无 ContextUtil 上下文,必须手动注入,否则下游 Consumer 恢复的 LocalMap 为空 → @DS 切库失败 / 丢 traceId。
     *
     * @param src 订阅源
     * @param msg Source 回调原始消息
     */
    private void handleSourceMessage(SubscriptionSource src, SourceMessage msg) {
        try {
            BridgeMessageEnvelope envelope = mapToEnvelope(src, msg);
            applyEnvelopeContext(envelope);  // ⭐ 让 RocketmqTemplate.wrap 可塞 header

            String dest = BizMqRouteConstant.Bridge.INGRESS + ":"
                + StrUtil.nullToDefault(src.getTargetHandler(),
                BizMqRouteConstant.Tags.INGRESS_MQTT_FORWARD);
            rocketmqTemplate.syncSend(dest, envelope);
            persistLastConsumeOffsetIfChanged(src, msg);
            log.debug("[SubscriptionSourceLifecycle] forwarded sourceCode={} traceId={} tenantId={} bytes={}",
                src.getSourceCode(), envelope.getTraceId(), envelope.getTenantId(),
                msg.getBody() == null ? 0 : msg.getBody().length);
        } catch (Exception e) {
            log.warn("[SubscriptionSourceLifecycle] forward failed sourceCode={}",
                src.getSourceCode(), e);
        } finally {
            // 清理 ThreadLocal + MDC,避免 worker 跨回调污染
            ContextUtil.remove();
        }
    }

    /**
     * 节流回写 offset(per-sourceCode 60s 最多 1 次)。
     *
     * @param src 订阅源
     * @param msg Source 回调原始消息
     */
    private void persistLastConsumeOffsetIfChanged(SubscriptionSource src, SourceMessage msg) {
        String offset = Optional.ofNullable(msg.getSourceMessageId())
            .filter(StrUtil::isNotBlank)
            .orElseGet(msg::getRoutingKey);
        if (StrUtil.isBlank(offset) || src.getId() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long prev = lastOffsetWriteAt.get(src.getSourceCode());
        if (prev != null && now - prev < OFFSET_WRITE_INTERVAL_MS) {
            return;
        }
        lastOffsetWriteAt.put(src.getSourceCode(), now);
        try {
            SubscriptionSource patch = new SubscriptionSource();
            patch.setId(src.getId());
            patch.setLastConsumeOffset(offset);
            subscriptionSourceService.updateById(patch);
        } catch (Exception e) {
            log.warn("[SubscriptionSourceLifecycle] persist offset failed sourceCode={} offset={}",
                src.getSourceCode(), offset, e);
        }
    }

    /**
     * 把 envelope 的 tenantId / traceId 注入 ContextUtil。tenantId fail-fast 防默认库串扰。
     *
     * @param envelope 桥接消息信封
     */
    private void applyEnvelopeContext(BridgeMessageEnvelope envelope) {
        if (StrUtil.isBlank(envelope.getTenantId())) {
            throw new IllegalStateException(
                "envelope.tenantId is blank; subscription_source.mapping_json must contain "
                    + "{\"sourceField\":\"...\",\"targetField\":\"tenantId\"} mapping");
        }
        try {
            ContextUtil.setTenantId(Long.valueOf(envelope.getTenantId()));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                "envelope.tenantId is not a valid long: " + envelope.getTenantId(), e);
        }
        if (StrUtil.isNotBlank(envelope.getTraceId())) {
            ContextUtil.setLogTraceId(envelope.getTraceId());
        }
    }

    /**
     * SourceMessage → BridgeMessageEnvelope,按 mapping_json 平移字段。
     *
     * @param src 订阅源
     * @param msg Source 回调原始消息
     * @return 桥接消息信封
     */
    private BridgeMessageEnvelope mapToEnvelope(SubscriptionSource src, SourceMessage msg) {
        String rawText = msg.getBody() == null ? null : new String(msg.getBody());

        Map<String, Object> parsedFields = new HashMap<>();
        if (StrUtil.isNotBlank(rawText) && JSON.isValidObject(rawText)) {
            parsedFields.putAll(JSON.parseObject(rawText));
        }
        Map<String, Object> mapped = applyMapping(src.getMappingJson(), parsedFields);

        BridgeMessageEnvelope.BridgeMessageEnvelopeBuilder b = BridgeMessageEnvelope.builder()
            .traceId(Optional.ofNullable(msg.getSourceMessageId())
                .filter(StrUtil::isNotBlank)
                .orElseGet(ContextUtil::getLogTraceId))
            .actionType(DeviceActionTypeEnum.INBOUND.getValue())
            .protocolType(ProtocolTypeEnum.BRIDGE_INGRESS.getValue())
            .ts(msg.getTs() <= 0 ? System.currentTimeMillis() : msg.getTs())
            .rawMessage(rawText)
            .productIdentification(MapUtil.getStr(mapped, "productIdentification"))
            .deviceIdentification(MapUtil.getStr(mapped, "deviceIdentification"))
            .topic(StrUtil.isNotBlank(src.getTargetTopicTemplate())
                ? src.getTargetTopicTemplate()
                : msg.getRoutingKey());

        // tenantId 跨租户场景需在 mapping 显式映射
        Optional.ofNullable(MapUtil.getStr(mapped, "tenantId"))
            .filter(StrUtil::isNotBlank)
            .ifPresent(b::tenantId);
        return b.build();
    }

    /**
     * 按 mapping_json 重命名字段(hutool {@link CopyOptions#setFieldMapping})。
     *
     * @param mappingJson 字段映射 JSON 数组
     * @param source      原始字段 map
     * @return 重命名后的字段 map
     */
    private Map<String, Object> applyMapping(String mappingJson, Map<String, Object> source) {
        if (StrUtil.isBlank(mappingJson) || MapUtil.isEmpty(source)) {
            return new HashMap<>();
        }
        Map<String, String> renameMap = parseRenameMap(mappingJson);
        if (renameMap.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> target = new HashMap<>();
        BeanUtil.copyProperties(source, target,
            CopyOptions.create().setFieldMapping(renameMap).ignoreNullValue());
        return target;
    }

    private Map<String, String> parseRenameMap(String mappingJson) {
        if (!JSON.isValidArray(mappingJson)) {
            log.warn("[SubscriptionSourceLifecycle] invalid mapping_json (not array): {}",
                StrUtil.maxLength(mappingJson, 100));
            return new HashMap<>();
        }
        Map<String, String> renameMap = new HashMap<>();
        JSON.parseArray(mappingJson).forEach(o -> {
            if (!(o instanceof JSONObject item)) {
                return;
            }
            String sf = item.getString("sourceField");
            String tf = item.getString("targetField");
            if (StrUtil.isAllNotBlank(sf, tf)) {
                renameMap.put(sf, tf);
            }
        });
        return renameMap;
    }

    private ConnectorConfig toConnectorConfig(DataSource ds, SubscriptionSource src) {
        return ConnectorConfig.builder()
            .type(parseConnectorType(ds.getSourceType()))
            .identifier(src.getSourceCode())  // identifier = sourceCode 保证 Source 实现层 startOne/stopOne 同 key
            .connectionJson(ds.getConnectionJson())
            .credentialJson(ds.getCredentialJson())
            .extraConfigJson(ds.getExtendParams())
            .serialization(StrUtil.nullToDefault(ds.getSerialization(), "JSON"))
            .build();
    }

    /**
     * sourceType → ConnectorType,未知返 null(fail-closed)。
     *
     * @param sourceType 源类型字符串
     * @return 连接器类型;未知返 null
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
}
