package com.mqttsnet.thinglinks.manager.bridge.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.manager.bridge.BridgeExecutionTraceManager;
import com.mqttsnet.thinglinks.mapper.bridge.BridgeExecutionTraceMapper;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionTraceStatsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 通用业务实现类
 * 桥接执行 trace
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BridgeExecutionTraceManagerImpl extends SuperManagerImpl<BridgeExecutionTraceMapper, BridgeExecutionTrace> implements BridgeExecutionTraceManager {

    private final BridgeExecutionTraceMapper traceMapper;

    @Override
    public List<BridgeExecutionTrace> getTraceList(BridgeExecutionTracePageQuery query) {
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        BridgeExecutionTracePageQuery safeQuery = safeQuery(query);
        wrap.lambda()
                .eq(StrUtil.isNotBlank(safeQuery.getTraceId()), BridgeExecutionTrace::getTraceId, safeQuery.getTraceId())
                .eq(safeQuery.getBridgeRuleId() != null, BridgeExecutionTrace::getBridgeRuleId, safeQuery.getBridgeRuleId())
                .eq(StrUtil.isNotBlank(safeQuery.getDirection()), BridgeExecutionTrace::getDirection, safeQuery.getDirection())
                .eq(StrUtil.isNotBlank(safeQuery.getTriggerSource()), BridgeExecutionTrace::getTriggerSource, safeQuery.getTriggerSource())
                .eq(StrUtil.isNotBlank(safeQuery.getProductIdentification()), BridgeExecutionTrace::getProductIdentification, safeQuery.getProductIdentification())
                .eq(StrUtil.isNotBlank(safeQuery.getDeviceIdentification()), BridgeExecutionTrace::getDeviceIdentification, safeQuery.getDeviceIdentification())
                .eq(StrUtil.isNotBlank(safeQuery.getActionType()), BridgeExecutionTrace::getActionType, safeQuery.getActionType())
                .eq(StrUtil.isNotBlank(safeQuery.getStatus()), BridgeExecutionTrace::getStatus, safeQuery.getStatus())
                .ge(safeQuery.getStartTimeBegin() != null, BridgeExecutionTrace::getStartTime, safeQuery.getStartTimeBegin())
                .le(safeQuery.getStartTimeEnd() != null, BridgeExecutionTrace::getStartTime, safeQuery.getStartTimeEnd())
                .orderByDesc(BridgeExecutionTrace::getStartTime);
        return traceMapper.selectList(wrap);
    }

    @Override
    public BridgeExecutionTraceStatsResultVO getTraceStats(BridgeExecutionTracePageQuery query) {
        Map<String, Object> stats = traceMapper.selectStats(safeQuery(query));
        return BridgeExecutionTraceStatsResultVO.builder()
                .total(toLong(stats, "total"))
                .success(toLong(stats, "success"))
                .failed(toLong(stats, "failed"))
                .partial(toLong(stats, "partial"))
                .deadLetter(toLong(stats, "deadLetter"))
                .avgLatencyMs(toLong(stats, "avgLatencyMs"))
                .build();
    }

    @Override
    public Map<String, Object> getTraceTimeBounds(BridgeExecutionTracePageQuery query) {
        return traceMapper.selectTimeBounds(safeQuery(query));
    }

    @Override
    public List<BridgeExecutionTraceStatsResultVO.TimelinePoint> getTraceTimeline(BridgeExecutionTracePageQuery query,
                                                                                  String granularity) {
        return Optional.ofNullable(traceMapper.selectTimelineStats(safeQuery(query), granularity))
                .orElse(List.of())
                .stream()
                .map(this::toTimelinePoint)
                .collect(Collectors.toList());
    }

    @Override
    public List<BridgeExecutionTraceStatsResultVO.TopRule> getTraceTopRules(BridgeExecutionTracePageQuery query,
                                                                            int limit) {
        return Optional.ofNullable(traceMapper.selectTopRules(safeQuery(query), limit))
                .orElse(List.of())
                .stream()
                .map(this::toTopRule)
                .collect(Collectors.toList());
    }

    @Override
    public BridgeExecutionTrace getByTraceId(String traceId) {
        if (StrUtil.isBlank(traceId)) {
            return null;
        }
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(BridgeExecutionTrace::getTraceId, traceId)
                .orderByDesc(BridgeExecutionTrace::getId)
                .last("LIMIT 1");
        return traceMapper.selectOne(wrap);
    }

    @Override
    public BridgeExecutionTrace getByTraceIdAndRuleId(String traceId, Long ruleId) {
        if (StrUtil.isBlank(traceId) || ruleId == null) {
            return null;
        }
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(BridgeExecutionTrace::getTraceId, traceId)
                .eq(BridgeExecutionTrace::getBridgeRuleId, ruleId);
        return traceMapper.selectOne(wrap);
    }

    private BridgeExecutionTracePageQuery safeQuery(BridgeExecutionTracePageQuery query) {
        return query == null ? new BridgeExecutionTracePageQuery() : query;
    }

    private BridgeExecutionTraceStatsResultVO.TimelinePoint toTimelinePoint(Map<String, Object> row) {
        return BridgeExecutionTraceStatsResultVO.TimelinePoint.builder()
                .timeLabel(String.valueOf(getValue(row, "timeLabel", "")))
                .success(toLong(row, "success"))
                .failed(toLong(row, "failed"))
                .partial(toLong(row, "partial"))
                .deadLetter(toLong(row, "deadLetter"))
                .avgLatencyMs(toLong(row, "avgLatencyMs"))
                .build();
    }

    private BridgeExecutionTraceStatsResultVO.TopRule toTopRule(Map<String, Object> row) {
        return BridgeExecutionTraceStatsResultVO.TopRule.builder()
                .bridgeRuleId(toLong(row, "bridgeRuleId"))
                .count(toLong(row, "triggerCount"))
                .success(toLong(row, "success"))
                .failed(toLong(row, "failed"))
                .deadLetter(toLong(row, "deadLetter"))
                .build();
    }

    private long toLong(Map<String, Object> row, String key) {
        Object value = getValue(row, key, 0L);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private Object getValue(Map<String, Object> row, String key, Object defaultValue) {
        if (row == null || row.isEmpty()) {
            return defaultValue;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String underscoreKey = CharSequenceUtil.toUnderlineCase(key);
        if (row.containsKey(underscoreKey)) {
            return row.get(underscoreKey);
        }
        String upperKey = key.toUpperCase();
        if (row.containsKey(upperKey)) {
            return row.get(upperKey);
        }
        String upperUnderscoreKey = underscoreKey.toUpperCase();
        return row.getOrDefault(upperUnderscoreKey, defaultValue);
    }
}
