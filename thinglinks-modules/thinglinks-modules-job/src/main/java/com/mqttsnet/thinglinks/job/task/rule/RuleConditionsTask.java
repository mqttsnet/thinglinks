package com.mqttsnet.thinglinks.job.task.rule;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.common.rocketmq.domain.MQMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 解析规则条件定时任务执行
     */
    public void parsingRuleConditions(String params) {
        if(StringUtils.isBlank(params)){
            return;
        }
        MQMessage mqMessage = new MQMessage();
        mqMessage.setTopic(ConsumerTopicConstant.THINGLINKS_RULE_TRIGGER);
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", params);
        mqMessage.setMessage(jsonObject.toJSONString());
        rocketMQTemplate.convertAndSend(mqMessage.getTopic(), mqMessage.getMessage());
    }
}
