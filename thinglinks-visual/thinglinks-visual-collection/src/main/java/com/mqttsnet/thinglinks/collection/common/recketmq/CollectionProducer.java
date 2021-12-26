package com.mqttsnet.thinglinks.collection.common.recketmq;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 采集数据生产
 */
@Slf4j
@Component
public class CollectionProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void senJsonObject(String topic, String json) {
        log.info(DateUtils.getTime() + ":MQ生产消息开始");
        rocketMQTemplate.convertAndSend(topic, json);
        log.info(DateUtils.getTime() + ":MQ生产消息结束");
    }

}
