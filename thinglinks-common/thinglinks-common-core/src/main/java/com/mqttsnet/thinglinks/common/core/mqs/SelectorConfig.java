package com.mqttsnet.thinglinks.common.core.mqs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class SelectorConfig {

    /*
    * 用于动态加载kafka rockermq bean的环境变量
    * */
    public static String selectorKafkaKey = "mqs.selector-kafka";

    /*
    * true 默认值，kafka
    * false  reockermq
    *
    * */
    @Value("${mqs.selector-kafka}")
    private boolean selectorKafka;

}
