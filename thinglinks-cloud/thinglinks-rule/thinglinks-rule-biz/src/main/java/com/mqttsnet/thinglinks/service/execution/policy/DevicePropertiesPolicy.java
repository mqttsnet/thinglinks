package com.mqttsnet.thinglinks.service.execution.policy;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.condition.enumeration.OperatorEnum;
import com.mqttsnet.basic.condition.model.dto.BaseConditionDTO;
import com.mqttsnet.basic.condition.service.ConditionEvaluatorService;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.tds.enumeration.TdDataTypeEnum;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.dto.linkage.AntiShakeSchemePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesConditionDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesConditionGroupDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesLeftParamDTO;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesRightParamsDTO;
import com.mqttsnet.thinglinks.common.cache.rule.trigger.RuleTriggerCacheKeys;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.AntiShakeStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionStatusEnum;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
import com.mqttsnet.thinglinks.service.execution.FieldValueWithType;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.publisher.ExecutionLogEventPublisher;
import com.mqttsnet.thinglinks.service.execution.service.ActionProcessorService;
import com.mqttsnet.thinglinks.service.execution.service.RulePolicyStrategyService;
import com.mqttsnet.thinglinks.service.execution.trigger.ActionCooldownService;
import com.mqttsnet.thinglinks.service.execution.trigger.AntiShakeCounterService;
import com.mqttsnet.thinglinks.service.execution.trigger.DeviceLatestSnapshotService;
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
 * Service class for applying device property-based policies.
 *
 * @author xiaonan
 */
@Slf4j
@Service
public class DevicePropertiesPolicy implements RulePolicyStrategyService {
    private final ObjectMapper objectMapper;

    @Autowired
    private ExecutionLogEventPublisher executionLogEventPublisher;

    @Autowired
    private ConditionEvaluatorService conditionEvaluatorService;

    @Autowired
    private ActionProcessorService actionProcessorService;

    @Autowired
    private AntiShakeCounterService antiShakeCounterService;

    @Autowired
    private ActionCooldownService actionCooldownService;

    @Autowired
    private DeviceLatestSnapshotService deviceLatestSnapshotService;

    public DevicePropertiesPolicy() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Applies the appropriate policies based on the provided context and policy conditions.
     *
     * @param context            the policy context containing all necessary information
     * @param conditionPolicyDTO the DTO that contains the policy conditions
     */
    @Override
    public void applyPolicy(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        log.info("Applying policy - Tenant ID: {}, Rule Identification: {}，Rule ExecutionId: {}", context.getTenantId(), context.getRuleIdentification(), context.getRuleExecutionId());
        // tenantId 上下文由调用方 RuleExecutionService.executePolicy 统一设置,本 Policy 信任不重设

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Rule execution started at: {}", startTime);

        // 校验参数
        validateContextAndPolicyDTO(context, conditionPolicyDTO);
        log.info("Validated Context and Policy DTO for Rule ID: {}", context.getRuleIdentification());

        // 执行条件逻辑
        applyConditionLogic(context, conditionPolicyDTO);

        // 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        log.info("Rule execution ended at: {}", endTime);

        // 记录规则执行日志
        executionLogEventPublisher.publishRuleExecutionLog(context.getRuleExecutionId(), context.getRuleIdentification(), context.getRuleName(),
                startTime, endTime, "", "Rule execution completed");
    }

    /**
     * Validates that the context and policy DTO are not null.
     *
     * @param context            the policy context to validate
     * @param conditionPolicyDTO the policy condition DTO to validate
     * @throws IllegalArgumentException if either context or rulePolicyDTO is null
     */
    private void validateContextAndPolicyDTO(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        if (context == null || context.getRulePolicyDTO() == null) {
            log.error("PolicyContext or RulePolicyDTO cannot be null.");
            throw BizException.wrap("PolicyContext and RulePolicyDTO cannot be null.");
        }
        log.info("Applying device properties policy for tenant ID {} and rule ID {}.",
                context.getRuleIdentification(), context.getRulePolicyDTO().getRuleIdentification());
    }

