package com.mqttsnet.thinglinks.job.task.rule;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.rule.api.RemoteRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: thinglinks
 * @description: 规则条件定时任务
 * @packagename: com.mqttsnet.thinglinks.job.task.link
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-11 15:08
 **/
@Component("ruleConditionsTask")
@Slf4j
public class RuleConditionsTask {

    @Resource
    private RemoteRuleService remoteRuleService;


    /**
     * 解析规则条件定时任务执行
     */
    public void parsingRuleConditions(String params) {
        if(StringUtils.isBlank(params)){
            return;
        }
        remoteRuleService.triggerDeviceLinkage(params);
    }
}
