package com.mqttsnet.thinglinks.common.mq;

/**
 * @Description: 消费者组常量（队列）
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: https://mqttsnet.com
 * @CreateDate: 2022/4/15$ 15:53$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/15$ 15:53$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
public interface ConsumerGroupConstant {
    /**
     * Topic 与 Consumer Group 共用的小写命名空间，值由根目录产品清单生成。
     */
    String THINGLINKS_MQ_NAMESPACE = "thinglinks";

    /**
     * Consumer Group 使用的大写命名空间，由小写命名空间派生生成。
     */
    String THINGLINKS_MQ_NAMESPACE_UPPER = "THINGLINKS";

    /**
     * Kafka 与 RocketMQ 消费组共用的前缀，由大写命名空间派生并与业务用途后缀组合。
     */
    String THINGLINKS_CONSUMER_GROUP_PREFIX = "CID_" + THINGLINKS_MQ_NAMESPACE_UPPER + "_";

    /**
     * 默认消费者组常量，值指向小写命名空间。
     */
    String THINGLINKS_GROUP = THINGLINKS_MQ_NAMESPACE;
}
