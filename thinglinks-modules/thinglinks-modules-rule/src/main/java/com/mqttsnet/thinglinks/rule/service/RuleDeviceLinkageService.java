package com.mqttsnet.thinglinks.rule.service;

/**
 * @program: thinglinks
 * @description: 规则设备联动业务层接口
 * @packagename: com.mqttsnet.thinglinks.rule.service
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-11-03 18:44
 **/
public interface RuleDeviceLinkageService {

    /**
     * 触发设备联动规则条件
     * @param ruleIdentification 规则标识
     * @return
     */
    void triggerDeviceLinkageByRuleIdentification(String ruleIdentification);

    /**
     * 规则触发条件验证
     * @param ruleIdentification 规则标识
     * @return
     */
    Boolean checkRuleConditions(String ruleIdentification);
}
