package com.mqttsnet.thinglinks.service.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.bridge.dispatcher.SinkDispatcher;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.manager.bridge.BridgeExecutionTraceManager;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionStepService;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionTraceService;
import com.mqttsnet.thinglinks.service.bridge.DataBridgeService;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionStepResultVO;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionTraceResultVO;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionTraceStatsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 桥接执行 trace 业务实现。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class BridgeExecutionTraceServiceImpl
        extends SuperServiceImpl<BridgeExecutionTraceManager, Long, BridgeExecutionTrace>
        implements BridgeExecutionTraceService {

    private static final String STATUS_DEAD_LETTER = "03";
    private static final int TOP_RULE_LIMIT = 8;

    private final SinkDispatcher sinkDispatcher;

    /**
     * 跨域字段注入避循环依赖。
     */
    @Autowired
    private BridgeExecutionStepService stepService;

    @Autowired
    private DataBridgeService dataBridgeService;

    @Override
    public List<BridgeExecutionTraceResultVO> getTraceResultVOList(BridgeExecutionTracePageQuery query) {
        return BeanPlusUtil.copyToList(superManager.getTraceList(query), BridgeExecutionTraceResultVO.class);
    }

    @Override
    public BridgeExecutionTraceStatsResultVO getTraceStats(BridgeExecutionTracePageQuery query) {
        BridgeExecutionTraceStatsResultVO result = superManager.getTraceStats(query);
        result.setTimeline(buildTimeline(query));
        result.setTopRules(superManager.getTraceTopRules(query, TOP_RULE_LIMIT));
        return result;
    }

    @Override
    public BridgeExecutionTraceResultVO getTraceDetail(String traceId) {
        return getTraceDetail(traceId, null);
    }

    @Override
    public BridgeExecutionTraceResultVO getTraceDetail(String traceId, Long ruleId) {
        BridgeExecutionTrace trace = ruleId != null
                ? superManager.getByTraceIdAndRuleId(traceId, ruleId)
                : superManager.getByTraceId(traceId);
        ArgumentAssert.notNull(trace, "trace 不存在");

        BridgeExecutionTraceResultVO vo = new BridgeExecutionTraceResultVO();
        BeanUtils.copyProperties(trace, vo);
        // 按 (traceId, ruleId) 精确查 step 避免多规则混合
        vo.setSteps(BeanPlusUtil.copyToList(
                stepService.getStepsByTraceIdAndRuleId(traceId, trace.getBridgeRuleId()),
                BridgeExecutionStepResultVO.class));
        return vo;
    }

    @Override
    public String replay(String traceId) {
        BridgeExecutionTrace trace = superManager.getByTraceId(traceId);
        ArgumentAssert.notNull(trace, "trace 不存在");
        if (!STATUS_DEAD_LETTER.equals(trace.getStatus())) {
            throw BizException.wrap("仅死信状态(status=03)的 trace 可重放,当前状态:" + trace.getStatus());
        }
        if (trace.getBridgeRuleId() == null) {
            throw BizException.wrap("trace 未关联桥接规则,无法重放(可能是订阅源拉取失败的入站消息)");
        }

        DataBridge rule = dataBridgeService.getById(trace.getBridgeRuleId());
        ArgumentAssert.notNull(rule, "桥接规则不存在或已被删除,无法重放 ruleId=" + trace.getBridgeRuleId());

        BridgeMessageEnvelope envelope = BridgeMessageEnvelope.builder()
                .tenantId(ContextUtil.getTenantIdStr())
                .productIdentification(trace.getProductIdentification())
                .deviceIdentification(trace.getDeviceIdentification())
                .actionType(trace.getActionType())
                .topic(trace.getTopic())
                .rawMessage(trace.getSourcePayloadSummary())
                .ts(System.currentTimeMillis())
                .build();

        // 新 traceId 取自当前 HTTP 请求 ContextUtil(由 HeaderThreadLocalInterceptor 设)
        String newTraceId = Optional.ofNullable(ContextUtil.getLogTraceId())
                .filter(StrUtil::isNotBlank)
                .orElseGet(SnowflakeIdUtil::nextId);
        log.info("[Replay] re-dispatching origTraceId={} newTraceId={} ruleId={}",
                traceId, newTraceId, rule.getId());

        // dispatch 热路径用缓存 VO,与 RocketMQ 消费链路语义一致
        DataBridgeCacheVO ruleVO = BeanPlusUtil.toBeanIgnoreError(rule, DataBridgeCacheVO.class);
        sinkDispatcher.dispatch(ruleVO, envelope);
        return newTraceId;
    }

    @Override
    public boolean removeBefore(LocalDateTime cutoff) {
        return superManager.remove(Wrappers.<BridgeExecutionTrace>lambdaQuery()
                .lt(BridgeExecutionTrace::getCreatedTime, cutoff));
    }

    private List<BridgeExecutionTraceStatsResultVO.TimelinePoint> buildTimeline(BridgeExecutionTracePageQuery query) {
        Map<String, Object> bounds = superManager.getTraceTimeBounds(query);
        LocalDateTime minTime = query != null && query.getStartTimeBegin() != null
                ? query.getStartTimeBegin()
                : toLocalDateTime(getMapValue(bounds, "minTime"));
        LocalDateTime maxTime = query != null && query.getStartTimeEnd() != null
                ? query.getStartTimeEnd()
                : toLocalDateTime(getMapValue(bounds, "maxTime"));
        if (minTime == null || maxTime == null) {
            return List.of();
        }
        TimeBucketGranularity granularity = resolveGranularity(minTime, maxTime);
        return superManager.getTraceTimeline(query, granularity.name());
    }

    private Object getMapValue(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String underlineKey = StrUtil.toUnderlineCase(key);
        if (row.containsKey(underlineKey)) {
            return row.get(underlineKey);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(underlineKey.toUpperCase());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private TimeBucketGranularity resolveGranularity(LocalDateTime minTime, LocalDateTime maxTime) {
        long hours = Math.max(Duration.between(minTime, maxTime).toHours(), 0L);
        if (hours <= 48) {
            return TimeBucketGranularity.HOUR;
        }
        long days = Math.max(Duration.between(minTime, maxTime).toDays(), 0L);
        return days <= 60 ? TimeBucketGranularity.DAY : TimeBucketGranularity.MONTH;
    }

    private enum TimeBucketGranularity {
        HOUR,
        DAY,
        MONTH
    }
}
