package net.mqtts.link.common.enums.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import net.mqtts.common.log.annotation.Log;
import net.mqtts.common.log.enums.BusinessType;
import net.mqtts.link.service.device.MqttsDeviceActionService;
import net.mqtts.link.service.device.MqttsDeviceDatasService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "mqtts", topic = "mqtts")
public class MqttsDeviceActionMessageConsumer implements RocketMQListener {
    @Autowired
    private MqttsDeviceActionService mqttsDeviceActionService;
    @Autowired
    private MqttsDeviceDatasService mqttsDeviceDatasService;

    @Override
    public void onMessage(Object message) {
        assert message!=null;
        System.out.println("Link消费消息"+message);
        JSONObject mqttsMessage = JSONObject.parseObject((String) message);
        /**
         * TODO 设备上下线处理
         * $event/close	设备断开事件
         * $event/connect	设备连接事件
         * ${topic}  其他为业务数据自行处理
         */
        if("$event/connect".equals(mqttsMessage.get("topic"))){
            mqttsDeviceActionService.connectEvent(mqttsMessage.get("msg").toString());
        }else if("$event/close".equals(mqttsMessage.get("topic"))){
            mqttsDeviceActionService.closeEvent(mqttsMessage.get("msg").toString());
        }else {
            mqttsDeviceDatasService.insertBaseDatas(mqttsMessage.get("msg").toString());
        }
    }
}
