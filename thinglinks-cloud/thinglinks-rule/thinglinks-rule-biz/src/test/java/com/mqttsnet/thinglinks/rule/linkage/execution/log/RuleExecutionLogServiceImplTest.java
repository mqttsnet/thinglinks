package com.mqttsnet.thinglinks.rule.linkage.execution.log;

import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmActionConfigDTO;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleActionTypeEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleExecutionStatusEnum;
import com.mqttsnet.thinglinks.manager.linkage.RuleExecutionLogManager;
import com.mqttsnet.thinglinks.service.alarm.RuleNotificationTemplateService;
import com.mqttsnet.thinglinks.service.linkage.RuleActionExecutionLogService;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionExecutionLogService;
import com.mqttsnet.thinglinks.service.linkage.impl.RuleExecutionLogServiceImpl;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleActionExecutionLogResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionExecutionLogResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStatsResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("场景联动执行日志服务")
class RuleExecutionLogServiceImplTest {

    @Mock
    private RuleExecutionLogManager ruleExecutionLogManager;
    @Mock
    private RuleConditionExecutionLogService ruleConditionExecutionLogService;
    @Mock
    private RuleActionExecutionLogService ruleActionExecutionLogService;
    @Mock
    private RuleNotificationTemplateService ruleNotificationTemplateService;

