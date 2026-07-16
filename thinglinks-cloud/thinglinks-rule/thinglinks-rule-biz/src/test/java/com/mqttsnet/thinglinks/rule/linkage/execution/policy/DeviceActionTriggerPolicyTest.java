package com.mqttsnet.thinglinks.rule.linkage.execution.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.condition.enumeration.OperatorEnum;
import com.mqttsnet.basic.condition.service.ConditionEvaluatorService;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.dto.linkage.AntiShakeSchemePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionConditionDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionConditionGroupDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionLeftParamDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionRightParamsDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.OperatorDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.AntiShakeStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import com.mqttsnet.thinglinks.service.execution.policy.DeviceActionTriggerPolicy;
import com.mqttsnet.thinglinks.service.execution.service.ActionProcessorService;
import com.mqttsnet.thinglinks.service.execution.trigger.ActionCooldownService;
import com.mqttsnet.thinglinks.service.execution.trigger.AntiShakeCounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("场景联动设备动作触发策略")
class DeviceActionTriggerPolicyTest {

    private static final String PRODUCT_ID = "product-1";
    private static final String DEVICE_ID = "device-1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LinkCacheDataHelper linkCacheDataHelper;
    @Mock
    private ExecutionLogEventPublisher executionLogEventPublisher;
    @Mock
    private ConditionEvaluatorService conditionEvaluatorService;
    @Mock
    private ActionProcessorService actionProcessorService;
    @Mock
    private ActionCooldownService actionCooldownService;
    @Mock
    private AntiShakeCounterService antiShakeCounterService;

