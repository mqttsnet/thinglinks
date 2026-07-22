package com.mqttsnet.thinglinks.service.linkage;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.linkage.Rule;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.save.linkage.RuleSaveVO;
import com.mqttsnet.thinglinks.vo.update.linkage.RuleUpdateVO;


/**
 * <p>
 * 业务接口
 * 规则信息
 * </p>
 *
 * @author mqttsnet
 * @date 2023-07-19 23:20:14
 * @create [2023-07-19 23:20:14] [mqttsnet]
 */
public interface RuleService extends SuperService<Long, Rule> {

    /**
     * 保存规则信息
     *
     * @param saveVO
     * @return
     */
    Rule saveRule(RuleSaveVO saveVO);

    /**
     * 修改规则信息
     *
     * @param updateVO
     * @return
     */
    Rule updateRule(RuleUpdateVO updateVO);

    /**
     * 删除规则信息
     *
     * @param id
     * @return
     */
    Boolean deleteRule(Long id);


    /**
     * 根据规则ID更新规则状态
     *
     * @param id     规则ID
     * @param status 规则状态
     * @return {@link Boolean} 更新结果
     */
    Boolean updateRuleStatus(Long id, Integer status);

    /**
     * 根据规则ID获取规则详情
     *
     * @param id 规则ID
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    RuleDetailsResultVO getRuleDetails(Long id);


    /**
     * 根据规则标识获取规则详情
     *
     * @param ruleIdentification 规则标识
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    RuleDetailsResultVO getRuleDetailsByIdentification(String ruleIdentification);

    /**
     * 触发规则策略
     *
     * @param tenantId           租户ID
     * @param ruleIdentification 规则标识
     * @return {@link RuleDetailsResultVO} 规则详情
     */
    RuleDetailsResultVO triggerRulePolicy(Long tenantId, String ruleIdentification);

    /**
     * 设备事件驱动触发规则策略(实时路径)。
     * <p>与 {@link #triggerRulePolicy} 的差异:规则详情走缓存(事件高频,避免每条消息查 DB);
     * 条件评估只针对与事件匹配的条件类型;左值优先取事件消息内值。
     *
     * @param tenantId             租户ID
     * @param ruleIdentification   规则标识
     * @param triggerConditionType 事件对应的条件类型(ConditionTypeEnum.value)
     * @param triggerEvent         触发事件上下文(消息内值)
     */
    void triggerRulePolicyForEvent(Long tenantId, String ruleIdentification,
                                   Integer triggerConditionType,
                                   com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO triggerEvent);
}


