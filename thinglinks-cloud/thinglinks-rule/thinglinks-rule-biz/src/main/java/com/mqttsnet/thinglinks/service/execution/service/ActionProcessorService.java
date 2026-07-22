package com.mqttsnet.thinglinks.service.execution.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionActionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.RuleActionExecutionExtendParamsDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleActionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.event.action.publisher.RuleConditionActionEventPublisher;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * -----------------------------------------------------------------------------
 * File Name: ActionProcessorService
 * -----------------------------------------------------------------------------
 * Description:
 * 动作处理服务类
 * -----------------------------------------------------------------------------
 *
 * @author mqttsnet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/31       mqttsnet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/31 16:07
 */
@Slf4j
@Service
public class ActionProcessorService {

    @Autowired
    private ExecutionLogEventPublisher executionLogEventPublisher;

    @Autowired
    private RuleConditionActionEventPublisher ruleConditionActionEventPublisher;

    public void processActions(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        conditionPolicyDTO.getConditionActionPolicyDTOS().stream()
                .filter(actionPolicyDTO -> StrUtil.isNotBlank(actionPolicyDTO.getActionContent()))
                .forEach(actionPolicyDTO -> handleActionExecution(context, actionPolicyDTO));
    }

    /**
     * 处理单个动作的执行
     */
    private void handleActionExecution(PolicyContext context, RuleConditionActionPolicyDTO actionPolicyDTO) {
        log.info("Processing action: {}", actionPolicyDTO.getActionContent());
        Long ruleExecutionId = context.getRuleExecutionId();
        RuleActionTypeEnum actionType = RuleActionTypeEnum.fromValue(actionPolicyDTO.getActionType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid action type"));

        LocalDateTime actionStartTime = LocalDateTime.now();
        boolean actionSuccess = executeAction(context, actionPolicyDTO, actionType);
        // 动作结束时间
        LocalDateTime actionEndTime = LocalDateTime.now();
        // 构建 extendParams
        String extendParams = buildExtendParams(actionPolicyDTO, actionStartTime, actionEndTime, actionSuccess);
        // 发布执行日志
        publishActionExecutionLog(ruleExecutionId, actionPolicyDTO, actionSuccess, actionStartTime, actionEndTime, extendParams);
    }

    /**
     * 执行动作
     */
    private boolean executeAction(PolicyContext context, RuleConditionActionPolicyDTO actionPolicyDTO, RuleActionTypeEnum actionType) {
        try {
            switch (actionType) {
                case COMMAND:
                    ruleConditionActionEventPublisher.publishCommandActionEvent(context, actionPolicyDTO);
                    break;
                case ALERT:
                    ruleConditionActionEventPublisher.publishAlertActionEvent(context, actionPolicyDTO);
                    break;
                case FORWARD:
                    ruleConditionActionEventPublisher.publishForwardActionEvent(context, actionPolicyDTO);
                    break;
                default:
                    log.warn("Unsupported action type: {}", actionPolicyDTO.getActionType());
                    return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error processing action: {}", actionPolicyDTO.getActionContent(), e);
            return false;
        }
    }

    /**
     * 构建扩展参数
     */
    private String buildExtendParams(RuleConditionActionPolicyDTO actionPolicyDTO, LocalDateTime actionStartTime,
                                     LocalDateTime actionEndTime, boolean actionSuccess) {
        long latencyMs = Math.max(ChronoUnit.MILLIS.between(actionStartTime, actionEndTime), 0L);
        RuleActionExecutionExtendParamsDTO extendParams = RuleActionExecutionExtendParamsDTO.builder()
                .ruleConditionId(actionPolicyDTO.getRuleConditionId())
                .actionType(actionPolicyDTO.getActionType())
                .actionName(RuleActionTypeEnum.fromValue(actionPolicyDTO.getActionType())
                        .map(RuleActionTypeEnum::getDesc)
                        .orElse("未知动作"))
                .result(actionSuccess)
                .latencyMs(latencyMs)
                .remark(actionPolicyDTO.getRemark())
                .build();
        return JSON.toJSONString(extendParams);
    }

    /**
     * 发布动作执行日志事件
     */
    private void publishActionExecutionLog(Long ruleExecutionId, RuleConditionActionPolicyDTO actionPolicyDTO,
                                           boolean actionSuccess, LocalDateTime actionStartTime, LocalDateTime actionEndTime,
                                           String extendParams) {
        executionLogEventPublisher.publishActionExecutionLog(
                ruleExecutionId,
                actionPolicyDTO.getActionType(),
                actionPolicyDTO.getActionContent(),
                actionSuccess,
                actionStartTime,
                actionEndTime,
                extendParams,
                actionSuccess ? "Action executed successfully" : "Action executed failed"
        );
        log.info("Published action execution log for action: {}", actionPolicyDTO.getActionContent());
    }

}
