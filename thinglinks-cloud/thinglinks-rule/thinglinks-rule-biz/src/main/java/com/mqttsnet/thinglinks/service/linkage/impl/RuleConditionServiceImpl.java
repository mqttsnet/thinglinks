package com.mqttsnet.thinglinks.service.linkage.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.condition.model.dto.ConditionInfoDTO;
import com.mqttsnet.basic.condition.model.dto.ConditionParamResult;
import com.mqttsnet.basic.condition.model.dto.SingleConditionDTO;
import com.mqttsnet.basic.condition.operator.ConditionOperator;
import com.mqttsnet.basic.condition.service.ConditionService;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import java.util.Objects;

import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.linkage.condition.group.DevicePropertiesConditionGroupDTO;
import com.mqttsnet.thinglinks.entity.linkage.RuleCondition;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleStatusEnum;
import com.mqttsnet.thinglinks.manager.linkage.RuleConditionManager;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionActionService;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionService;
import com.mqttsnet.thinglinks.service.linkage.support.RuleTimingJobSynchronizer;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionActionPageQuery;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionActionResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleConditionActionSaveVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleConditionSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleConditionActionUpdateVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleConditionUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 业务实现类
 * 规则条件表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:36:30
 * @create [2023-07-19 23:36:30] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class RuleConditionServiceImpl extends SuperServiceImpl<RuleConditionManager, Long, RuleCondition> implements RuleConditionService {

    private final ConditionService conditionService;

    private final RuleConditionActionService ruleConditionActionService;

    private final RuleTimingJobSynchronizer ruleTimingJobSynchronizer;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 保存规则条件信息
     *
     * @param saveVO
     * @return
     */
    @Override
    public RuleCondition saveRuleCondition(RuleConditionSaveVO saveVO) {
        log.info("saveRuleCondition saveVO:{}", saveVO);
        //校验参数
        checkedRuleConditionSaveVO(saveVO);
        //构建参数
        RuleCondition ruleCondition = builderRuleConditionSaveVO(saveVO);
        //更新
        superManager.save(ruleCondition);
        // 条件集变化后对账定时任务(只有定时触发条件的规则才挂 XXL-Job)
        ruleTimingJobSynchronizer.syncByRuleId(ruleCondition.getRuleId());
        return ruleCondition;
    }

    /**
     * 修改规则条件信息
     *
     * @param updateVO
     * @return
     */
    @Override
    public RuleCondition updateRuleCondition(RuleConditionUpdateVO updateVO) {
        log.info("updateRuleCondition updateVO:{}", updateVO);

        // 参数校验
        checkedRuleConditionUpdateVO(updateVO);

        // 获取原始RuleCondition
        RuleCondition originalRuleCondition = superManager.getById(updateVO.getId());

        // 断言验证
        ArgumentAssert.notNull(originalRuleCondition, "rule not found with id: " + updateVO.getId());

        // 使用BeanPlusUtil来转换更新的属性
        BeanPlusUtil.copyProperties(updateVO, originalRuleCondition);

        // 更新对象
        superManager.updateById(originalRuleCondition);

        // 条件类型/启停可能变化,对账定时任务
        ruleTimingJobSynchronizer.syncByRuleId(originalRuleCondition.getRuleId());

        return originalRuleCondition;
    }


    /**
     * 删除规则条件信息
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteRuleCondition(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        RuleCondition ruleCondition = superManager.getById(id);
        if (null == ruleCondition) {
            throw BizException.wrap("The RuleCondition does not exist");
        }
        Boolean removed = superManager.removeById(id);
        // 删除定时条件后规则可能不再需要调度任务
        ruleTimingJobSynchronizer.syncByRuleId(ruleCondition.getRuleId());
        return removed;
    }

    /**
     * Retrieve all available condition operators.
     *
     * @return A list of all condition operators.
     */
    @Override
    public List<ConditionOperator> getAllOperator() {
        return conditionService.getAllOperator();
    }

    /**
     * Retrieve all available condition operator connectors.
     *
     * @return A list of all condition operator connectors.
     */
    @Override
    public List<ConditionOperator> getAllOperatorConnect() {
        return conditionService.getAllOperatorConnect();
    }

    /**
     * Validate the provided conditions.
     *
     * @param condition List of conditions to validate.
     * @return `true` if validation is successful, otherwise an exception will be thrown.
     */
    @Override
    public boolean check(List<ConditionInfoDTO> condition) {
        return conditionService.check(condition);
    }

    /**
     * Flatten the conditions to get only single conditions.
     *
     * @param conditionInfos List of condition info.
     * @return A list of flattened single conditions.
     */
    @Override
    public List<SingleConditionDTO> selectSingleCondition(List<ConditionInfoDTO> conditionInfos) {
        return conditionService.selectSingleCondition(conditionInfos);
    }


    /**
     * Extract variable parameters from the conditions.
     *
     * @param conditionInfos List of condition info.
     * @return A list of extracted variable parameters.
     */
    @Override
    public List<ConditionParamResult> selectVariableParam(List<ConditionInfoDTO> conditionInfos) {
        return conditionService.selectVariableParam(conditionInfos);
    }


    /**
     * Determine if the provided conditions are of single condition structure.
     *
     * @param conditionInfos List of condition info.
     * @return `true` if it's a single condition, `false` otherwise.
     */
    @Override
    public boolean isSingleCondition(List<ConditionInfoDTO> conditionInfos) {
        return conditionService.isSingleCondition(conditionInfos);
    }

    /**
     * Translate the provided conditions into a human-readable format.
     *
     * @param condition List of conditions.
     * @return A string representation of the conditions in a human-readable format.
     */
    @Override
    public String transfer(List<ConditionInfoDTO> condition) {
        return conditionService.transfer(condition);
    }

    /**
     * Saves the rule condition action.
     *
     * @param saveVO Data object containing the rule conditions to save.
     * @return Returns the result of the saved rule condition action.
     */
    @Override
    public RuleConditionResultVO saveRuleConditionAction(RuleConditionSaveVO saveVO) {

        // save rule condition
        RuleCondition ruleCondition = saveRuleCondition(saveVO);

        List<RuleConditionActionSaveVO> conditionActionSaveVOS = Optional.ofNullable(saveVO.getConditionActionSaveVOS())
                .filter(actionSaveVOS -> !actionSaveVOS.isEmpty())
                .orElseThrow(() -> new BizException("The RuleConditionActionSaveVO does not exist"));

        RuleCondition condition = Optional.ofNullable(superManager.findOneByConditionIdentification(ruleCondition.getConditionIdentification()))
                .orElseThrow(() -> new BizException("RuleCondition with given identification not found"));

        // save rule condition action
        conditionActionSaveVOS.forEach(conditionActionSaveVO -> {
            conditionActionSaveVO.setRuleConditionId(condition.getId());
            ruleConditionActionService.saveRuleConditionAction(conditionActionSaveVO);
        });

        return BeanPlusUtil.toBeanIgnoreError(condition, RuleConditionResultVO.class);
    }

    /**
     * Updates the rule condition action.
     *
     * @param updateVO Data object containing the rule conditions to update.
     * @return Returns the result of the updated rule condition action.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleConditionResultVO updateRuleConditionAction(RuleConditionUpdateVO updateVO) {

        // Fetch and validate the RuleCondition to be updated
        RuleCondition existingCondition = Optional.ofNullable(superManager.getById(updateVO.getId()))
                .orElseThrow(() -> new BizException("RuleCondition with given identification not found"));

        // Update the RuleCondition
        RuleCondition updatedRuleCondition = updateRuleCondition(updateVO);

        // Process RuleConditionActions
        Optional.ofNullable(updateVO.getConditionActionUpdateVOS())
                .filter(actionUpdateVOS -> !actionUpdateVOS.isEmpty())
                .ifPresent(actionUpdateVOS -> processRuleConditionActions(updatedRuleCondition.getId(), actionUpdateVOS));

        return BeanPlusUtil.toBeanIgnoreError(updatedRuleCondition, RuleConditionResultVO.class);
    }

    /**
     * Retrieves a list of rule conditions based on the provided query criteria.
     *
     * @param query The query conditions encapsulated in {@link RuleConditionPageQuery}.
     * @return A list of {@link RuleConditionResultVO} entities that match the provided query.
     */
    @Override
    public List<RuleConditionResultVO> getRuleConditionList(RuleConditionPageQuery query) {
        List<RuleCondition> ruleConditions = superManager.getRuleConditionList(query);
        return BeanPlusUtil.toBeanList(ruleConditions, RuleConditionResultVO.class);
    }

    /**
     * Processes RuleCondition actions, including creation, update, and deletion.
     *
     * @param ruleConditionId ID of the rule condition to which the actions are associated.
     * @param actionUpdateVOS List of actions to be updated.
     */
    public void processRuleConditionActions(Long ruleConditionId, List<RuleConditionActionUpdateVO> actionUpdateVOS) {
        log.info("Processing RuleCondition actions for ruleConditionId:{} with actions:{}", ruleConditionId, actionUpdateVOS);

        RuleConditionActionPageQuery ruleConditionActionPageQuery = new RuleConditionActionPageQuery();
        ruleConditionActionPageQuery.setRuleConditionId(ruleConditionId);
        List<RuleConditionActionResultVO> existingActions = ruleConditionActionService.getRuleConditionActionList(ruleConditionActionPageQuery);

        // Update or create new actions based on the provided action updates
        actionUpdateVOS.forEach(actionUpdateVO -> {
            Optional<RuleConditionActionResultVO> optionalExistingAction = existingActions.stream()
                    .filter(action -> Objects.equals(action.getId(), actionUpdateVO.getId()))
                    .findFirst();

            if (optionalExistingAction.isPresent()) {
                ruleConditionActionService.updateRuleConditionAction(actionUpdateVO);
            } else {
                RuleConditionActionSaveVO saveVO = BeanPlusUtil.toBeanIgnoreError(actionUpdateVO, RuleConditionActionSaveVO.class);
                saveVO.setRuleConditionId(ruleConditionId);
                ruleConditionActionService.saveRuleConditionAction(saveVO);
            }
        });

        // Delete any existing actions that weren't included in the action updates
        existingActions.stream()
                .filter(existingAction -> actionUpdateVOS.stream().noneMatch(vo -> Objects.equals(vo.getId(), existingAction.getId())))
                .forEach(action -> ruleConditionActionService.deleteRuleConditionAction(action.getId()));
    }


    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedRuleConditionSaveVO(RuleConditionSaveVO saveVO) {

        ArgumentAssert.notNull(saveVO.getRuleId(), "RuleId Cannot be null");
        //规则信息状态
        ArgumentAssert.notNull(saveVO.getStatus(), "Status Cannot be null");
        if (!RuleStatusEnum.STATE_COLLECTION.contains(saveVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }

        List<DevicePropertiesConditionGroupDTO> devicePropertiesConditionGroupDTOS = parseConditionValue(saveVO.getConditionScheme());
        List<ConditionInfoDTO> conditionInfoDTOList = BeanPlusUtil.copyToList(devicePropertiesConditionGroupDTOS, ConditionInfoDTO.class);
        if (!conditionInfoDTOList.isEmpty()) {
            ArgumentAssert.isTrue(conditionService.check(conditionInfoDTOList));

        }
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private RuleCondition builderRuleConditionSaveVO(RuleConditionSaveVO saveVO) {
        RuleCondition ruleCondition = BeanPlusUtil.toBeanIgnoreError(saveVO, RuleCondition.class);
        ruleCondition.setConditionIdentification(String.valueOf(SnowflakeIdUtil.nextId()));
        ruleCondition.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return ruleCondition;
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedRuleConditionUpdateVO(RuleConditionUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        //规则信息状态
        ArgumentAssert.notNull(updateVO.getStatus(), "Status Cannot be null");
        if (!RuleStatusEnum.STATE_COLLECTION.contains(updateVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }

        List<DevicePropertiesConditionGroupDTO> devicePropertiesConditionGroupDTOS = parseConditionValue(updateVO.getConditionScheme());
        List<ConditionInfoDTO> conditionInfoDTOList = BeanPlusUtil.copyToList(devicePropertiesConditionGroupDTOS, ConditionInfoDTO.class);
        if (!conditionInfoDTOList.isEmpty()) {
            ArgumentAssert.isTrue(conditionService.check(conditionInfoDTOList));
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
            return objectMapper.readValue(conditionValue, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Error parsing the condition scheme string", e);
            throw BizException.wrap("Error parsing the condition scheme string");
        }
    }

}


