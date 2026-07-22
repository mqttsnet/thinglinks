package com.mqttsnet.thinglinks.rule.linkage.execution.action;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionActionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.RuleActionExecutionExtendParamsDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleActionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.event.action.publisher.RuleConditionActionEventPublisher;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import com.mqttsnet.thinglinks.service.execution.service.ActionProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("场景联动动作执行处理")
class ActionProcessorServiceTest {

    @Mock
    private ExecutionLogEventPublisher executionLogEventPublisher;
    @Mock
    private RuleConditionActionEventPublisher ruleConditionActionEventPublisher;

    private ActionProcessorService service;

    @BeforeEach
    void setUp() {
        service = new ActionProcessorService();
        ReflectionTestUtils.setField(service, "executionLogEventPublisher", executionLogEventPublisher);
        ReflectionTestUtils.setField(service, "ruleConditionActionEventPublisher", ruleConditionActionEventPublisher);
    }

    @Test
    @DisplayName("告警动作链路抛错时记录失败动作日志，避免执行日志误判成功")
    void processActionsShouldPublishFailedActionLogWhenAlertActionThrows() {
        PolicyContext context = new PolicyContext();
        context.setRuleExecutionId(1001L);
        RuleConditionActionPolicyDTO action = RuleConditionActionPolicyDTO.builder()
                .ruleConditionId(10L)
                .actionType(RuleActionTypeEnum.ALERT.getValue())
                .actionContent("{\"version\":2,\"alarmIdentification\":\"alarm-1\"}")
                .remark("触发告警")
                .build();
        RuleConditionPolicyDTO condition = RuleConditionPolicyDTO.builder()
                .conditionActionPolicyDTOS(List.of(action))
                .build();
        doThrow(new IllegalStateException("alarm record not saved"))
                .when(ruleConditionActionEventPublisher).publishAlertActionEvent(context, action);

        service.processActions(context, condition);

        org.mockito.ArgumentCaptor<String> extendParamsCaptor = forClass(String.class);
        verify(executionLogEventPublisher).publishActionExecutionLog(
                eq(1001L),
                eq(RuleActionTypeEnum.ALERT.getValue()),
                eq(action.getActionContent()),
                eq(false),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                extendParamsCaptor.capture(),
                eq("Action executed failed")
        );
        RuleActionExecutionExtendParamsDTO extendParams =
                JSON.parseObject(extendParamsCaptor.getValue(), RuleActionExecutionExtendParamsDTO.class);
        assertEquals(10L, extendParams.getRuleConditionId());
        assertEquals(RuleActionTypeEnum.ALERT.getValue(), extendParams.getActionType());
        assertEquals("触发告警动作", extendParams.getActionName());
        assertFalse(extendParams.getResult());
    }
}
