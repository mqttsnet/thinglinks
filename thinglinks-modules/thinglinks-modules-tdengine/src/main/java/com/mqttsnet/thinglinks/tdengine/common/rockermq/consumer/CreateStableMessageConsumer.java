package com.mqttsnet.thinglinks.tdengine.common.rockermq.consumer;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @Description: TDengine超级表创键监听（Rocketmq模式）
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/22$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/22$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "thinglinks-tdengine", topic = "create-stable")
public class CreateStableMessageConsumer implements RocketMQListener {

    @Override
    public void onMessage(Object message) {
        assert message!=null;
        System.out.println("TDengine消费创键超级表消息"+message);
        JSONObject stableMessage = JSONObject.parseObject(String.valueOf(message));

    }
}
