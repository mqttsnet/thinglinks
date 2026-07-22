package com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.dto.linkage.execution.RuleConditionExecutionExtendParamsDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.RuleExecutionLogExtendParamsDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.ActionExecutionLogEvent;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.ConditionExecutionLogEvent;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.RuleExecutionLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * -----------------------------------------------------------------------------
 * File Name: ExecutionLogEventPublisher
 * -----------------------------------------------------------------------------
 * Description:
 * <p>
 * 执行日志事件发布器
 * -----------------------------------------------------------------------------
 *
 * @author mqttsnet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/12/2       mqttsnet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/12/2 19:33
 */
@Component
@Slf4j
public class ExecutionLogEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 发布规则执行日志事件
     */
    public void publishRuleExecutionLog(Long ruleExecutionId, String ruleIdentification, String ruleName, LocalDateTime startTime, LocalDateTime endTime, String extendParams, String remark) {
        String normalizedExtendParams = buildRuleExtendParams(startTime, endTime, extendParams);
        RuleExecutionLogEvent event = new RuleExecutionLogEvent(this, ruleExecutionId, ruleIdentification, ruleName, startTime, endTime, normalizedExtendParams, remark);
        eventPublisher.publishEvent(event);
        log.info("Published rule execution log event: {}", event);
    }

    /**
     * 发布条件执行日志事件
     */
    public void publishConditionExecutionLog(Long ruleExecutionId, String conditionUuid, ConditionTypeEnum conditionTypeEnum, Boolean evaluationResult, LocalDateTime startTime, LocalDateTime endTime, String extendParams, String remark) {
        String normalizedExtendParams = buildConditionExtendParams(conditionUuid, conditionTypeEnum, evaluationResult, startTime, endTime, extendParams);
        ConditionExecutionLogEvent event = new ConditionExecutionLogEvent(this, ruleExecutionId, conditionUuid, conditionTypeEnum, evaluationResult, startTime, endTime, normalizedExtendParams, remark);
        eventPublisher.publishEvent(event);
        log.info("Published condition execution log event: {}", event);
    }

    /**
     * 发布动作执行日志事件
     */
    public void publishActionExecutionLog(Long ruleExecutionId, Integer actionType, String actionContent, Boolean result,
                                          LocalDateTime startTime, LocalDateTime endTime, String extendParams, String remark) {
        String normalizedExtendParams = ensureLatency(extendParams, startTime, endTime);
        ActionExecutionLogEvent event = new ActionExecutionLogEvent(this, ruleExecutionId, actionType, actionContent, result,
                startTime, endTime, normalizedExtendParams, remark);
        eventPublisher.publishEvent(event);
        log.info("Published action execution log event: {}", event);
    }

    private String buildRuleExtendParams(LocalDateTime startTime, LocalDateTime endTime, String extendParams) {
        Long latencyMs = resolveLatencyMs(startTime, endTime);
        if (isJsonObject(extendParams)) {
            return ensureLatency(extendParams, startTime, endTime);
        }
        RuleExecutionLogExtendParamsDTO params = RuleExecutionLogExtendParamsDTO.builder()
                .latencyMs(latencyMs)
                .detail(StrUtil.isNotBlank(extendParams) ? extendParams : null)
                .build();
        return JSON.toJSONString(params);
    }

    private String buildConditionExtendParams(String conditionUuid, ConditionTypeEnum conditionTypeEnum, Boolean evaluationResult,
                                              LocalDateTime startTime, LocalDateTime endTime, String extendParams) {
        Long latencyMs = resolveLatencyMs(startTime, endTime);
        if (isJsonObject(extendParams)) {
            return ensureLatency(extendParams, startTime, endTime);
        }
        RuleConditionExecutionExtendParamsDTO params = RuleConditionExecutionExtendParamsDTO.builder()
                .conditionUuid(conditionUuid)
                .conditionType(conditionTypeEnum == null ? null : conditionTypeEnum.getValue())
                .evaluationResult(evaluationResult)
                .latencyMs(latencyMs)
                .detail(StrUtil.isNotBlank(extendParams) ? extendParams : null)
                .build();
        return JSON.toJSONString(params);
    }

    private String ensureLatency(String extendParams, LocalDateTime startTime, LocalDateTime endTime) {
        Long latencyMs = resolveLatencyMs(startTime, endTime);
        if (StrUtil.isBlank(extendParams)) {
            return JSON.toJSONString(RuleExecutionLogExtendParamsDTO.builder()
                    .latencyMs(latencyMs)
                    .build());
        }
        if (!isJsonObject(extendParams)) {
            return JSON.toJSONString(RuleExecutionLogExtendParamsDTO.builder()
                    .latencyMs(latencyMs)
                    .detail(extendParams)
                    .build());
        }
        JSONObject jsonObject = JSON.parseObject(extendParams);
        jsonObject.putIfAbsent("latencyMs", latencyMs);
        return JSON.toJSONString(jsonObject);
    }

    private Long resolveLatencyMs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return Math.max(Duration.between(startTime, endTime).toMillis(), 0L);
    }

    private boolean isJsonObject(String value) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        try {
            JSON.parseObject(value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

}
