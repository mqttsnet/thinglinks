package com.mqttsnet.thinglinks.service.execution.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyPair;
import com.mqttsnet.thinglinks.service.execution.RulePolicyStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 具体匹配实现
 *
 * @author xiaonan
 */
@Slf4j
@Service
@DS(DsConstant.BASE_TENANT)
public class RuleExecutionService {
    private final RulePolicyStrategyFactory rulePolicyStrategyFactory;

    @Autowired
    public RuleExecutionService(RulePolicyStrategyFactory rulePolicyStrategyFactory) {
        this.rulePolicyStrategyFactory = rulePolicyStrategyFactory;
    }

    /**
     * 根据给定的策略上下文执行策略。
     *
     * @param context 包含规则细节和条件的策略上下文
     */
    public void executePolicy(PolicyContext context) {
        Long originalTenantId = ContextUtil.getTenantId();
        try {
            ContextUtil.setTenantId(context.getTenantId());
            List<PolicyPair<RulePolicyStrategyService, RuleConditionPolicyDTO>> policyPairs = rulePolicyStrategyFactory.getPolicyStrategies(context);
            for (PolicyPair<RulePolicyStrategyService, RuleConditionPolicyDTO> policyPair : policyPairs) {
                RulePolicyStrategyService policy = policyPair.getFirst();
                RuleConditionPolicyDTO conditionPolicyDTO = policyPair.getSecond();
                policy.applyPolicy(context, conditionPolicyDTO);
            }
        } catch (Exception e) {
            log.error("Failed to execute policy.", e);
        } finally {
            ContextUtil.setTenantId(originalTenantId);
        }
    }


}
