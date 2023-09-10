package com.mqttsnet.thinglinks.rule.common.consumer.kafka;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.kafka.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.rule.service.RuleDeviceLinkageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Description: 规则引擎-触发器规则消息消费（kafka模式）
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/10/28$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2022/10/28$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
//@Component
public class RuleTriggerMessageKafkaConsumer {

    @Autowired
    private RuleDeviceLinkageService ruleDeviceLinkageService;

    @Async("ruleAsync")
    @KafkaListener(topics = {ConsumerTopicConstant.THINGLINKS_RULE_TRIGGER})
    public void onMessage(ConsumerRecord<?, ?> record) {
        if (null == record) {
            log.warn("message cannot be empty {}", record);
            return;
        }

        log.info("规则引擎-触发器规则数据消费-->Received message={}", record);

        try {

            Object message = JSONObject.parse(String.valueOf(record.value()));
            JSONObject json = JSONObject.parseObject(message.toString());
            Boolean flag = ruleDeviceLinkageService.checkRuleConditions(json.getString("msg"));
            log.info("规则匹配结果:{}", flag);
            //触发执行动作
            if(flag) {
                ruleDeviceLinkageService.execAction(json.getString("msg"));
            }

        } catch (Exception e) {
            log.error("规则引擎-触发器规则数据消费-->消费失败，失败原因：{}", e.getMessage());
        }
    }
}
