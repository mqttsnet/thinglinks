package net.mqtts.link.common.enums.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @Description: Mqtt动作消息消费（Rocketmq模式）
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/22$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/22$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "mqtts", topic = "mqtts")
public class MqttsDeviceActionMessageConsumer implements RocketMQListener {

    @Override
    public void onMessage(Object message) {
        assert message!=null;

        System.out.println("Link消费消息"+message);
    }
}
