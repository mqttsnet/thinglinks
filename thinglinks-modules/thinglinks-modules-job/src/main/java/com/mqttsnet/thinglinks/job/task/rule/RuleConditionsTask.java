package com.mqttsnet.thinglinks.job.task.rule;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

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

//    @Resource
//    private RemoteJobService remoteJobService;


    /**
     * 解析规则条件定时任务执行
     */
    public void parsingRuleConditions(String params) {
        StopWatch watch = new StopWatch();
        watch.start();
        if (log.isInfoEnabled()) {
            log.info("解析规则条件定时任务开始，参数：{}", params);
        }
        //TODO 解析规则条件定时任务执行处理
        watch.stop();
        if (log.isInfoEnabled()) {
            log.info("解析规则条件定时任务结束，耗时(millisecond)：{}", watch.getTime());
        }
    }
}
