package com.mqttsnet.thinglinks.service.linkage.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.common.cache.rule.trigger.RuleTriggerCacheKeys;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionActionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RuleConditionPolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import com.mqttsnet.thinglinks.dto.linkage.execution.PolicyContext;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.entity.linkage.Rule;
import com.mqttsnet.thinglinks.enumeration.linkage.RuleStatusEnum;
import com.mqttsnet.thinglinks.manager.linkage.RuleManager;
import com.mqttsnet.thinglinks.service.execution.service.RuleExecutionService;
import com.mqttsnet.thinglinks.service.execution.trigger.RuleEffectiveWindowChecker;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionActionService;
import com.mqttsnet.thinglinks.service.linkage.support.RuleTimingJobSynchronizer;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionService;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionActionPageQuery;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionActionDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
 * 规则信息
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:20:14
 * @create [2023-07-19 23:20:14] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class RuleServiceImpl extends SuperServiceImpl<RuleManager, Long, Rule> implements RuleService {

    private final RuleConditionService ruleConditionService;

    private final RuleConditionActionService ruleConditionActionService;

    private final RuleTimingJobSynchronizer ruleTimingJobSynchronizer;

    private final RuleExecutionService ruleExecutionService;

    private final CachePlusOps cachePlusOps;

    /**
     * Saves the rule information.
     *
     * @param saveVO The value object containing details for saving.
     * @return The saved rule.
     */
    @Override
    public Rule saveRule(RuleSaveVO saveVO) {
        log.info("saveRule saveVO:{}", saveVO);

        checkedRuleSaveVO(saveVO);
        Rule rule = builderRuleSaveVO(saveVO);

        // 新建规则不注册调度任务:任务只服务"定时触发"条件,由条件保存时经 RuleTimingJobSynchronizer 对账
        saveRuleToSuperManager(rule);

        return rule;
    }

    /**
     * Saves the rule to the super manager.
     *
     * @param rule The rule to be saved.
     * @return true if the rule was saved successfully, false otherwise.
     */
    private boolean saveRuleToSuperManager(Rule rule) {
        try {
            return superManager.save(rule);
        } catch (Exception e) {
            log.error("Failed to save rule: {}", e.getMessage(), e);
            throw BizException.wrap("Failed to save rule");
        }
    }

    /**
     * Updates the rule based on the provided updateVO.
     *
     * @param updateVO The value object containing update details.
     * @return The updated rule.
     */
    @Override
    public Rule updateRule(RuleUpdateVO updateVO) {
        log.info("updateRule updateVO:{}", updateVO);

        // Validate updateVO.
        checkedRuleUpdateVO(updateVO);

        // Load the existing rule from the database or cache.
        Rule existingRule = Optional.ofNullable(superManager.getById(updateVO.getId()))
                .orElseThrow(() -> BizException.wrap("The rule does not exist"));

        // Map values from updateVO to the existingRule.
        updateRulePropertiesFromVO(existingRule, updateVO);

        // Save the updated rule.
        superManager.updateById(existingRule);

        // 按最新条件集对账定时任务(频率/状态变化统一由同步器重建,非定时规则无任务)
        ruleTimingJobSynchronizer.sync(existingRule);

        return existingRule;
    }

    /**
     * Updates rule properties from the update value object.
     *
     * @param existingRule The existing rule.
     * @param updateVO     The value object containing update details.
     */
    private void updateRulePropertiesFromVO(Rule existingRule, RuleUpdateVO updateVO) {
        BeanPlusUtil.copyProperties(updateVO, existingRule);
    }


    /**
     * 删除规则信息
     *
     * @param id 规则ID
     * @return {@link Boolean} 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteRule(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");

        Rule rule = superManager.getById(id);
        if (null == rule) {
            throw BizException.wrap("The Rule does not exist");
        }

        // 清理关联的定时任务(如有)
        ruleTimingJobSynchronizer.removeOnDelete(rule);
        return superManager.removeById(id);
    }


    /**
     * 根据规则ID更新规则状态
     *
     * @param id     设备ID
     * @param status 规则状态
     * @return {@link Boolean} 更新结果
     */
    @Override
    public Boolean updateRuleStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ArgumentAssert.notNull(status, "status Cannot be null");
        if (!RuleStatusEnum.STATE_COLLECTION.contains(status)) {
            throw BizException.wrap("Status is not exist");
        }
        Rule rule = superManager.getById(id);
        if (null == rule) {
            throw BizException.wrap("The rule does not exist");
        }
        if (Objects.equals(status, rule.getStatus())) {
            return true;
        }
        rule.setStatus(status);
        boolean updated = superManager.updateById(rule);
        if (updated) {
            // 启停切换后对账定时任务(定时规则跟随启停,非定时规则本就无任务)
            ruleTimingJobSynchronizer.sync(rule);
        }
        return updated;
    }

    /**
     * 根据规则ID获取规则详情
     *
     * @param id 规则ID
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    @Override
    public RuleDetailsResultVO getRuleDetails(Long id) {
        RuleDetailsResultVO ruleDetails = Optional.ofNullable(superManager.getById(id))
                .map(rule -> BeanPlusUtil.toBeanIgnoreError(rule, RuleDetailsResultVO.class))
                .orElseThrow(() -> new BizException("Rule not found"));


        RuleConditionPageQuery ruleConditionPageQuery = new RuleConditionPageQuery();
        ruleConditionPageQuery.setRuleId(id);
        List<RuleConditionDetailsResultVO> conditions = Optional.ofNullable(ruleConditionService.getRuleConditionList(ruleConditionPageQuery))
                .orElse(Collections.emptyList())
                .stream()
                .map(condition -> {
                    RuleConditionDetailsResultVO conditionDetails = BeanPlusUtil.toBeanIgnoreError(condition, RuleConditionDetailsResultVO.class);
                    RuleConditionActionPageQuery ruleConditionActionPageQuery = new RuleConditionActionPageQuery();
                    ruleConditionActionPageQuery.setRuleConditionId(condition.getId());
                    List<RuleConditionActionDetailsResultVO> actions = Optional.ofNullable(ruleConditionActionService.getRuleConditionActionList(ruleConditionActionPageQuery))
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(action -> BeanPlusUtil.toBeanIgnoreError(action, RuleConditionActionDetailsResultVO.class))
                            .collect(Collectors.toList());
                    conditionDetails.setConditionActionDetailsResultVOS(actions);
                    return conditionDetails;
                })
                .collect(Collectors.toList());

        ruleDetails.setConditionDetailsResultVOS(conditions);
        return ruleDetails;
    }


    /**
     * 根据规则标识获取规则详情
     *
     * @param ruleIdentification 规则标识
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    @Override
    public RuleDetailsResultVO getRuleDetailsByIdentification(String ruleIdentification) {
        RuleDetailsResultVO ruleDetails = Optional.ofNullable(superManager.findOneByRuleIdentification(ruleIdentification))
                .map(rule -> BeanPlusUtil.toBeanIgnoreError(rule, RuleDetailsResultVO.class))
                .orElseThrow(() -> new BizException("Rule not found"));

        RuleConditionPageQuery ruleConditionPageQuery = new RuleConditionPageQuery();
        ruleConditionPageQuery.setRuleId(ruleDetails.getId());
        List<RuleConditionDetailsResultVO> conditions = Optional.ofNullable(ruleConditionService.getRuleConditionList(ruleConditionPageQuery))
                .orElse(Collections.emptyList())
                .stream()
                .map(condition -> {
                    RuleConditionDetailsResultVO conditionDetails = BeanPlusUtil.toBeanIgnoreError(condition, RuleConditionDetailsResultVO.class);
                    RuleConditionActionPageQuery ruleConditionActionPageQuery = new RuleConditionActionPageQuery();
                    ruleConditionActionPageQuery.setRuleConditionId(condition.getId());
                    List<RuleConditionActionDetailsResultVO> actions = Optional.ofNullable(ruleConditionActionService.getRuleConditionActionList(ruleConditionActionPageQuery))
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(action -> BeanPlusUtil.toBeanIgnoreError(action, RuleConditionActionDetailsResultVO.class))
                            .collect(Collectors.toList());
                    conditionDetails.setConditionActionDetailsResultVOS(actions);
                    return conditionDetails;
                })
                .collect(Collectors.toList());

        ruleDetails.setConditionDetailsResultVOS(conditions);
        return ruleDetails;
    }


    /**
     * 触发规则策略
     *
     * @param tenantId           租户ID
     * @param ruleIdentification 规则标识
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    @Override
    public RuleDetailsResultVO triggerRulePolicy(Long tenantId, String ruleIdentification) {
        return doTriggerRulePolicy(tenantId, ruleIdentification);
    }

    private RuleDetailsResultVO doTriggerRulePolicy(Long tenantId, String ruleIdentification) {
        try {
            // Retrieve rule details based on the rule identification
            RuleDetailsResultVO ruleDetails = getRuleDetailsByIdentification(ruleIdentification);

            if (Objects.isNull(ruleDetails)) {
                throw BizException.wrap("ruleIdentification is not exist");
            }
            if (!Objects.equals(ruleDetails.getStatus(), RuleStatusEnum.ACTIVATED.getValue())) {
                log.info("[RuleTrigger] rule disabled, skip timing trigger. rule={}", ruleIdentification);
                return ruleDetails;
            }

            // Set tenant ID in policy context
            PolicyContext policyContext = new PolicyContext();
            policyContext.setRuleExecutionId(Long.valueOf(SnowflakeIdUtil.nextId()));
            policyContext.setRuleIdentification(ruleIdentification);
            policyContext.setRuleName(ruleDetails.getRuleName());
            policyContext.setTenantId(tenantId.toString());

            // Convert rule details to policy DTO
            RulePolicyDTO rulePolicyDTO = convertToRulePolicyDTO(ruleDetails);

            // Execute the policy
            policyContext.setRulePolicyDTO(rulePolicyDTO);
            ruleExecutionService.executePolicy(policyContext);

            return ruleDetails;
        } catch (Exception e) {
            log.warn("Error executing rule policy for Tenant ID: {}, Rule Identification: {}", tenantId, ruleIdentification, e);
            throw BizException.wrap("Failed to execute rule policy", e);
        }
    }

    @Override
    public void triggerRulePolicyForEvent(Long tenantId, String ruleIdentification,
                                          Integer triggerConditionType, TriggerEventDTO triggerEvent) {
        if (tenantId == null || StringUtils.isBlank(ruleIdentification)) {
            log.warn("[RuleTrigger] tenantId/ruleIdentification missing, skip. tenantId={} rule={}",
                    tenantId, ruleIdentification);
            return;
        }
        try {
            // 事件路径高频:规则详情走缓存(TTL 见 RuleTriggerCacheKeys.DETAILS_TTL),miss 回源 DB
            RuleDetailsResultVO ruleDetails = getRuleDetailsWithCache(ruleIdentification);
            if (Objects.isNull(ruleDetails)) {
                log.warn("[RuleTrigger] rule not found, skip. ruleIdentification={}", ruleIdentification);
                return;
            }
            if (!Objects.equals(ruleDetails.getStatus(), RuleStatusEnum.ACTIVATED.getValue())) {
                log.info("[RuleTrigger] rule disabled, skip event trigger. rule={}", ruleIdentification);
                return;
            }
            // 生效时间窗校验(effectiveType=指定时间的 week/timeframe 窗口)
            if (!RuleEffectiveWindowChecker.isEffectiveNow(ruleDetails.getEffectiveType(), ruleDetails.getAppointContent())) {
                log.info("[RuleTrigger] out of effective window, skip. rule={}", ruleIdentification);
                return;
            }

            PolicyContext policyContext = new PolicyContext();
            policyContext.setRuleExecutionId(Long.valueOf(SnowflakeIdUtil.nextId()));
            policyContext.setRuleIdentification(ruleIdentification);
            policyContext.setRuleName(ruleDetails.getRuleName());
            policyContext.setTenantId(tenantId.toString());
            policyContext.setTriggerConditionType(triggerConditionType);
            policyContext.setTriggerEvent(triggerEvent);
            policyContext.setRulePolicyDTO(convertToRulePolicyDTO(ruleDetails));

            ruleExecutionService.executePolicy(policyContext);
        } catch (Exception e) {
            // 事件路径不抛出:单规则失败不影响同事件的其它规则,也不触发 MQ 重试(动作重放风险大于漏评估)
            log.error("[RuleTrigger] event-driven execution failed rule={} err={}", ruleIdentification, e.getMessage(), e);
        }
    }

    /**
     * 规则详情缓存读 ── 事件路径每条消息×每条命中规则调用一次,不能次次查 DB。
     * cacheNullValues=true:规则已删但索引桶未过期的窗口期内,"查无此规则"的结论也缓存,防止事件风暴打穿 DB。
     */
    private RuleDetailsResultVO getRuleDetailsWithCache(String ruleIdentification) {
        CacheResult<RuleDetailsResultVO> result = cachePlusOps.get(
                RuleTriggerCacheKeys.ruleDetails(ruleIdentification),
                k -> {
                    try {
                        return getRuleDetailsByIdentification(ruleIdentification);
                    } catch (Exception e) {
                        // 查无此规则时底层抛 BizException:转 null 让空值缓存生效,而非每条消息都回源报错
                        log.warn("[RuleTrigger] rule details load failed rule={} err={}",
                                ruleIdentification, e.getMessage());
                        return null;
                    }
                }, true);
        return result == null ? null : result.getValue();
    }

    private RulePolicyDTO convertToRulePolicyDTO(RuleDetailsResultVO ruleDetails) {
        RulePolicyDTO rulePolicyDTO = BeanPlusUtil.toBeanIgnoreError(ruleDetails, RulePolicyDTO.class);
        List<RuleConditionPolicyDTO> conditionPolicyDTOs = ruleDetails.getConditionDetailsResultVOS().stream()
                .map(conditionDetails -> {
                    RuleConditionPolicyDTO conditionPolicyDTO = BeanPlusUtil.toBeanIgnoreError(conditionDetails, RuleConditionPolicyDTO.class);
                    List<RuleConditionActionPolicyDTO> actionPolicyDTOs = conditionDetails.getConditionActionDetailsResultVOS().stream()
                            .map(actionDetails -> BeanPlusUtil.toBeanIgnoreError(actionDetails, RuleConditionActionPolicyDTO.class))
                            .collect(Collectors.toList());
                    conditionPolicyDTO.setConditionActionPolicyDTOS(actionPolicyDTOs);
                    return conditionPolicyDTO;
                })
                .collect(Collectors.toList());
        rulePolicyDTO.setRuleConditionPolicyDTOS(conditionPolicyDTOs);
        return rulePolicyDTO;
    }

    /**
     * 新增 校验参数
     *
     * @param saveVO
     */
    private void checkedRuleSaveVO(RuleSaveVO saveVO) {

        ArgumentAssert.notBlank(saveVO.getAppId(), "AppId Cannot be null");
        //规则信息状态
        ArgumentAssert.notNull(saveVO.getStatus(), "Status Cannot be null");
        if (!RuleStatusEnum.STATE_COLLECTION.contains(saveVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }
    }

    /**
     * 新增 构建参数
     *
     * @param saveVO
     * @return
     */
    private Rule builderRuleSaveVO(RuleSaveVO saveVO) {
        Rule rule = BeanUtil.toBeanIgnoreError(saveVO, Rule.class);
        rule.setRuleIdentification(String.valueOf(SnowflakeIdUtil.nextId()));
        rule.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return rule;
    }

    /**
     * 修改 校验参数
     *
     * @param updateVO
     */
    private void checkedRuleUpdateVO(RuleUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");
        //产品模型状态
        ArgumentAssert.notNull(updateVO.getStatus(), "Status Cannot be null");
        if (!RuleStatusEnum.STATE_COLLECTION.contains(updateVO.getStatus())) {
            throw BizException.wrap("Status is not exist");
        }
    }

}
