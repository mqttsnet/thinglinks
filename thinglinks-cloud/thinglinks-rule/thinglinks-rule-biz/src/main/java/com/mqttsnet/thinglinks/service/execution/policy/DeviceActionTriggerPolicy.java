package com.mqttsnet.thinglinks.service.execution.policy;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.condition.enumeration.OperatorEnum;
import com.mqttsnet.basic.condition.model.dto.BaseConditionDTO;
import com.mqttsnet.basic.condition.service.ConditionEvaluatorService;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.tds.enumeration.TdDataTypeEnum;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceActionCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.vo.result.DeviceActionResultVO;
import com.mqttsnet.thinglinks.dto.linkage.AntiShakeSchemePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionConditionDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionConditionGroupDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionLeftParamDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DeviceActionRightParamsDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.enumeration.linkage.AntiShakeStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.FieldValueWithType;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import com.mqttsnet.thinglinks.service.execution.service.ActionProcessorService;
import com.mqttsnet.thinglinks.service.execution.service.AntiShakeSchemeEvaluatorService;
import com.mqttsnet.thinglinks.service.execution.service.RulePolicyStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * -----------------------------------------------------------------------------
 * File Name: DeviceActionTriggerPolicy.java
 * -----------------------------------------------------------------------------
 * Description:
 * 设备动作触发策略
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
 * @date 2023-11-26 16:42
 */
@Slf4j
@DS(DsConstant.BASE_TENANT)
@Service
public class DeviceActionTriggerPolicy implements RulePolicyStrategyService {
    private final LinkCacheDataHelper linkCacheDataHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    private ExecutionLogEventPublisher executionLogEventPublisher;

    @Autowired
    private ConditionEvaluatorService conditionEvaluatorService;

    @Autowired
    private ActionProcessorService actionProcessorService;

