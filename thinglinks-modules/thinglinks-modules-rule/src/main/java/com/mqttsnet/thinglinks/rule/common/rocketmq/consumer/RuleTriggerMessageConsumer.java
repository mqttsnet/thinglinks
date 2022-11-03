package com.mqttsnet.thinglinks.rule.common.rocketmq.consumer;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.rule.api.RemoteRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Description: 规则引擎-触发器规则消息消费（Rocketmq模式）
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
@Component
@RocketMQMessageListener(consumerGroup = ConsumerGroupConstant.THINGLINKS_JOB_GROUP, topic = ConsumerTopicConstant.THINGLINKS_RULE_TRIGGER)
public class RuleTriggerMessageConsumer implements RocketMQListener {

    @Autowired
    private RemoteRuleService remoteRuleService;

    @Async("ruleAsync")
    @Override
    public void onMessage(Object message) {
        assert message != null : "message cannot be empty";
        log.info("规则引擎-触发器规则数据消费-->Received message={}", message);
        try {
            R<Boolean> data = remoteRuleService.checkRuleConditions(String.valueOf(message));
            Boolean flag = data.getData();
            log.info("告警结果:{}", flag);
        } catch (Exception e) {
            log.error("规则引擎-触发器规则数据消费-->消费失败，失败原因：{}", e.getMessage());
        }
    }
}
