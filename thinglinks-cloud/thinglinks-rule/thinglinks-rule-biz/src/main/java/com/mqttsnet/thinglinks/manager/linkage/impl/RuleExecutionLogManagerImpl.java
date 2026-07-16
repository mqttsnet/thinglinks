package com.mqttsnet.thinglinks.manager.linkage.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.manager.linkage.RuleExecutionLogManager;
import com.mqttsnet.thinglinks.mapper.linkage.RuleExecutionLogMapper;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStatsResultVO;
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
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleExecutionLogManagerImpl extends SuperManagerImpl<RuleExecutionLogMapper, RuleExecutionLog> implements RuleExecutionLogManager {

    private final RuleExecutionLogMapper ruleExecutionLogMapper;

    @Override
    public List<RuleExecutionLog> getRuleExecutionLogList(RuleExecutionLogPageQuery query) {
        QueryWrap<RuleExecutionLog> queryWrap = new QueryWrap<>();
        RuleExecutionLogPageQuery safeQuery = query == null ? new RuleExecutionLogPageQuery() : query;
        queryWrap.lambda()
                .eq(safeQuery.getId() != null, RuleExecutionLog::getId, safeQuery.getId())
                .like(CharSequenceUtil.isNotBlank(safeQuery.getRuleIdentification()),
                        RuleExecutionLog::getRuleIdentification, safeQuery.getRuleIdentification())
                .like(CharSequenceUtil.isNotBlank(safeQuery.getRuleName()),
                        RuleExecutionLog::getRuleName, safeQuery.getRuleName())
                .eq(safeQuery.getStatus() != null, RuleExecutionLog::getStatus, safeQuery.getStatus())
                .ge(safeQuery.getStartTimeBegin() != null,
                        RuleExecutionLog::getStartTime, safeQuery.getStartTimeBegin())
                .le(safeQuery.getStartTimeEnd() != null,
                        RuleExecutionLog::getStartTime, safeQuery.getStartTimeEnd())
                .like(CharSequenceUtil.isNotBlank(safeQuery.getRemark()),
                        RuleExecutionLog::getRemark, safeQuery.getRemark())
                .like(CharSequenceUtil.isNotBlank(safeQuery.getExtendParams()),
                        RuleExecutionLog::getExtendParams, safeQuery.getExtendParams())
                .orderByDesc(RuleExecutionLog::getStartTime);
        return ruleExecutionLogMapper.selectList(queryWrap);
    }

    @Override
    public RuleExecutionLogStatsResultVO getRuleExecutionLogStats(RuleExecutionLogPageQuery query) {
        Map<String, Object> stats = ruleExecutionLogMapper.selectStats(safeQuery(query));
        return RuleExecutionLogStatsResultVO.builder()
                .total(toLong(stats, "total"))
                .completed(toLong(stats, "completed"))
                .executing(toLong(stats, "executing"))
                .notExecuted(toLong(stats, "notExecuted"))
                .avgLatencyMs(toLong(stats, "avgLatencyMs"))
                .build();
    }

    @Override
    public Map<String, Object> getRuleExecutionLogTimeBounds(RuleExecutionLogPageQuery query) {
        return ruleExecutionLogMapper.selectTimeBounds(safeQuery(query));
    }

    @Override
    public List<RuleExecutionLogStatsResultVO.TimelinePoint> getRuleExecutionLogTimeline(RuleExecutionLogPageQuery query,
                                                                                         String granularity) {
        return Optional.ofNullable(ruleExecutionLogMapper.selectTimelineStats(safeQuery(query), granularity))
                .orElse(List.of())
                .stream()
                .map(this::toTimelinePoint)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getRuleExecutionLogIds(RuleExecutionLogPageQuery query, int limit) {
        return ruleExecutionLogMapper.selectIdsForClear(safeQuery(query), limit);
    }

    private RuleExecutionLogPageQuery safeQuery(RuleExecutionLogPageQuery query) {
        return query == null ? new RuleExecutionLogPageQuery() : query;
    }

    private RuleExecutionLogStatsResultVO.TimelinePoint toTimelinePoint(Map<String, Object> row) {
        return RuleExecutionLogStatsResultVO.TimelinePoint.builder()
                .timeLabel(String.valueOf(getValue(row, "timeLabel", "")))
                .completed(toLong(row, "completed"))
                .executing(toLong(row, "executing"))
                .notExecuted(toLong(row, "notExecuted"))
                .avgLatencyMs(toLong(row, "avgLatencyMs"))
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