    public DeviceActionTriggerPolicy(LinkCacheDataHelper linkCacheDataHelper) {
        this.linkCacheDataHelper = linkCacheDataHelper;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void applyPolicy(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        log.info("Applying policy - Tenant ID: {}, Rule Identification: {}", context.getTenantId(), context.getRuleIdentification());
        // tenantId 上下文由调用方 RuleExecutionService.executePolicy 统一设置,本 Policy 信任不重设

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Rule execution started at: {}", startTime);

        validateContextAndPolicyDTO(context, conditionPolicyDTO);
        log.info("Validated Context and Policy DTO for Rule ID: {}", context.getRuleIdentification());
        applyActionLogic(context, conditionPolicyDTO);

        // 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        log.info("Rule execution ended at: {}", endTime);

        // 记录规则执行日志
        executionLogEventPublisher.publishRuleExecutionLog(context.getRuleExecutionId(), context.getRuleIdentification(), context.getRuleName(),
                startTime, endTime, null, "Rule execution completed");


    }

    private void validateContextAndPolicyDTO(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        if (context == null || context.getRulePolicyDTO() == null) {
            log.error("PolicyContext or RulePolicyDTO cannot be null.");
            throw BizException.wrap("PolicyContext and RulePolicyDTO cannot be null.");
        }
        log.info("Applying device action trigger policy for tenant ID {} and rule ID {}.",
                context.getRuleIdentification(), context.getRulePolicyDTO().getRuleIdentification());
    }


    /**
     * Processes the action logic for the given policy conditions.
     *
     * @param context            the policy context to validate
     * @param conditionPolicyDTO the policy condition DTO containing the condition logic to apply
     */
    private void applyActionLogic(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        log.info("Processing action logic - Condition Type: {}, Status: {}",
                conditionPolicyDTO.getConditionType(), conditionPolicyDTO.getStatus());
        ConditionTypeEnum conditionType = ConditionTypeEnum.fromValue(conditionPolicyDTO.getConditionType());
        ConditionStatusEnum conditionStatus = ConditionStatusEnum.fromValue(conditionPolicyDTO.getStatus());

        if (!ConditionTypeEnum.DEVICE_ACTION_TRIGGER.equals(conditionType) || ConditionStatusEnum.DISABLED.equals(conditionStatus)) {
            log.info("The condition type is not device action trigger or the condition is not enabled.");
            return;
        }

        List<DeviceActionConditionGroupDTO> conditionGroups = parseConditionValue(conditionPolicyDTO.getConditionScheme());
        log.info("Parsed condition groups - Number of Groups: {}", conditionGroups.size());
        AntiShakeStatusEnum antiShakeStatus = AntiShakeStatusEnum.fromValue(conditionPolicyDTO.getAntiShake());

        // Check if AntiShakePolicy is enabled and apply it
        Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO = Optional.empty();
        if (AntiShakeStatusEnum.ENABLED.equals(antiShakeStatus)) {
            optionalAntiShakeSchemePolicyDTO = performAntiShakeLogic(conditionPolicyDTO.getAntiShakeScheme());
            log.info("Anti-shake policy applied: {}", optionalAntiShakeSchemePolicyDTO);
        }

        Map<String, Boolean> conditionResults = new HashMap<>();
        boolean allConditionsMet = true; // 用于检查是否所有条件都满足

        for (DeviceActionConditionGroupDTO group : conditionGroups) {
            boolean groupResult = evaluateConditionGroup(context, optionalAntiShakeSchemePolicyDTO, group);
            conditionResults.put(group.getUuid(), groupResult);
            // 如果任何一个组不满足，allConditionsMet 将变为 false
            allConditionsMet = allConditionsMet && groupResult;
            log.info("Condition group {} evaluated to {}", group.getUuid(), groupResult);
        }

        log.info("All condition groups evaluated. All conditions met: {}", allConditionsMet);

        // 如果所有条件组都满足，则执行相应的动作
        if (allConditionsMet) {
            actionProcessorService.processActions(context, conditionPolicyDTO);
        }
    }

    /**
     * Performs anti-shake logic based on the given anti-shake scheme.
     *
     * @param antiShakeScheme the anti-shake scheme to apply
     * @return an optional AntiShakeSchemePolicyDTO object
     */
    public Optional<AntiShakeSchemePolicyDTO> performAntiShakeLogic(String antiShakeScheme) {
        try {
            AntiShakeSchemePolicyDTO policyDTO = objectMapper.readValue(antiShakeScheme, new TypeReference<AntiShakeSchemePolicyDTO>() {
            });
            log.info("Anti-shake logic applied with scheme: {}", policyDTO);
            return Optional.of(policyDTO);
        } catch (IOException e) {
            log.error("Error parsing the anti-shake scheme string", e);
            return Optional.empty();
        }
    }

    private List<DeviceActionConditionGroupDTO> parseConditionValue(String conditionValue) {
        try {
            return objectMapper.readValue(conditionValue, new TypeReference<List<DeviceActionConditionGroupDTO>>() {
            });
        } catch (IOException e) {
            log.error("Error parsing the condition scheme string", e);
            throw new RuntimeException("Cannot parse the condition scheme string", e);
        }
    }

    /**
     * Evaluates a condition group using the logical operator.
     *
     * @param context                          the policy context to validate
     * @param optionalAntiShakeSchemePolicyDTO The optional anti-shake policy to apply.
     * @param group                            The condition group to evaluate.
     * @return The result of the condition group evaluation.
     */
    private boolean evaluateConditionGroup(PolicyContext context, Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO, DeviceActionConditionGroupDTO group) {
        try {
            log.info("Evaluating condition group - Group ID: {}, Logical Operator: {}, Number of Conditions: {}",
                    group.getUuid(), group.getLogicalOperator(), group.getConditions().size());

            LocalDateTime conditionExecutionLogStartTime = LocalDateTime.now();

            StringBuilder extendParamsBuilder = new StringBuilder();
            boolean result = true; // 默认为 true，适用于 AND 和 OR 逻辑

            // 用于构造简单的备注信息
            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append("Condition Group UUID:【").append(group.getUuid()).append("】, ");

            if (group.getLogicalOperator().equalsIgnoreCase(BaseConditionDTO.LogicalOperator.AND.getValue())) {
                // 处理 AND 逻辑
                for (DeviceActionConditionDTO condition : group.getConditions()) {
                    boolean conditionResult = evaluateCondition(context, optionalAntiShakeSchemePolicyDTO, condition);

                    // 记录每个条件的评估细节到 extendParams
                    extendParamsBuilder.append("Condition UUID:【").append(condition.getUuid()).append("】")
                            .append(", Type: ").append(condition.getType())
                            .append(", Result: ").append(conditionResult)
                            .append("; ");

                    log.info("Condition evaluation result in AND group - Condition ID: {}, Result: {}",
                            condition.getUuid(), conditionResult);

                    if (!conditionResult) {
                        log.info("Condition group evaluation result - Group ID: {}, Result: false (due to AND logic)", group.getUuid());
                        return false; // 短路逻辑，如果是 AND 且有 false
                    }
                }
                remarkBuilder.append("Logical Operator: AND, All conditions must be true.");
            } else { // 如果是 OR 逻辑
                result = false;
                for (DeviceActionConditionDTO condition : group.getConditions()) {
                    boolean conditionResult = evaluateCondition(context, optionalAntiShakeSchemePolicyDTO, condition);

                    // 记录每个条件的评估细节到 extendParams
                    extendParamsBuilder.append("Condition UUID:【").append(condition.getUuid()).append("】")
                            .append(", Type: ").append(condition.getType())
                            .append(", Result: ").append(conditionResult)
                            .append("; ");

                    log.info("Condition evaluation result in OR group - Condition ID: {}, Result: {}",
                            condition.getUuid(), conditionResult);

                    if (conditionResult) {
                        log.info("Condition group evaluation result - Group ID: {}, Result: true (due to OR logic)", group.getUuid());
                        return true; // 短路逻辑，如果是 OR 且有 true
                    }
                }
                remarkBuilder.append("Logical Operator: OR, At least one condition must be true.");
            }

            log.info("Final condition group evaluation result - Group ID: {}, Result: {}", group.getUuid(), result);

            // 将条件组评估的详细信息存储到 extendParams
            String extendParams = extendParamsBuilder.toString();
            // 简洁明了的 remark
            String remark = remarkBuilder.toString();
            // 推送条件组执行日志事件
            executionLogEventPublisher.publishConditionExecutionLog(context.getRuleExecutionId(), group.getUuid(), ConditionTypeEnum.DEVICE_ACTION_TRIGGER, result, conditionExecutionLogStartTime, LocalDateTime.now(), extendParams, remark);

            return result;
        } catch (Exception e) {
            log.error("Error evaluating condition group: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean evaluateCondition(PolicyContext context, Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO, DeviceActionConditionDTO actionConditionDTO) {
        // 记录评估开始时间
        LocalDateTime actionStartTime = LocalDateTime.now();

        try {
            // 获取左侧值的标识符
            String productIdentification = actionConditionDTO.getLeftParam().getProductIdentification();
            String deviceIdentification = actionConditionDTO.getLeftParam().getDeviceIdentification();

            // 从缓存中获取设备动作缓存数据
            Map<Long, DeviceActionCacheVO> deviceActionCacheVO = linkCacheDataHelper.getDeviceActionCacheVO(productIdentification, deviceIdentification, false);

            log.info("Evaluating action - Product ID: {}, Device ID: {}, Operator: {}, Action Cache Size: {}",
                    productIdentification, deviceIdentification, actionConditionDTO.getOperator().getValue(), deviceActionCacheVO.size());

            // 提取左侧值
            FieldValueWithType fieldValueWithType = extractLeftValue(optionalAntiShakeSchemePolicyDTO, actionConditionDTO.getLeftParam(), deviceActionCacheVO);
            if (fieldValueWithType == null) {
                log.warn("No left value found for Condition UUID : {}", actionConditionDTO.getUuid());
                recordActionLog(context, actionConditionDTO, actionStartTime, false, "No left value found");
                return false;
            }

            // 获取操作符和右侧值
            TdDataTypeEnum leftDataType = fieldValueWithType.getDataType();
            OperatorEnum operator = OperatorEnum.valueOf(actionConditionDTO.getOperator().getValue().toUpperCase());

            // 获取右侧值，并转换为与左侧值相同的数据类型
            List<Object> rightValues = actionConditionDTO.getRightParams().stream()
                    .map(DeviceActionRightParamsDTO::getValue)
                    .map(value -> TdDataTypeEnum.convertValue(value, leftDataType))
                    .collect(Collectors.toList());

            log.info("Action details - Left Value: {}, Data Type: {}, Operator: {}, Right Values: {}",
                    fieldValueWithType.getValue(), leftDataType.getDataType(), operator, rightValues);

            // 执行条件比较
            boolean result = conditionEvaluatorService.compare(fieldValueWithType.getValue(), operator, rightValues);

            // 记录条件评估结果
            log.info("Condition evaluation result - Condition UUID: {}, Result: {}", actionConditionDTO.getUuid(), result);

            // 记录评估日志
            StringBuilder extendParamsBuilder = new StringBuilder();
            extendParamsBuilder.append("Condition UUID: 【").append(actionConditionDTO.getUuid()).append("】")
                    .append(", Type: ").append(actionConditionDTO.getType())
                    .append(", DataType: ").append(leftDataType.getDataType())
                    .append(", Left Value: ").append(fieldValueWithType.getValue())
                    .append(", Operator: ").append(operator)
                    .append(", Right Values: ").append(rightValues)
                    .append("; ");

            recordActionLog(context, actionConditionDTO, actionStartTime, result, extendParamsBuilder.toString());

            return result;
        } catch (Exception e) {
            log.error("Error evaluating action: Action Condition UUID: {}, Error: {}", actionConditionDTO.getUuid(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 记录单个条件的执行日志
     *
     * @param context      The policy context to validate.
     * @param condition    The condition to record.
     * @param startTime    The start time of the condition evaluation.
     * @param result       The result of the condition evaluation.
     * @param extendParams Detailed parameters related to the condition.
     */
    private void recordActionLog(PolicyContext context, DeviceActionConditionDTO condition, LocalDateTime startTime,
                                 boolean result, String extendParams) {
        // 记录条件评估结束时间
        LocalDateTime endTime = LocalDateTime.now();
        // 构造备注信息
        String remark = "Condition UUID:【" + condition.getUuid() + "】, Result: " + result;
        // 发布条件评估日志事件
        executionLogEventPublisher.publishConditionExecutionLog(context.getRuleExecutionId(), condition.getUuid(), ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER, result, startTime, endTime, extendParams, remark);
    }

    /**
     * Extracts the left value from device action.
     *
     * @param optionalAntiShakeSchemePolicyDTO The optional anti-shake policy to apply.
     * @param leftParam                        Left parameter DTO.
     * @param deviceActionCacheVO              Device action collection.
     * @return {@link FieldValueWithType} object containing the left value.
     */
    private FieldValueWithType extractLeftValue(Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO, DeviceActionLeftParamDTO leftParam, Map<Long, DeviceActionCacheVO> deviceActionCacheVO) {
        try {
            AntiShakeSchemeEvaluatorService<DeviceActionCacheVO> evaluator = new AntiShakeSchemeEvaluatorService<>();
            DeviceActionCacheVO selectedAction = evaluator.applyAntiShakePolicy(deviceActionCacheVO, optionalAntiShakeSchemePolicyDTO);

            if (Objects.isNull(selectedAction)) {
                log.warn("No latest DeviceActionCacheVO found.");
                return null;
            }

            return safelyGetActionValue(BeanPlusUtil.toBeanIgnoreError(selectedAction, DeviceActionResultVO.class));
        } catch (Exception e) {
            log.error("Error extracting left value: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从 DeviceActionResultVO 获取动作结果值。
     *
     * @param actionResult 动作结果对象
     * @return {@link FieldValueWithType} 属性类型和值
     */
    private FieldValueWithType safelyGetActionValue(DeviceActionResultVO actionResult) {
        try {
            if (actionResult != null && StrUtil.isNotBlank(actionResult.getActionType())) {
                String valueAsString = String.valueOf(actionResult.getActionType());
                TdDataTypeEnum dataTypeEnum = TdDataTypeEnum.valueOfByDataType("string");
                Object convertedValue = TdDataTypeEnum.convertValue(valueAsString, dataTypeEnum);
                return new FieldValueWithType(dataTypeEnum, convertedValue);
            } else {
                return actionResult != null ? new FieldValueWithType(TdDataTypeEnum.NCHAR, actionResult.getActionType()) : null;
            }
        } catch (Exception e) {
            log.error("Error converting action value: {}", e.getMessage(), e);
            return null;
        }
    }
}