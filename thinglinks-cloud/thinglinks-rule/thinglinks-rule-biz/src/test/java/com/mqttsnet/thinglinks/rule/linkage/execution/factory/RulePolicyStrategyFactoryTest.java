package com.mqttsnet.thinglinks.rule.linkage.execution.factory;

import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyPair;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.RulePolicyStrategyFactory;
import com.mqttsnet.thinglinks.service.execution.policy.DeviceActionTriggerPolicy;
import com.mqttsnet.thinglinks.service.execution.policy.DevicePropertiesPolicy;
import com.mqttsnet.thinglinks.service.execution.policy.TimingTriggerPolicy;
import com.mqttsnet.thinglinks.service.execution.service.RulePolicyStrategyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("场景联动策略工厂")
class RulePolicyStrategyFactoryTest {

    private final DevicePropertiesPolicy devicePropertiesPolicy = mock(DevicePropertiesPolicy.class);
    private final TimingTriggerPolicy timingTriggerPolicy = mock(TimingTriggerPolicy.class);
    private final DeviceActionTriggerPolicy deviceActionTriggerPolicy = mock(DeviceActionTriggerPolicy.class);
    private final RulePolicyStrategyFactory factory = new RulePolicyStrategyFactory(
            devicePropertiesPolicy,
            timingTriggerPolicy,
            deviceActionTriggerPolicy);

    @Test
    @DisplayName("事件触发路径只分发匹配的条件类型，避免一次事件扫全量条件")
    void getPolicyStrategiesShouldFilterByTriggerConditionType() {
        RuleConditionPolicyDTO properties = condition(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER);
        RuleConditionPolicyDTO action = condition(ConditionTypeEnum.DEVICE_ACTION_TRIGGER);
        PolicyContext context = context(List.of(properties, action));
        context.setTriggerConditionType(ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue());

        List<PolicyPair<RulePolicyStrategyService, RuleConditionPolicyDTO>> strategies =
                factory.getPolicyStrategies(context);

        assertEquals(1, strategies.size());
        assertSame(deviceActionTriggerPolicy, strategies.get(0).getFirst());
        assertSame(action, strategies.get(0).getSecond());
    }

    @Test
    @DisplayName("定时/API 路径未指定触发类型时按条件列表完整分发")
    void getPolicyStrategiesShouldReturnAllStrategiesWhenTriggerTypeMissing() {
        RuleConditionPolicyDTO properties = condition(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER);
        RuleConditionPolicyDTO timing = condition(ConditionTypeEnum.TIMING_TRIGGER);
        RuleConditionPolicyDTO action = condition(ConditionTypeEnum.DEVICE_ACTION_TRIGGER);
        PolicyContext context = context(List.of(properties, timing, action));

        List<PolicyPair<RulePolicyStrategyService, RuleConditionPolicyDTO>> strategies =
                factory.getPolicyStrategies(context);

        assertEquals(3, strategies.size());
        assertSame(devicePropertiesPolicy, strategies.get(0).getFirst());
        assertSame(timingTriggerPolicy, strategies.get(1).getFirst());
        assertSame(deviceActionTriggerPolicy, strategies.get(2).getFirst());
    }

    @Test
    @DisplayName("上下文或规则条件为空时返回空策略列表")
    void getPolicyStrategiesShouldReturnEmptyWhenContextMissing() {
        assertTrue(factory.getPolicyStrategies(null).isEmpty());

        PolicyContext context = new PolicyContext();
        context.setRulePolicyDTO(RulePolicyDTO.builder().build());
        assertTrue(factory.getPolicyStrategies(context).isEmpty());
    }

    @Test
    @DisplayName("暂未支持的自定义 API 条件会显式失败，避免静默漏执行")
    void getPolicyStrategiesShouldFailForUnsupportedConditionType() {
        PolicyContext context = context(List.of(condition(ConditionTypeEnum.CUSTOM_API_TRIGGER)));

        assertThrows(IllegalStateException.class, () -> factory.getPolicyStrategies(context));
    }

    private PolicyContext context(List<RuleConditionPolicyDTO> conditions) {
        PolicyContext context = new PolicyContext();
        context.setRulePolicyDTO(RulePolicyDTO.builder()
                .ruleIdentification("rule-1")
                .ruleName("测试联动")
                .ruleConditionPolicyDTOS(conditions)
                .build());
        return context;
    }

    private RuleConditionPolicyDTO condition(ConditionTypeEnum type) {
        return RuleConditionPolicyDTO.builder()
                .conditionType(type.getValue())
                .build();
    }
}
