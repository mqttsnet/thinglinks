package com.mqttsnet.thinglinks.collection.common.mq;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 采集数据生产
 */
@Slf4j
@Component
public class CollectionProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void senJsonObject(String topic, String json) {
        log.info(DateUtils.formatYYYY_MM_DD_HH_MM_SS(LocalDateTime.now()) + ":MQ生产消息开始");
        rocketMQTemplate.convertAndSend(topic, json);
        log.info(DateUtils.formatYYYY_MM_DD_HH_MM_SS(LocalDateTime.now()) + ":MQ生产消息结束");
    }

}
