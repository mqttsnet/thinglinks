package com.mqttsnet.thinglinks.monitor.config.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "recketmq")
public class MQConfig {

    /**
     * 系统数据主题
     */
    private String systemTopic;

}