    /**
     * Processes the condition logic for the given policy conditions.
     *
     * @param context            the policy context to validate
     * @param conditionPolicyDTO the policy condition DTO containing the condition logic to apply
     */
    private void applyConditionLogic(PolicyContext context, RuleConditionPolicyDTO conditionPolicyDTO) {
        log.info("Processing condition logic - Condition Type: {}, Status: {}", conditionPolicyDTO.getConditionType(), conditionPolicyDTO.getStatus());
        ConditionTypeEnum conditionType = ConditionTypeEnum.fromValue(conditionPolicyDTO.getConditionType());
        ConditionStatusEnum conditionStatus = ConditionStatusEnum.fromValue(conditionPolicyDTO.getStatus());

        if (!ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.equals(conditionType) || ConditionStatusEnum.DISABLED.equals(conditionStatus)) {
            log.info("The condition type is not device properties trigger or the condition is not enabled.");
            return;
        }

        // 解析条件组
        List<DevicePropertiesConditionGroupDTO> conditionGroups = parseConditionValue(conditionPolicyDTO.getConditionScheme());
        log.info("Parsed condition groups - Number of Groups: {}", conditionGroups.size());
        AntiShakeStatusEnum antiShakeStatus = AntiShakeStatusEnum.fromValue(conditionPolicyDTO.getAntiShake());

        // Check if AntiShakePolicy is enabled and apply it
        Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO = Optional.empty();
        if (AntiShakeStatusEnum.ENABLED.equals(antiShakeStatus)) {
            optionalAntiShakeSchemePolicyDTO = performAntiShakeLogic(conditionPolicyDTO.getAntiShakeScheme());
            log.info("Anti-shake policy applied: {}", optionalAntiShakeSchemePolicyDTO);
        }

        Map<String, Boolean> conditionResults = new HashMap<>();
        boolean allConditionsMet = true;

        for (DevicePropertiesConditionGroupDTO group : conditionGroups) {
            boolean groupResult = evaluateConditionGroup(context, optionalAntiShakeSchemePolicyDTO, group);
            conditionResults.put(group.getUuid(), groupResult);
            // 如果任何一个组不满足，allConditionsMet 将变为 false
            allConditionsMet = allConditionsMet && groupResult;
            log.info("Condition group {} evaluated to {}", group.getUuid(), groupResult);
        }

        log.info("All condition groups evaluated. All conditions met: {}", allConditionsMet);
        // 所有条件组满足且拿到动作冷却执行权才执行动作(事件驱动下条件持续满足时防动作风暴;
        // 事件/定时双触发共用同一把冷却闸,存量定时任务未停用期间天然防重复动作)
        if (allConditionsMet && actionCooldownService.tryAcquire(context, conditionPolicyDTO)) {
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

    /**
     * Parses the condition scheme string into a  st of DevicePropertiesConditionGroupDTO objects.
     *
     * @param conditionValue the condition scheme string to parse
     * @return a list of DevicePropertiesConditionGroupDTO objects
     * @throws RuntimeException if there is an error parsing the condition scheme
     */
    private List<DevicePropertiesConditionGroupDTO> parseConditionValue(String conditionValue) {
        try {
            return objectMapper.readValue(conditionValue, new TypeReference<List<DevicePropertiesConditionGroupDTO>>() {
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
    private boolean evaluateConditionGroup(PolicyContext context, Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO, DevicePropertiesConditionGroupDTO group) {
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
                for (DevicePropertiesConditionDTO condition : group.getConditions()) {
                    boolean conditionResult = evaluateCondition(context, optionalAntiShakeSchemePolicyDTO, condition);

                    // 记录每个条件的评估细节到 extendParams
                    extendParamsBuilder.append("Condition UUID: 【").append(condition.getUuid()).append("】")
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
                for (DevicePropertiesConditionDTO condition : group.getConditions()) {
                    boolean conditionResult = evaluateCondition(context, optionalAntiShakeSchemePolicyDTO, condition);

                    // 记录每个条件的评估细节到 extendParams
                    extendParamsBuilder.append("Condition UUID: 【").append(condition.getUuid()).append("】")
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
            executionLogEventPublisher.publishConditionExecutionLog(context.getRuleExecutionId(), group.getUuid(), ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER, result, conditionExecutionLogStartTime, LocalDateTime.now(), extendParams, remark);

            return result;
        } catch (Exception e) {
            log.error("Error evaluating condition group: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * Evaluates a single condition using the provided AntiShake scheme and condition details.
     *
     * @param context                          The policy context to validate.
     * @param optionalAntiShakeSchemePolicyDTO The optional anti-shake policy to apply.
     * @param condition                        The condition to evaluate.
     * @return true if the condition is met, false otherwise.
     */
    private boolean evaluateCondition(PolicyContext context, Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO, DevicePropertiesConditionDTO condition) {
        try {
            // 获取左侧值的标识符
            String productIdentification = condition.getLeftParam().getProductIdentification();
            String deviceIdentification = condition.getLeftParam().getDeviceIdentification();

            // 记录评估开始时间
            LocalDateTime conditionStartTime = LocalDateTime.now();

            // 选定参与评估的物模型数据:事件内值(实时路径,零缓存读)→ 最新快照(定时/跨设备)→ 数据收集池(过渡期兜底)
            ProductResultVO selectedProductResult = resolveEvaluationSource(context, optionalAntiShakeSchemePolicyDTO, condition);
            if (Objects.isNull(selectedProductResult)) {
                log.info("No evaluation data selected (anti-shake gating or no snapshot) - Condition UUID: {}, Product ID: {}, Device ID: {}",
                        condition.getUuid(), productIdentification, deviceIdentification);
                recordConditionLog(context, condition, conditionStartTime, false, "No evaluation data selected");
                return false;
            }

            log.info("Evaluating condition - Product ID: {}, Device ID: {}, Operator: {}",
                    productIdentification, deviceIdentification, condition.getOperator().getValue());

            // 提取左侧值（需要根据具体的左侧参数定义来获取）
            FieldValueWithType fieldValueWithType = extractFieldValue(selectedProductResult, condition.getLeftParam());

            // 如果左侧值为 null，记录警告并返回 false
            if (Objects.isNull(fieldValueWithType)) {
                log.warn("No left value found for Condition UUID: {}", condition.getUuid());
                recordConditionLog(context, condition, conditionStartTime, false, "No left value found");
                return false;
            }

            // 获取左侧值的数据类型
            TdDataTypeEnum leftDataType = fieldValueWithType.getDataType();

            // 获取操作符
            OperatorEnum operator = OperatorEnum.valueOf(condition.getOperator().getValue().toUpperCase());

            // 获取右侧值，并转换为与左侧值相同的数据类型
            List<Object> rightValues = condition.getRightParams().stream()
                    .map(DevicePropertiesRightParamsDTO::getValue)
                    .map(rightStr -> TdDataTypeEnum.convertValue(rightStr, leftDataType))
                    .collect(Collectors.toList());

            // 记录条件详细信息
            StringBuilder extendParamsBuilder = new StringBuilder();
            extendParamsBuilder.append("Condition UUID: 【").append(condition.getUuid()).append("】")
                    .append(", Type: ").append(condition.getType())
                    .append(", DataType: ").append(leftDataType.getDataType())
                    .append(", Left Value: ").append(fieldValueWithType.getValue())
                    .append(", Operator: ").append(operator)
                    .append(", Right Values: ").append(rightValues)
                    .append("; ");

            log.info("Condition details - Left Value: {}, dataType: {}, Operator: {}, Right Values: {}",
                    fieldValueWithType.getValue(), leftDataType.getDataType(), operator, rightValues);

            // 执行条件比较
            boolean result = conditionEvaluatorService.compare(fieldValueWithType.getValue(), operator, rightValues);

            // 记录条件评估结果
            log.info("Condition evaluation result - Result: {}", result);

            // 记录条件评估日志
            recordConditionLog(context, condition, conditionStartTime, result, extendParamsBuilder.toString());

            return result;
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", e.getMessage(), e);
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
    private void recordConditionLog(PolicyContext context, DevicePropertiesConditionDTO condition,
                                    LocalDateTime startTime, boolean result, String extendParams) {
        // 记录条件评估结束时间
        LocalDateTime endTime = LocalDateTime.now();
        // 构造备注信息
        String remark = "Condition UUID：【" + condition.getUuid() + "】, Result: " + result;
        // 发布条件评估日志事件
        executionLogEventPublisher.publishConditionExecutionLog(context.getRuleExecutionId(), condition.getUuid(), ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER, result, startTime, endTime, extendParams, remark);
    }


    /**
     * 选定参与条件评估的物模型数据(三级数据源):
     * <ol>
     *   <li><b>事件内值</b>:事件触发路径且条件命中事件设备(含"全部设备"范围),左值直接取消息,
     *       零缓存读;配了防抖走窗口计数器(达标按 first/last 取值,未达标返回 null 短路);</li>
     *   <li><b>最新快照</b>:定时/API 触发或跨设备条件,读事件消费时维护的 latest 快照;</li>
     *   <li><b>数据收集池</b>:快照 miss 时的过渡期兜底(升级初期快照尚未积累),池下线后删除此支。</li>
     * </ol>
     *
     * @return 选中的物模型数据;防抖未达标/无数据返回 null
     */
    private ProductResultVO resolveEvaluationSource(PolicyContext context,
                                                    Optional<AntiShakeSchemePolicyDTO> optionalAntiShakeSchemePolicyDTO,
                                                    DevicePropertiesConditionDTO condition) {
        DevicePropertiesLeftParamDTO leftParam = condition.getLeftParam();
        String productIdentification = leftParam.getProductIdentification();
        String deviceIdentification = leftParam.getDeviceIdentification();
        TriggerEventDTO evt = context.getTriggerEvent();

        boolean eventScoped = evt != null
                && Objects.equals(evt.getProductIdentification(), productIdentification)
                && (isAllDeviceScope(deviceIdentification)
                || Objects.equals(evt.getDeviceIdentification(), deviceIdentification));
        if (eventScoped && evt.getThingModel() instanceof ProductResultVO current) {
            if (optionalAntiShakeSchemePolicyDTO.isEmpty()) {
                return current;
            }
            AntiShakeSchemePolicyDTO policy = optionalAntiShakeSchemePolicyDTO.get();
            // 窗口下限 1 秒:配置缺失/0/负值兜底,防止 TTL=0 导致计数键即写即失效、门槛永远达不到
            long windowSeconds = policy.getFrequency() == null || policy.getFrequency().getTimeValue() == null
                    ? 1L : Math.max(1L, policy.getFrequency().getTimeValue().longValue());
            // "全部设备"条件的防抖按事件设备维度隔离,各设备独立计数
            String evalDevice = isAllDeviceScope(deviceIdentification)
                    ? evt.getDeviceIdentification() : deviceIdentification;
            return antiShakeCounterService.countAndSelect(
                            RuleTriggerCacheKeys.antiShakeCounter(condition.getUuid(), evalDevice, windowSeconds),
                            RuleTriggerCacheKeys.antiShakeFirst(condition.getUuid(), evalDevice, windowSeconds),
                            policy, evt.getRawMessage())
                    .map(payload -> JSON.parseObject(payload, ProductResultVO.class))
                    .orElse(null);
        }

        // 定时/API 触发或跨设备条件:读最新快照(事件消费时维护);
        // "全部设备"条件无具体设备可读、快照未积累时返回 null → 条件按 false 处理
        if (StrUtil.isNotBlank(deviceIdentification) && !isAllDeviceScope(deviceIdentification)) {
            Optional<String> latest = deviceLatestSnapshotService
                    .getLatestPayload(productIdentification, deviceIdentification);
            if (latest.isPresent()) {
                return JSON.parseObject(latest.get(), ProductResultVO.class);
            }
        }
        return null;
    }

    private boolean isAllDeviceScope(String deviceIdentification) {
        return StrUtil.equals(BizConstant.ALL, deviceIdentification);
    }

    /**
     * 从选定的物模型数据中按 (serviceCode, propertyCode) 抽取左值。
     *
     * @param selectedProductResult 选定的物模型数据
     * @param leftParam             左参数定义
     * @return {@link FieldValueWithType};未匹配返回 null
     */
    private FieldValueWithType extractFieldValue(ProductResultVO selectedProductResult,
                                                 DevicePropertiesLeftParamDTO leftParam) {
        try {
            return selectedProductResult.getServices().stream()
                    .filter(serviceResult -> Objects.equals(serviceResult.getServiceCode(), leftParam.getServiceCode()))
                    .flatMap(serviceResult -> serviceResult.getProperties().stream())
                    .filter(propertyResult -> Objects.equals(propertyResult.getPropertyCode(), leftParam.getField()))
                    .findFirst()
                    .map(this::safelyGetPropertyValue)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error extracting left value: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * 从 ProductPropertyResultVO 获取属性值。
     *
     * @param propertyResult 属性结果对象
     * @return {@link FieldValueWithType} 属性类型和值
     */
    private FieldValueWithType safelyGetPropertyValue(ProductPropertyResultVO propertyResult) {
        try {
            if (null != propertyResult && null != propertyResult.getPropertyValue()) {
                String valueAsString = String.valueOf(propertyResult.getPropertyValue());
                TdDataTypeEnum dataTypeEnum = TdDataTypeEnum.valueOfByDataType(propertyResult.getDatatype());
                Object convertedValue = TdDataTypeEnum.convertValue(valueAsString, dataTypeEnum);
                return new FieldValueWithType(dataTypeEnum, convertedValue);
            } else {
                // 如果propertyValue不是字符串，直接返回原始值和类型
                return propertyResult != null ? new FieldValueWithType(TdDataTypeEnum.NCHAR, propertyResult.getPropertyValue()) : null;
            }
        } catch (Exception e) {
            log.error("Error converting property value: {}", e.getMessage(), e);
            return null;
        }
    }

}
