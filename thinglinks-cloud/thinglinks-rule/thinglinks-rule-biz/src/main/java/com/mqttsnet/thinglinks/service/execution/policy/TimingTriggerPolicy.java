package com.mqttsnet.thinglinks.service.execution.policy;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import com.mqttsnet.thinglinks.service.execution.service.ActionProcessorService;
import com.mqttsnet.thinglinks.service.execution.service.RulePolicyStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * -----------------------------------------------------------------------------
 * File Name: TimingTriggerPolicy.java
 * -----------------------------------------------------------------------------
 * Description:
 * 定时触发策略
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-26 16:41
 */
@Slf4j
@Service
@DS(DsConstant.BASE_TENANT)
public class TimingTriggerPolicy implements RulePolicyStrategyService {

    @Autowired
    private ExecutionLogEventPublisher executionLogEventPublisher;

    @Autowired
    private ActionProcessorService actionProcessorService;

    @Override
    public void applyPolicy(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        log.info("Applying policy - Tenant ID: {}, Rule Identification: {}", context.getTenantId(), context.getRuleIdentification());
        // tenantId 上下文由调用方 RuleExecutionService.executePolicy 统一设置,本 Policy 信任不重设
        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Rule execution started at: {}", startTime);

        log.info("The timed triggering strategy is being applied to the rule：{}", context.getRulePolicyDTO().getRuleName());
        actionProcessorService.processActions(context, conditionPolicyDTO);

        // 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        log.info("Rule execution ended at: {}", endTime);

        // 记录规则执行日志
        executionLogEventPublisher.publishRuleExecutionLog(context.getRuleExecutionId(), context.getRuleIdentification(), context.getRuleName(),
                startTime, endTime, null, "Rule execution completed");
    }
}
