package com.mqttsnet.thinglinks.link.common.rockermq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerGroupConstant;
import lombok.extern.slf4j.Slf4j;
import com.mqttsnet.thinglinks.link.service.device.DeviceActionService;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: Mqtt动作消息消费（Rocketmq模式）
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
@RocketMQMessageListener(consumerGroup = ConsumerGroupConstant.THINGLINKS_GROUP, topic = "thinglinks-link")
public class DeviceActionMessageConsumer implements RocketMQListener {
    @Autowired
    private DeviceActionService deviceActionService;
    @Autowired
    private DeviceDatasService deviceDatasService;

    @Override
    public void onMessage(Object message) {
        assert message!=null:"message cannot be empty";
        log.info("ThingLinks物联网平台数据消费-->Received message={}", message);
        JSONObject thinglinksMessage = JSONObject.parseObject(String.valueOf(message));
        /**
         * TODO 设备上下线处理
         * $event/close	设备断开事件
         * $event/connect	设备连接事件
         * ${topic}  其他为业务数据自行处理
         */
        if("$event/connect".equals(thinglinksMessage.get("topic"))){
            deviceActionService.connectEvent(String.valueOf(thinglinksMessage.getString("msg")));
        }else if("$event/close".equals(thinglinksMessage.get("topic"))){
            deviceActionService.closeEvent(String.valueOf(thinglinksMessage.getString("msg")));
        }else {
            deviceDatasService.insertBaseDatas(thinglinksMessage);
        }
    }
}