    private DeviceActionTriggerPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DeviceActionTriggerPolicy(linkCacheDataHelper);
        ReflectionTestUtils.setField(policy, "executionLogEventPublisher", executionLogEventPublisher);
        ReflectionTestUtils.setField(policy, "conditionEvaluatorService", conditionEvaluatorService);
        ReflectionTestUtils.setField(policy, "actionProcessorService", actionProcessorService);
        ReflectionTestUtils.setField(policy, "actionCooldownService", actionCooldownService);
        ReflectionTestUtils.setField(policy, "antiShakeCounterService", antiShakeCounterService);
    }

    @Test
    @DisplayName("产品级 all 设备规则可以用离线虚拟动作组匹配 MQS 断开事件")
    void applyPolicyShouldMatchAllDeviceOfflineVirtualGroup() throws Exception {
        PolicyContext context = policyContext(DeviceActionTypeEnum.DISCONNECT.getValue());
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                false,
                actionCondition(BizConstant.ALL, "设备离线"));
        when(conditionEvaluatorService.compare(eq(DeviceActionTypeEnum.DISCONNECT.getValue()),
                eq(OperatorEnum.EQ), anyList()))
                .thenAnswer(invocation -> {
                    List<?> rightValues = invocation.getArgument(2);
                    assertTrue(rightValues.contains(DeviceActionTypeEnum.DISCONNECT.getValue()));
                    assertTrue(rightValues.contains(DeviceActionTypeEnum.CLOSE.getValue()));
                    assertTrue(rightValues.contains(DeviceActionTypeEnum.KICKED.getValue()));
                    assertTrue(rightValues.contains(DeviceActionTypeEnum.HEART_TIMEOUT.getValue()));
                    assertTrue(rightValues.contains(DeviceActionTypeEnum.ERROR.getValue()));
                    return true;
                });
        when(actionCooldownService.tryAcquire(context, conditionPolicy)).thenReturn(true);

        policy.applyPolicy(context, conditionPolicy);

        verify(actionProcessorService).processActions(context, conditionPolicy);
    }

    @Test
    @DisplayName("动作值大小写和中文别名统一归一化后再比较")
    void applyPolicyShouldNormalizeCaseAndChineseAliasBeforeCompare() throws Exception {
        PolicyContext context = policyContext("disconnect");
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                false,
                actionCondition(BizConstant.ALL, "客户端主动断开"));
        when(conditionEvaluatorService.compare(eq(DeviceActionTypeEnum.DISCONNECT.getValue()),
                eq(OperatorEnum.EQ), eq(Collections.singletonList(DeviceActionTypeEnum.DISCONNECT.getValue()))))
                .thenReturn(true);
        when(actionCooldownService.tryAcquire(context, conditionPolicy)).thenReturn(true);

        policy.applyPolicy(context, conditionPolicy);

        verify(actionProcessorService).processActions(context, conditionPolicy);
    }

    @Test
    @DisplayName("防抖开启时按事件设备维度累计，达标后取首/末动作再评估")
    void applyPolicyShouldEvaluateSelectedActionAfterAntiShakeReached() throws Exception {
        PolicyContext context = policyContext(DeviceActionTypeEnum.CONNECT.getValue());
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                true,
                actionCondition(BizConstant.ALL, DeviceActionTypeEnum.DISCONNECT.getValue()));
        when(antiShakeCounterService.countAndSelect(any(), any(), any(), eq(DeviceActionTypeEnum.CONNECT.getValue())))
                .thenReturn(Optional.of("客户端主动断开"));
        when(conditionEvaluatorService.compare(eq(DeviceActionTypeEnum.DISCONNECT.getValue()),
                eq(OperatorEnum.EQ), eq(Collections.singletonList(DeviceActionTypeEnum.DISCONNECT.getValue()))))
                .thenReturn(true);
        when(actionCooldownService.tryAcquire(context, conditionPolicy)).thenReturn(true);

        policy.applyPolicy(context, conditionPolicy);

        verify(antiShakeCounterService).countAndSelect(any(), any(), any(), eq(DeviceActionTypeEnum.CONNECT.getValue()));
        verify(actionProcessorService).processActions(context, conditionPolicy);
    }

    @Test
    @DisplayName("条件满足但动作冷却未取得执行权时不重复执行动作")
    void applyPolicyShouldSkipActionsWhenCooldownSuppresses() throws Exception {
        PolicyContext context = policyContext(DeviceActionTypeEnum.DISCONNECT.getValue());
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                false,
                actionCondition(BizConstant.ALL, DeviceActionTypeEnum.DISCONNECT.getValue()));
        when(conditionEvaluatorService.compare(eq(DeviceActionTypeEnum.DISCONNECT.getValue()),
                eq(OperatorEnum.EQ), eq(Collections.singletonList(DeviceActionTypeEnum.DISCONNECT.getValue()))))
                .thenReturn(true);
        when(actionCooldownService.tryAcquire(context, conditionPolicy)).thenReturn(false);

        policy.applyPolicy(context, conditionPolicy);

        verify(actionProcessorService, never()).processActions(any(), any());
    }

    @Test
    @DisplayName("事件路径只评估设备动作触发条件，禁用条件不执行动作")
    void applyPolicyShouldIgnoreDisabledCondition() throws Exception {
        PolicyContext context = policyContext(DeviceActionTypeEnum.DISCONNECT.getValue());
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                false,
                actionCondition(BizConstant.ALL, DeviceActionTypeEnum.DISCONNECT.getValue()));
        conditionPolicy.setStatus(ConditionStatusEnum.DISABLED.getValue());

        policy.applyPolicy(context, conditionPolicy);

        verify(conditionEvaluatorService, never()).compare(any(), any(), anyList());
        verify(actionProcessorService, never()).processActions(any(), any());
    }

    @Test
    @DisplayName("产品级 all 规则遇到没有动作值的事件时不回退空设备缓存")
    void applyPolicyShouldNotFallbackCacheForAllDeviceWhenEventActionMissing() throws Exception {
        PolicyContext context = policyContext(" ");
        RuleConditionPolicyDTO conditionPolicy = conditionPolicy(
                false,
                actionCondition(BizConstant.ALL, DeviceActionTypeEnum.DISCONNECT.getValue()));

        policy.applyPolicy(context, conditionPolicy);

        verify(linkCacheDataHelper, never()).getDeviceActionCacheVO(anyString(), anyString(), eq(false));
        verify(actionProcessorService, never()).processActions(any(), any());
    }

    @Test
    @DisplayName("防抖配置非法时返回空配置，策略执行按非防抖兜底")
    void performAntiShakeLogicShouldReturnEmptyForInvalidJson() {
        assertFalse(policy.performAntiShakeLogic("{bad-json").isPresent());
    }

    private PolicyContext policyContext(String actionType) {
        PolicyContext context = new PolicyContext();
        context.setTenantId("tenant-a");
        context.setRuleIdentification("rule-action-offline");
        context.setRuleName("设备离线告警");
        context.setTriggerConditionType(ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue());
        context.setRulePolicyDTO(RulePolicyDTO.builder()
                .ruleIdentification("rule-action-offline")
                .ruleName("设备离线告警")
                .build());
        context.setTriggerEvent(TriggerEventDTO.builder()
                .productIdentification(PRODUCT_ID)
                .deviceIdentification(DEVICE_ID)
                .actionType(actionType)
                .eventUtc(1000L)
                .build());
        return context;
    }

    private RuleConditionPolicyDTO conditionPolicy(boolean antiShake,
                                                   DeviceActionConditionDTO condition) throws Exception {
        DeviceActionConditionGroupDTO group = DeviceActionConditionGroupDTO.builder()
                .type("GROUP")
                .uuid("group-1")
                .logicalOperator("and")
                .conditions(Collections.singletonList(condition))
                .build();
        RuleConditionPolicyDTO policyDTO = RuleConditionPolicyDTO.builder()
                .ruleId(1L)
                .conditionType(ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue())
                .status(ConditionStatusEnum.ENABLED.getValue())
                .antiShake(antiShake ? AntiShakeStatusEnum.ENABLED.getValue() : AntiShakeStatusEnum.DISABLED.getValue())
                .conditionScheme(objectMapper.writeValueAsString(Collections.singletonList(group)))
                .build();
        if (antiShake) {
            AntiShakeSchemePolicyDTO antiShakeScheme = AntiShakeSchemePolicyDTO.builder()
                    .frequency(AntiShakeSchemePolicyDTO.Frequency.builder()
                            .timeValue(3)
                            .count(2)
                            .build())
                    .occurrence(AntiShakeSchemePolicyDTO.Occurrence.builder()
                            .last(true)
                            .build())
                    .build();
            policyDTO.setAntiShakeScheme(objectMapper.writeValueAsString(antiShakeScheme));
        }
        return policyDTO;
    }

    private DeviceActionConditionDTO actionCondition(String deviceIdentification, String rightValue) {
        return DeviceActionConditionDTO.builder()
                .type("CONDITION")
                .uuid("condition-1")
                .leftParam(DeviceActionLeftParamDTO.builder()
                        .productIdentification(PRODUCT_ID)
                        .deviceIdentification(deviceIdentification)
                        .dataType("string")
                        .build())
                .operator(OperatorDTO.builder()
                        .value("EQ")
                        .desc("等于")
                        .build())
                .rightParams(Collections.singletonList(DeviceActionRightParamsDTO.builder()
                        .type("CONSTANT")
                        .value(rightValue)
                        .desc(rightValue)
                        .build()))
                .build();
    }
}