    private RuleExecutionLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RuleExecutionLogServiceImpl(ruleConditionExecutionLogService,
                ruleActionExecutionLogService, ruleNotificationTemplateService);
        ReflectionTestUtils.setField(service, "superManager", ruleExecutionLogManager);
    }

    @Test
    @DisplayName("详情按触发、条件匹配、动作执行生成新的步骤链路，并用步骤跨度修正规则主表耗时")
    void getRuleExecutionLogDetailsShouldBuildStepTimelineAndResolveReliableLatency() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 5, 21, 0);
        RuleExecutionLog ruleLog = RuleExecutionLog.builder()
                .ruleIdentification("rule-1")
                .ruleName("测试属性触发")
                .status(RuleExecutionStatusEnum.COMPLETED.getValue())
                .startTime(startTime)
                .endTime(plusMillis(startTime, 100))
                .remark("Rule execution completed")
                .build();
        ruleLog.setId(1001L);
        when(ruleExecutionLogManager.getById(1001L)).thenReturn(ruleLog);
        when(ruleConditionExecutionLogService.getRuleConditionExecutionLogResultVOList(any()))
                .thenReturn(List.of(RuleConditionExecutionLogResultVO.builder()
                        .id(2001L)
                        .ruleExecutionId(1001L)
                        .conditionUuid("condition-a")
                        .conditionType(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue())
                        .evaluationResult(true)
                        .startTime(plusMillis(startTime, 10))
                        .endTime(plusMillis(startTime, 60))
                        .extendParams("{\"property\":\"temperature\"}")
                        .remark("命中属性条件")
                        .build()));
        when(ruleActionExecutionLogService.getRuleActionExecutionLogResultVOList(any()))
                .thenReturn(List.of(RuleActionExecutionLogResultVO.builder()
                        .id(3001L)
                        .ruleExecutionId(1001L)
                        .actionType(RuleActionTypeEnum.ALERT.getValue())
                        .actionContent("{\"version\":2,\"alarmIdentification\":\"alarm-1\"}")
                        .result(true)
                        .startTime(plusMillis(startTime, 80))
                        .endTime(plusMillis(startTime, 380))
                        .extendParams("{\"channel\":\"dingTalk\"}")
                        .remark("通知已发送")
                        .build()));
        when(ruleNotificationTemplateService.parseActionConfig(any()))
                .thenReturn(RuleAlarmActionConfigDTO.builder()
                        .version(2)
                        .alarmIdentification("alarm-1")
                        .build());

        RuleExecutionLogDetailsResultVO details = service.getRuleExecutionLogDetails(1001L);

        assertEquals(3, details.getStepCount());
        assertEquals(380L, details.getTotalLatencyMs());
        assertEquals("设备属性触发", details.getTriggerSource());
        assertEquals("规则触发", details.getSteps().get(0).getStepName());
        assertEquals("设备属性触发", details.getSteps().get(1).getStepName());
        assertEquals("触发告警动作", details.getSteps().get(2).getStepName());
        assertTrue(details.getSteps().get(2).getOutputSummary().contains("alarm-1"));
    }

    @Test
    @DisplayName("详情遇到旧告警动作内容解析失败时保留原始内容摘要，不让详情接口报错")
    void getRuleExecutionLogDetailsShouldTolerateBrokenAlarmActionContent() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 5, 21, 10);
        RuleExecutionLog ruleLog = RuleExecutionLog.builder()
                .ruleIdentification("rule-legacy")
                .ruleName("旧动作内容")
                .status(RuleExecutionStatusEnum.COMPLETED.getValue())
                .startTime(startTime)
                .endTime(plusMillis(startTime, 30))
                .build();
        ruleLog.setId(1002L);
        when(ruleExecutionLogManager.getById(1002L)).thenReturn(ruleLog);
        when(ruleConditionExecutionLogService.getRuleConditionExecutionLogResultVOList(any()))
                .thenReturn(List.of());
        when(ruleActionExecutionLogService.getRuleActionExecutionLogResultVOList(any()))
                .thenReturn(List.of(RuleActionExecutionLogResultVO.builder()
                        .id(3002L)
                        .ruleExecutionId(1002L)
                        .actionType(RuleActionTypeEnum.ALERT.getValue())
                        .actionContent("legacy-content")
                        .result(false)
                        .startTime(plusMillis(startTime, 10))
                        .endTime(plusMillis(startTime, 20))
                        .remark("发送失败")
                        .build()));
        when(ruleNotificationTemplateService.parseActionConfig(any()))
                .thenThrow(new IllegalArgumentException("broken"));

        RuleExecutionLogDetailsResultVO details = service.getRuleExecutionLogDetails(1002L);

        assertEquals(2, details.getStepCount());
        assertEquals("01", details.getSteps().get(1).getStatus());
        assertTrue(details.getSteps().get(1).getOutputSummary().contains("parseStatus"));
        assertTrue(details.getResultSummary().contains("失败步骤"));
    }

    @Test
    @DisplayName("统计按当前查询条件走数据库聚合，不拉取分页当前页或全量对象列表")
    void getRuleExecutionLogStatsShouldUseAggregateQuery() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 5, 22, 0);
        when(ruleExecutionLogManager.getRuleExecutionLogStats(any(RuleExecutionLogPageQuery.class)))
                .thenReturn(RuleExecutionLogStatsResultVO.builder()
                        .total(3L)
                        .completed(1L)
                        .executing(1L)
                        .notExecuted(1L)
                        .avgLatencyMs(100L)
                        .build());
        when(ruleExecutionLogManager.getRuleExecutionLogTimeBounds(any(RuleExecutionLogPageQuery.class)))
                .thenReturn(Map.of("minTime", startTime, "maxTime", startTime.plusSeconds(2)));
        when(ruleExecutionLogManager.getRuleExecutionLogTimeline(any(RuleExecutionLogPageQuery.class), eq("HOUR")))
                .thenReturn(List.of(RuleExecutionLogStatsResultVO.TimelinePoint.builder()
                        .timeLabel("07-05 22:00")
                        .completed(1L)
                        .executing(1L)
                        .notExecuted(1L)
                        .avgLatencyMs(100L)
                        .build()));

        RuleExecutionLogStatsResultVO stats = service.getRuleExecutionLogStats(new RuleExecutionLogPageQuery());

        assertEquals(3L, stats.getTotal());
        assertEquals(1L, stats.getCompleted());
        assertEquals(1L, stats.getExecuting());
        assertEquals(1L, stats.getNotExecuted());
        assertEquals(100L, stats.getAvgLatencyMs());
        assertEquals(1, stats.getTimeline().size());
        assertEquals(1L, stats.getTimeline().get(0).getCompleted());
        assertEquals(1L, stats.getTimeline().get(0).getExecuting());
        assertEquals(1L, stats.getTimeline().get(0).getNotExecuted());
        verify(ruleExecutionLogManager, never()).getRuleExecutionLogList(any(RuleExecutionLogPageQuery.class));
    }

    @Test
    @DisplayName("详情和统计优先使用扩展参数中的毫秒耗时，避免数据库秒级时间精度导致显示 0ms")
    void getRuleExecutionLogDetailsShouldPreferLatencyFromExtendParams() {
        LocalDateTime sameSecond = LocalDateTime.of(2026, 7, 5, 22, 25, 37);
        RuleExecutionLog ruleLog = RuleExecutionLog.builder()
                .ruleIdentification("rule-latency")
                .ruleName("测试属性触发")
                .status(RuleExecutionStatusEnum.COMPLETED.getValue())
                .startTime(sameSecond)
                .endTime(sameSecond)
                .extendParams("{\"latencyMs\":32}")
                .build();
        ruleLog.setId(1003L);
        when(ruleExecutionLogManager.getById(1003L)).thenReturn(ruleLog);
        when(ruleConditionExecutionLogService.getRuleConditionExecutionLogResultVOList(any()))
                .thenReturn(List.of(RuleConditionExecutionLogResultVO.builder()
                        .id(2003L)
                        .ruleExecutionId(1003L)
                        .conditionUuid("condition-latency")
                        .conditionType(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue())
                        .evaluationResult(true)
                        .startTime(sameSecond)
                        .endTime(sameSecond)
                        .extendParams("{\"latencyMs\":12,\"detail\":\"battery > 1\"}")
                        .remark("命中属性条件")
                        .build()));
        when(ruleActionExecutionLogService.getRuleActionExecutionLogResultVOList(any()))
                .thenReturn(List.of());
        when(ruleExecutionLogManager.getRuleExecutionLogStats(any(RuleExecutionLogPageQuery.class)))
                .thenReturn(RuleExecutionLogStatsResultVO.builder()
                        .total(1L)
                        .completed(1L)
                        .executing(0L)
                        .notExecuted(0L)
                        .avgLatencyMs(32L)
                        .build());
        when(ruleExecutionLogManager.getRuleExecutionLogTimeBounds(any(RuleExecutionLogPageQuery.class)))
                .thenReturn(Map.of("minTime", sameSecond, "maxTime", sameSecond));
        when(ruleExecutionLogManager.getRuleExecutionLogTimeline(any(RuleExecutionLogPageQuery.class), eq("HOUR")))
                .thenReturn(List.of(RuleExecutionLogStatsResultVO.TimelinePoint.builder()
                        .timeLabel("07-05 22:00")
                        .completed(1L)
                        .executing(0L)
                        .notExecuted(0L)
                        .avgLatencyMs(32L)
                        .build()));

        RuleExecutionLogDetailsResultVO details = service.getRuleExecutionLogDetails(1003L);
        RuleExecutionLogStatsResultVO stats = service.getRuleExecutionLogStats(new RuleExecutionLogPageQuery());

        assertEquals(32L, details.getTotalLatencyMs());
        assertEquals(32L, details.getSteps().get(0).getLatencyMs());
        assertEquals(12L, details.getSteps().get(1).getLatencyMs());
        assertEquals(32L, stats.getAvgLatencyMs());
    }

    @Test
    @DisplayName("清理执行日志必须带规则标识或规则流水号，避免误删全量日志")
    void clearRuleExecutionLogsShouldRequireScopedQuery() {
        assertThrows(BizException.class, () -> service.clearRuleExecutionLogs(new RuleExecutionLogPageQuery()));
    }

    @Test
    @DisplayName("清理执行日志按限定条件分批获取主日志流水号，再删除条件、动作和主日志")
    void clearRuleExecutionLogsShouldRemoveChildrenBeforeRuleLogs() {
        RuleExecutionLogPageQuery query = new RuleExecutionLogPageQuery();
        query.setRuleIdentification("rule-clear");
        when(ruleExecutionLogManager.getRuleExecutionLogIds(query, 500))
                .thenReturn(List.of(11L, 12L), List.of());
        when(ruleExecutionLogManager.remove(any())).thenReturn(true);

        Long cleared = service.clearRuleExecutionLogs(query);

        assertEquals(2L, cleared);
        verify(ruleConditionExecutionLogService).removeByRuleExecutionIds(List.of(11L, 12L));
        verify(ruleActionExecutionLogService).removeByRuleExecutionIds(List.of(11L, 12L));
        verify(ruleExecutionLogManager).remove(any());
        verify(ruleExecutionLogManager, never()).getRuleExecutionLogList(query);
    }

    private RuleExecutionLog log(Long id, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        RuleExecutionLog log = RuleExecutionLog.builder()
                .ruleIdentification("rule-" + id)
                .ruleName("规则-" + id)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        log.setId(id);
        return log;
    }

    private LocalDateTime plusMillis(LocalDateTime time, long millis) {
        return time.plusNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }
}
