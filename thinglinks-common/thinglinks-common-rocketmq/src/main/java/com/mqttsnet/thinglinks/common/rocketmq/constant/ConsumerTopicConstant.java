package com.mqttsnet.thinglinks.common.rocketmq.constant;

import lombok.Data;

/**
 * @Description: 消费者主题常量
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/15$ 15:53$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/15$ 15:53$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
public class ConsumerTopicConstant {

    /**
     * MQTT设备消息监听主题
     */
    public static final String THINGLINKS_LINK_MQTT_MSG = "thinglinks-link-mqttMsg";

    /**
     * TDengine超级表创键修改动作监听主题
     */
    public static final String PRODUCTSUPERTABLE_CREATEORUPDATE = "productSuperTable-createOrUpdate";

    /**
     * 系统指标数据采集动作监听主题
     */
    public static final String THINGLINKS_COLLECTION_SYSTEM = "thinglinks_collection_system";

    /**
     * 规则引擎触发器规则动作监听主题
     */
    public static final String THINGLINKS_RULE_TRIGGER = "thinglinks_rule_trigger";

}
