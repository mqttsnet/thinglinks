package com.mqttsnet.thinglinks.service.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.databridge.model.ConnectorConfig;
import com.mqttsnet.basic.databridge.model.ConnectorPayload;
import com.mqttsnet.basic.databridge.model.ConnectorType;
import com.mqttsnet.basic.databridge.model.SendResult;
import com.mqttsnet.basic.databridge.registry.ConnectorRegistry;
import com.mqttsnet.basic.databridge.spi.Sink;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.bridge.enrich.DataSourceInfoAttacher;
import com.mqttsnet.thinglinks.bridge.event.DataSourceDeleteCheckEvent;
import com.mqttsnet.thinglinks.bridge.event.publisher.BridgeEventPublisher;
import com.mqttsnet.thinglinks.bridge.trace.BridgeStepType;
import com.mqttsnet.thinglinks.bridge.trace.BridgeTraceBuilder;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.manager.bridge.DataBridgeManager;
import com.mqttsnet.thinglinks.service.bridge.DataBridgeService;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.vo.query.bridge.DataBridgePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataBridgeResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataBridgeSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataBridgeUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据桥接-规则业务实现。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DataBridgeServiceImpl
        extends SuperServiceImpl<DataBridgeManager, Long, DataBridge>
        implements DataBridgeService {

    private static final String SERIALIZATION_DEFAULT = "JSON";

    private final DataSourceInfoAttacher dataSourceInfoAttacher;
    private final ApplicationEventPublisher eventPublisher;
    private final BridgeEventPublisher bridgeEventPublisher;

    /**
     * 跨域字段注入避循环依赖。
     */
    @Autowired
    private DataSourceService dataSourceService;

    @Autowired(required = false)
    private ConnectorRegistry connectorRegistry;

    @Override
    public List<DataBridgeResultVO> getDataBridgeResultVOList(DataBridgePageQuery query) {
        List<DataBridgeResultVO> voList = BeanPlusUtil.copyToList(
                superManager.getDataBridgeList(query), DataBridgeResultVO.class);
        // 列表屏蔽 actionConfigJson 明文 + 批量 join 数据源
        voList.forEach(vo -> vo.setActionConfigJson(null));
        attachDataSourceInfo(voList);
        return voList;
    }

    @Override
    public DataBridgeResultVO getDataBridgeDetail(Long id) {
        DataBridge entity = superManager.getById(id);
        ArgumentAssert.notNull(entity, "桥接规则不存在或已被删除");
        DataBridgeResultVO vo = new DataBridgeResultVO();
        BeanUtils.copyProperties(entity, vo);
        attachDataSourceInfo(Collections.singletonList(vo));
        return vo;
    }

    /**
     * 委托 attacher 批量反填 dataSourceCode + dataSourceName。
     */
    @Override
    public void attachDataSourceInfo(List<DataBridgeResultVO> voList) {
        dataSourceInfoAttacher.attach(voList,
                DataBridgeResultVO::getDataSourceId,
                (vo, ds) -> {
                    vo.setDataSourceCode(ds.getDataSourceCode());
                    vo.setDataSourceName(ds.getDataSourceName());
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataBridgeSaveVO saveDataBridge(DataBridgeSaveVO saveVO) {
        log.info("保存桥接规则: name={} direction={}", saveVO.getRuleName(), saveVO.getDirection());
        ArgumentAssert.notNull(dataSourceService.getById(saveVO.getDataSourceId()), "关联数据源不存在");

        DataBridge entity = new DataBridge();
        BeanUtils.copyProperties(saveVO, entity);
        if (StrUtil.isBlank(entity.getRuleCode())) {
            entity.setRuleCode(SnowflakeIdUtil.nextId());
        }
        entity.setEnable(Boolean.FALSE);

        ArgumentAssert.isTrue(superManager.save(entity), "桥接规则保存失败");
        bridgeEventPublisher.publishRuleChangedEvent(entity);
        return saveVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataBridgeUpdateVO updateDataBridge(DataBridgeUpdateVO updateVO) {
        log.info("更新桥接规则: id={}", updateVO.getId());
        ArgumentAssert.notNull(superManager.getById(updateVO.getId()), "桥接规则不存在或已被删除");
        ArgumentAssert.notNull(dataSourceService.getById(updateVO.getDataSourceId()), "关联数据源不存在");

        DataBridge entity = new DataBridge();
        BeanUtils.copyProperties(updateVO, entity);
        ArgumentAssert.isTrue(superManager.updateById(entity), "桥接规则更新失败");
        bridgeEventPublisher.publishRuleChangedEvent(entity);
        return updateVO;
    }

    @Override
    public Map<String, Object> testSink(Long id, Map<String, Object> sampleEnvelope) {
        DataBridge rule = superManager.getById(id);
        ArgumentAssert.notNull(rule, "桥接规则不存在或已被删除");
        DataSource ds = dataSourceService.getById(rule.getDataSourceId());
        ArgumentAssert.notNull(ds, "关联数据源不存在");
        if (!Boolean.TRUE.equals(ds.getEnable())) {
            throw BizException.wrap("关联数据源未启用,请先启用数据源后再测试");
        }
        if (connectorRegistry == null) {
            throw BizException.wrap("数据桥接 starter 未启用,无法测试发送");
        }
        ConnectorType type = ConnectorType.valueOf(ds.getSourceType());
        Sink sink = connectorRegistry.getSink(type);
        ArgumentAssert.notNull(sink, "未注册 " + type + " 类型的 Sink");

        // trace 上下文(testSink 也写入,详情页"最近日志"可见)
        BridgeMessageEnvelope traceEnv = buildEnvelopeForTrace(sampleEnvelope);
        DataBridgeCacheVO ruleVO = BeanPlusUtil.toBeanIgnoreError(rule, DataBridgeCacheVO.class);
        BridgeTraceBuilder trace = BridgeTraceBuilder.startManual(
                traceEnv, ruleVO, BridgeTraceBuilder.TRIGGER_TEST_SINK);
        long ingestStart = System.currentTimeMillis();
        trace.addSuccessStep(BridgeStepType.INGEST, "测试样例输入",
                BridgeTraceBuilder.truncate(asJson(sampleEnvelope)),
                null, System.currentTimeMillis() - ingestStart, null);

        ConnectorPayload payload = ConnectorPayload.builder()
                .body(toBodyBytes(sampleEnvelope))
                .headers(new HashMap<>())
                .routingKey("test-" + System.currentTimeMillis())
                .ts(System.currentTimeMillis())
                .build();
        ConnectorConfig cfg = ConnectorConfig.builder()
                .type(type)
                .identifier("testSink-" + ds.getDataSourceCode())
                .connectionJson(ds.getConnectionJson())
                .credentialJson(ds.getCredentialJson())
                .extraConfigJson(ds.getExtendParams())
                .serialization(StrUtil.nullToDefault(ds.getSerialization(), SERIALIZATION_DEFAULT))
                .build();

        SendResult r;
        try {
            r = sink.send(payload, cfg);
        } catch (Exception e) {
            trace.addFailedStep(BridgeStepType.SINK_SEND, type.name() + " 投递",
                    BridgeTraceBuilder.truncate(asJson(sampleEnvelope)),
                    e.getMessage(), 0L, null);
            trace.endWithError("测试发送异常: " + e.getMessage());
            publishTrace(trace);
            throw e;
        }

        recordSendStep(trace, type, r, sampleEnvelope, ds.getId());
        publishTrace(trace);
        return buildResult(r, trace.getTraceId());
    }

    private byte[] toBodyBytes(Map<String, Object> sample) {
        if (sample == null || sample.isEmpty()) {
            return "{}".getBytes(StandardCharsets.UTF_8);
        }
        byte[] body = JsonUtil.toJsonAsBytes(sample);
        return body.length > 0 ? body : "{}".getBytes(StandardCharsets.UTF_8);
    }

    private void recordSendStep(BridgeTraceBuilder trace, ConnectorType type, SendResult r,
                                Map<String, Object> sampleEnvelope, Long dsId) {
        Map<String, Object> ext = new HashMap<>();
        ext.put("sinkType", type.name());
        ext.put("dataSourceId", dsId);
        ext.put("messageId", r.getMessageId());
        if (r.isSuccess()) {
            trace.addSuccessStep(BridgeStepType.SINK_SEND, type.name() + " 投递",
                    BridgeTraceBuilder.truncate(asJson(sampleEnvelope)),
                    BridgeTraceBuilder.truncate(asJson(r.safeAttributes())),
                    r.getLatencyMs(), ext);
            trace.end(BridgeTraceBuilder.STATUS_SUCCESS,
                    "测试发送成功 messageId=" + r.getMessageId());
        } else {
            String err = StrUtil.nullToDefault(r.errorMessage(), "测试发送失败");
            trace.addFailedStep(BridgeStepType.SINK_SEND, type.name() + " 投递",
                    BridgeTraceBuilder.truncate(asJson(sampleEnvelope)),
                    err, r.getLatencyMs(), ext);
            trace.end(BridgeTraceBuilder.STATUS_FAILED, err);
        }
    }

    private Map<String, Object> buildResult(SendResult r, String traceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", r.isSuccess());
        result.put("messageId", r.getMessageId());
        result.put("latencyMs", r.getLatencyMs());
        result.put("errorCode", r.getErrorCode());
        result.put("errorMessage", r.errorMessage());
        result.put("attributes", r.safeAttributes());
        result.put("traceId", traceId);  // 前端可跳详情抽屉
        return result;
    }

    /**
     * 从 sampleEnvelope 提取业务字段构造 envelope 给 trace 用。缺失时给占位值,保证 trace 可筛选/展示。
     * traceId 不设 ── 由 BridgeTraceBuilder.start 统一从 ContextUtil 读上游 traceId。
     */
    private BridgeMessageEnvelope buildEnvelopeForTrace(Map<String, Object> sample) {
        Map<String, Object> s = Optional.ofNullable(sample).orElseGet(HashMap::new);
        return BridgeMessageEnvelope.builder()
                .tenantId(strOf(s.get(CommonIotConstants.TENANT_ID)))
                .productIdentification(strOf(s.get("productIdentification")))
                .deviceIdentification(strOf(s.get("deviceIdentification")))
                .actionType(strOf(s.get("actionType")))
                .topic(strOf(s.get(CommonIotConstants.TOPIC)))
                .rawMessage(asJson(s))
                .ts(System.currentTimeMillis())
                .build();
    }

    private String strOf(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private String asJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return JsonUtil.toJson(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    /**
     * 异步落库 trace,异常仅 warn 不阻塞主返回。
     */
    private void publishTrace(BridgeTraceBuilder trace) {
        try {
            eventPublisher.publishEvent(trace.toCompletedEvent());
        } catch (Exception e) {
            log.warn("[testSink] publishTrace failed traceId={}", trace.getTraceId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Boolean enable) {
        DataBridge rule = superManager.getById(id);
        ArgumentAssert.notNull(rule, "桥接规则不存在或已被删除");
        // 启用前后端兜底:要求关联数据源 enable=true
        if (Boolean.TRUE.equals(enable)) {
            DataSource ds = dataSourceService.getById(rule.getDataSourceId());
            ArgumentAssert.notNull(ds, "关联数据源不存在");
            if (!Boolean.TRUE.equals(ds.getEnable())) {
                throw BizException.wrap("关联数据源未启用,请先启用数据源");
            }
        }
        rule.setEnable(enable);
        boolean ok = superManager.updateById(rule);
        bridgeEventPublisher.publishRuleChangedEvent(rule);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyRule(Long id) {
        DataBridge src = superManager.getById(id);
        ArgumentAssert.notNull(src, "桥接规则不存在或已被删除");

        DataBridge copy = new DataBridge();
        BeanUtils.copyProperties(src, copy);
        copy.setId(null);
        copy.setRuleCode(SnowflakeIdUtil.nextId());
        copy.setRuleName(src.getRuleName() + " (副本)");
        copy.setEnable(Boolean.FALSE);
        copy.setCreatedTime(null);
        copy.setUpdatedTime(null);
        ArgumentAssert.isTrue(superManager.save(copy), "复制规则失败");
        bridgeEventPublisher.publishRuleChangedEvent(copy);
        return copy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDataBridge(Long id) {
        DataBridge rule = superManager.getById(id);
        ArgumentAssert.notNull(rule, "桥接规则不存在或已被删除");
        // 启用中规则禁止直接删,避免 matcher 缓存还在分发但 DB 已消失
        if (Boolean.TRUE.equals(rule.getEnable())) {
            throw BizException.wrap("启用中的规则不允许删除,请先禁用后再删");
        }
        boolean ok = superManager.removeById(id);
        if (ok) {
            bridgeEventPublisher.publishRuleDeletedEvent(rule);
        }
        return ok;
    }

    @Override
    public List<DataBridgeResultVO> getEnabledRules(String appId, String direction) {
        return BeanPlusUtil.copyToList(superManager.getEnabledRules(appId, direction),
                DataBridgeResultVO.class);
    }

    /**
     * 监听 DataSource 待删除事件,持有规则则抛错阻止删除(切断循环依赖)。
     */
    @EventListener
    public void onDataSourceDeleteCheck(DataSourceDeleteCheckEvent event) {
        Long dsId = event.getEventSource().getDataSourceId();
        long ruleCount = superManager.count(Wrappers.<DataBridge>lambdaQuery()
                .eq(DataBridge::getDataSourceId, dsId));
        if (ruleCount > 0) {
            throw BizException.wrap("此数据源被 " + ruleCount + " 条桥接规则引用,请先删除关联规则后重试");
        }
        long dlqCount = superManager.count(Wrappers.<DataBridge>lambdaQuery()
                .eq(DataBridge::getDeadLetterDataSourceId, dsId));
        if (dlqCount > 0) {
            throw BizException.wrap("此数据源被 " + dlqCount + " 条桥接规则配为死信目标,请先调整规则的死信配置");
        }
    }
}
