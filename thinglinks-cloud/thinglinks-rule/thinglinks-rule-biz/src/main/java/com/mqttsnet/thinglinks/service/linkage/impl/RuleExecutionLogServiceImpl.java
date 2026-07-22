package com.mqttsnet.thinglinks.service.linkage.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleActionTypeEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleExecutionStatusEnum;
import com.mqttsnet.thinglinks.manager.linkage.RuleExecutionLogManager;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.RuleExecutionLogEvent;
import com.mqttsnet.thinglinks.service.linkage.RuleActionExecutionLogService;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionExecutionLogService;
import com.mqttsnet.thinglinks.service.linkage.RuleExecutionLogService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleActionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleActionExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStatsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStepResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务实现类
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleExecutionLogServiceImpl extends SuperServiceImpl<RuleExecutionLogManager, Long, RuleExecutionLog> implements RuleExecutionLogService {

    private static final String STEP_STATUS_SUCCESS = "00";
    private static final String STEP_STATUS_FAILED = "01";
    private static final String STEP_STATUS_SKIPPED = "02";
    private static final String STEP_TRIGGER = "TRIGGER";
    private static final String STEP_RULE_MATCH = "RULE_MATCH";
    private static final String STEP_ACTION = "ACTION";
    private static final int DELETE_BATCH_SIZE = 500;
    private static final DateTimeFormatter HOUR_BUCKET_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:00");
    private static final DateTimeFormatter DAY_BUCKET_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_BUCKET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RuleConditionExecutionLogService ruleConditionExecutionLogService;

    private final RuleActionExecutionLogService ruleActionExecutionLogService;

    private final RuleNotificationTemplateService ruleNotificationTemplateService;


    /**
     * 保存规则执行日志
     *
     * @param event
     */
    public void saveRuleExecutionLog(BaseExecutionLogEvent event) {
        RuleExecutionLog ruleLog = new RuleExecutionLog();
        ruleLog.setId(event.getRuleExecutionId() != null ? event.getRuleExecutionId() : Long.valueOf(SnowflakeIdUtil.nextId()));
        ruleLog.setRuleIdentification(((RuleExecutionLogEvent) event).getRuleIdentification());
        ruleLog.setRuleName(((RuleExecutionLogEvent) event).getRuleName());
        ruleLog.setStatus(RuleExecutionStatusEnum.COMPLETED.getValue());
        ruleLog.setStartTime(event.getStartTime());
        ruleLog.setEndTime(event.getEndTime());
        ruleLog.setExtendParams(event.getExtendParams());
        ruleLog.setRemark(event.getRemark());
        superManager.save(ruleLog);
        log.info("Rule execution log saved: {}", ruleLog);
    }

    @Override
    public RuleExecutionLogDetailsResultVO getRuleExecutionLogDetails(Long id) {
        // 获取规则执行日志
        RuleExecutionLog ruleExecutionLog = Optional.ofNullable(superManager.getById(id))
                .orElseThrow(() -> new BizException("Rule execution log not found"));

        // 将规则执行日志转换为VO
        RuleExecutionLogDetailsResultVO ruleExecutionLogDetailsResultVO = BeanPlusUtil.toBeanIgnoreError(ruleExecutionLog, RuleExecutionLogDetailsResultVO.class);

        // 获取规则条件及其对应的动作
        List<RuleConditionExecutionLogDetailsResultVO> conditions = getRuleConditions(ruleExecutionLogDetailsResultVO.getId());
        ruleExecutionLogDetailsResultVO.setConditionExecutionLogDetailsResultVOList(conditions);

        // 获取动作执行日志
        List<RuleActionExecutionLogDetailsResultVO> actionExecutionLogs = getRuleActions(ruleExecutionLogDetailsResultVO.getId());
        ruleExecutionLogDetailsResultVO.setActionExecutionLogDetailsResultVOList(actionExecutionLogs);
        List<RuleExecutionLogStepResultVO> steps = buildSteps(ruleExecutionLogDetailsResultVO, conditions, actionExecutionLogs);
        ruleExecutionLogDetailsResultVO.setSteps(steps);
        ruleExecutionLogDetailsResultVO.setStepCount(steps.size());
        ruleExecutionLogDetailsResultVO.setTotalLatencyMs(resolveTotalLatency(ruleExecutionLogDetailsResultVO, steps));
        ruleExecutionLogDetailsResultVO.setTriggerSource(resolveTriggerSource(conditions));
        ruleExecutionLogDetailsResultVO.setResultSummary(buildResultSummary(ruleExecutionLogDetailsResultVO, steps));

        return ruleExecutionLogDetailsResultVO;
    }

    @Override
    public RuleExecutionLogStatsResultVO getRuleExecutionLogStats(RuleExecutionLogPageQuery query) {
        RuleExecutionLogStatsResultVO result = superManager.getRuleExecutionLogStats(query);
        result.setTimeline(buildTimeline(query));
        return result;
    }

    @Override
    public Long clearRuleExecutionLogs(RuleExecutionLogPageQuery query) {
        RuleExecutionLogPageQuery safeQuery = requireClearScope(query);
        long cleared = 0L;
        while (true) {
            List<Long> batch = superManager.getRuleExecutionLogIds(safeQuery, DELETE_BATCH_SIZE);
            if (batch == null || batch.isEmpty()) {
                break;
            }
            ruleConditionExecutionLogService.removeByRuleExecutionIds(batch);
            ruleActionExecutionLogService.removeByRuleExecutionIds(batch);
            boolean removed = superManager.remove(Wrappers.<RuleExecutionLog>lambdaQuery()
                    .in(RuleExecutionLog::getId, batch));
            if (!removed) {
                throw BizException.wrap("清理执行日志失败，请稍后重试");
            }
            cleared += batch.size();
        }
        return cleared;
    }

    private RuleExecutionLogPageQuery requireClearScope(RuleExecutionLogPageQuery query) {
        if (query == null || (query.getId() == null && StrUtil.isBlank(query.getRuleIdentification()))) {
            throw BizException.wrap("清理执行日志请先限定规则或规则流水号");
        }
        return query;
    }

    private List<RuleExecutionLogStatsResultVO.TimelinePoint> buildTimeline(RuleExecutionLogPageQuery query) {
        Map<String, Object> bounds = superManager.getRuleExecutionLogTimeBounds(query);
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
        return superManager.getRuleExecutionLogTimeline(query, granularity.name());
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

    private LinkedHashMap<String, TimelineBucket> initBuckets(LocalDateTime minTime,
                                                              LocalDateTime maxTime,
                                                              TimeBucketGranularity granularity) {
        LinkedHashMap<String, TimelineBucket> bucketMap = new LinkedHashMap<>();
        LocalDateTime cursor = bucketStart(minTime, granularity);
        LocalDateTime end = bucketStart(maxTime, granularity);
        while (!cursor.isAfter(end)) {
            bucketMap.put(formatBucket(cursor, granularity), new TimelineBucket());
            cursor = nextBucket(cursor, granularity);
        }
        return bucketMap;
    }

    private void fillTimelineBucket(Map<String, TimelineBucket> bucketMap,
                                    TimeBucketGranularity granularity,
                                    RuleExecutionLog log) {
        if (log == null || log.getStartTime() == null) {
            return;
        }
        TimelineBucket bucket = bucketMap.get(formatBucket(bucketStart(log.getStartTime(), granularity), granularity));
        if (bucket == null) {
            return;
        }
        Integer status = log.getStatus();
        if (RuleExecutionStatusEnum.COMPLETED.getValue().equals(status)) {
            bucket.completed++;
        } else if (RuleExecutionStatusEnum.IN_PROGRESS.getValue().equals(status)) {
            bucket.executing++;
        } else if (RuleExecutionStatusEnum.NOT_EXECUTED.getValue().equals(status)) {
            bucket.notExecuted++;
        }
        long latency = resolveLatency(log.getExtendParams(), log.getStartTime(), log.getEndTime());
        if (latency >= 0) {
            bucket.latencySum += latency;
            bucket.latencyCount++;
        }
    }

    private LocalDateTime bucketStart(LocalDateTime time, TimeBucketGranularity granularity) {
        if (granularity == TimeBucketGranularity.MONTH) {
            return LocalDateTime.of(time.getYear(), time.getMonth(), 1, 0, 0);
        }
        if (granularity == TimeBucketGranularity.DAY) {
            return time.toLocalDate().atStartOfDay();
        }
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    private LocalDateTime nextBucket(LocalDateTime time, TimeBucketGranularity granularity) {
        if (granularity == TimeBucketGranularity.MONTH) {
            return time.plusMonths(1);
        }
        if (granularity == TimeBucketGranularity.DAY) {
            return time.plusDays(1);
        }
        return time.plusHours(1);
    }

    private String formatBucket(LocalDateTime time, TimeBucketGranularity granularity) {
        if (granularity == TimeBucketGranularity.MONTH) {
            return MONTH_BUCKET_FORMATTER.format(time);
        }
        if (granularity == TimeBucketGranularity.DAY) {
            return DAY_BUCKET_FORMATTER.format(time);
        }
        return HOUR_BUCKET_FORMATTER.format(time);
    }

    private List<RuleConditionExecutionLogDetailsResultVO> getRuleConditions(Long ruleExecutionId) {
        RuleConditionExecutionLogPageQuery conditionQuery = new RuleConditionExecutionLogPageQuery();
        conditionQuery.setRuleExecutionId(ruleExecutionId);
        return ruleConditionExecutionLogService.getRuleConditionExecutionLogResultVOList(conditionQuery).stream()
                .map(condition -> BeanPlusUtil.toBeanIgnoreError(condition, RuleConditionExecutionLogDetailsResultVO.class))
                .collect(Collectors.toList());
    }

    private List<RuleActionExecutionLogDetailsResultVO> getRuleActions(Long ruleExecutionId) {
        RuleActionExecutionLogPageQuery actionQuery = new RuleActionExecutionLogPageQuery();
        actionQuery.setRuleExecutionId(ruleExecutionId);
        return ruleActionExecutionLogService.getRuleActionExecutionLogResultVOList(actionQuery).stream()
                .map(action -> BeanPlusUtil.toBeanIgnoreError(action, RuleActionExecutionLogDetailsResultVO.class))
                .collect(Collectors.toList());
    }

    private List<RuleExecutionLogStepResultVO> buildSteps(RuleExecutionLogDetailsResultVO ruleLog,
                                                          List<RuleConditionExecutionLogDetailsResultVO> conditions,
                                                          List<RuleActionExecutionLogDetailsResultVO> actions) {
        List<RuleExecutionLogStepResultVO> steps = new ArrayList<>();
        steps.add(buildTriggerStep(ruleLog));
        Optional.ofNullable(conditions).orElse(List.of()).forEach(condition -> steps.add(buildConditionStep(condition)));
        Optional.ofNullable(actions).orElse(List.of()).forEach(action -> steps.add(buildActionStep(action)));
        steps.sort(Comparator
                .comparing(RuleExecutionLogStepResultVO::getStartedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(step -> step.getId() == null ? 0L : step.getId()));
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepNo(i + 1);
        }
        return steps;
    }

    private RuleExecutionLogStepResultVO buildTriggerStep(RuleExecutionLogDetailsResultVO ruleLog) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("ruleIdentification", ruleLog.getRuleIdentification());
        input.put("ruleName", ruleLog.getRuleName());
        input.put("executionId", ruleLog.getId());
        return RuleExecutionLogStepResultVO.builder()
                .id(ruleLog.getId())
                .stepType(STEP_TRIGGER)
                .stepName("规则触发")
                .status(resolveRuleStepStatus(ruleLog.getStatus()))
                .latencyMs(resolveLatency(ruleLog.getExtendParams(), ruleLog.getStartTime(), ruleLog.getEndTime()))
                .inputSummary(toJson(input))
                .outputSummary(StrUtil.blankToDefault(ruleLog.getRemark(), "规则执行完成"))
                .startedAt(ruleLog.getStartTime())
                .extendParams(normalizeSummary(ruleLog.getExtendParams()))
                .remark(ruleLog.getRemark())
                .build();
    }

    private RuleExecutionLogStepResultVO buildConditionStep(RuleConditionExecutionLogDetailsResultVO condition) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("conditionUuid", condition.getConditionUuid());
        output.put("evaluationResult", Boolean.TRUE.equals(condition.getEvaluationResult()));
        output.put("remark", condition.getRemark());
        return RuleExecutionLogStepResultVO.builder()
                .id(condition.getId())
                .stepType(STEP_RULE_MATCH)
                .stepName(resolveConditionName(condition.getConditionType()))
                .status(Boolean.TRUE.equals(condition.getEvaluationResult()) ? STEP_STATUS_SUCCESS : STEP_STATUS_SKIPPED)
                .latencyMs(resolveLatency(condition.getExtendParams(), condition.getStartTime(), condition.getEndTime()))
                .inputSummary(normalizeSummary(condition.getExtendParams()))
                .outputSummary(toJson(output))
                .errorMsg(Boolean.TRUE.equals(condition.getEvaluationResult()) ? null : condition.getRemark())
                .startedAt(condition.getStartTime())
                .extendParams(normalizeSummary(condition.getExtendParams()))
                .remark(condition.getRemark())
                .build();
    }

    private RuleExecutionLogStepResultVO buildActionStep(RuleActionExecutionLogDetailsResultVO action) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("actionType", action.getActionType());
        output.put("actionName", resolveActionName(action.getActionType()));
        output.put("result", Boolean.TRUE.equals(action.getResult()));
        output.put("remark", action.getRemark());
        if (RuleActionTypeEnum.ALERT.getValue().equals(action.getActionType())) {
            output.put("alarm", parseAlarmActionSummary(action.getActionContent()));
        }
        return RuleExecutionLogStepResultVO.builder()
                .id(action.getId())
                .stepType(STEP_ACTION)
                .stepName(resolveActionName(action.getActionType()))
                .status(Boolean.TRUE.equals(action.getResult()) ? STEP_STATUS_SUCCESS : STEP_STATUS_FAILED)
                .latencyMs(resolveLatency(action.getExtendParams(), action.getStartTime(), action.getEndTime()))
                .inputSummary(normalizeSummary(action.getActionContent()))
                .outputSummary(toJson(output))
                .errorMsg(Boolean.TRUE.equals(action.getResult()) ? null : action.getRemark())
                .startedAt(action.getStartTime())
                .extendParams(normalizeSummary(action.getExtendParams()))
                .remark(action.getRemark())
                .build();
    }

    private Map<String, Object> parseAlarmActionSummary(String actionContent) {
        Map<String, Object> summary = new LinkedHashMap<>();
        try {
            RuleAlarmActionConfigDTO config = ruleNotificationTemplateService.parseActionConfig(actionContent);
            summary.put("alarmIdentification", config.getAlarmIdentification());
            summary.put("recipientCount", config.getRecipients() == null ? 0 : config.getRecipients().size());
            summary.put("channelTemplateCount", config.getChannelTemplates() == null ? 0 : config.getChannelTemplates().size());
            summary.put("version", config.getVersion());
        } catch (Exception e) {
            summary.put("parseStatus", "FAILED");
            summary.put("message", "告警动作内容无法解析");
            summary.put("raw", normalizeSummary(actionContent));
        }
        return summary;
    }

    private String resolveRuleStepStatus(Integer status) {
        if (RuleExecutionStatusEnum.COMPLETED.getValue().equals(status)) {
            return STEP_STATUS_SUCCESS;
        }
        if (RuleExecutionStatusEnum.IN_PROGRESS.getValue().equals(status)) {
            return STEP_STATUS_SKIPPED;
        }
        return STEP_STATUS_FAILED;
    }

    private String resolveConditionName(Integer conditionType) {
        ConditionTypeEnum conditionTypeEnum = ConditionTypeEnum.fromValue(conditionType);
        return conditionTypeEnum == null ? "规则匹配" : conditionTypeEnum.getDesc();
    }

    private String resolveActionName(Integer actionType) {
        return RuleActionTypeEnum.fromValue(actionType)
                .map(RuleActionTypeEnum::getDesc)
                .orElse("执行动作");
    }

    private String resolveTriggerSource(List<RuleConditionExecutionLogDetailsResultVO> conditions) {
        return Optional.ofNullable(conditions).orElse(List.of()).stream()
                .map(RuleConditionExecutionLogDetailsResultVO::getConditionType)
                .findFirst()
                .map(this::resolveConditionName)
                .orElse("场景联动");
    }

    private String buildResultSummary(RuleExecutionLogDetailsResultVO ruleLog,
                                      List<RuleExecutionLogStepResultVO> steps) {
        long failed = steps.stream().filter(step -> STEP_STATUS_FAILED.equals(step.getStatus())).count();
        long skipped = steps.stream().filter(step -> STEP_STATUS_SKIPPED.equals(step.getStatus())).count();
        if (failed > 0) {
            return "执行完成，存在失败步骤 " + failed + " 个";
        }
        if (skipped > 0) {
            return "执行完成，存在未命中或跳过步骤 " + skipped + " 个";
        }
        return StrUtil.blankToDefault(ruleLog.getRemark(), "执行成功");
    }

    private Long resolveTotalLatency(RuleExecutionLogDetailsResultVO ruleLog,
                                     List<RuleExecutionLogStepResultVO> steps) {
        long ruleLatency = resolveLatency(ruleLog.getExtendParams(), ruleLog.getStartTime(), ruleLog.getEndTime());
        long spanLatency = resolveStepSpanLatency(steps);
        if (ruleLatency >= 0 || spanLatency >= 0) {
            return Math.max(ruleLatency, spanLatency);
        }
        return Optional.ofNullable(steps).orElse(List.of()).stream()
                .map(RuleExecutionLogStepResultVO::getLatencyMs)
                .filter(item -> item != null && item >= 0)
                .mapToLong(Long::longValue)
                .sum();
    }

    private long resolveStepSpanLatency(List<RuleExecutionLogStepResultVO> steps) {
        List<RuleExecutionLogStepResultVO> validSteps = Optional.ofNullable(steps).orElse(List.of()).stream()
                .filter(item -> item.getStartedAt() != null && item.getLatencyMs() != null && item.getLatencyMs() >= 0)
                .collect(Collectors.toList());
        if (validSteps.isEmpty()) {
            return -1L;
        }
        LocalDateTime minStartTime = validSteps.stream()
                .map(RuleExecutionLogStepResultVO::getStartedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime maxEndTime = validSteps.stream()
                .map(item -> item.getStartedAt().plus(Duration.ofMillis(item.getLatencyMs())))
                .max(LocalDateTime::compareTo)
                .orElse(null);
        return millisBetween(minStartTime, maxEndTime);
    }

    private long millisBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return -1L;
        }
        long millis = Duration.between(startTime, endTime).toMillis();
        return Math.max(millis, 0L);
    }

    private long resolveLatency(String extendParams, LocalDateTime startTime, LocalDateTime endTime) {
        Long latencyMs = resolveLatencyFromExtendParams(extendParams);
        if (latencyMs != null && latencyMs >= 0) {
            return latencyMs;
        }
        return millisBetween(startTime, endTime);
    }

    private Long resolveLatencyFromExtendParams(String extendParams) {
        if (StrUtil.isBlank(extendParams)) {
            return null;
        }
        try {
            return JSON.parseObject(extendParams).getLong("latencyMs");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeSummary(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            Object json = JSON.parse(raw);
            return JSON.toJSONString(json);
        } catch (Exception ignored) {
            return raw;
        }
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return JSON.toJSONString(data);
        } catch (Exception e) {
            return String.valueOf(data);
        }
    }

    private enum TimeBucketGranularity {
        HOUR,
        DAY,
        MONTH
    }

    private static class TimelineBucket {
        private long completed;
        private long executing;
        private long notExecuted;
        private long latencySum;
        private long latencyCount;

        private long avgLatency() {
            return latencyCount == 0 ? 0L : Math.round((double) latencySum / latencyCount);
        }
    }

}
