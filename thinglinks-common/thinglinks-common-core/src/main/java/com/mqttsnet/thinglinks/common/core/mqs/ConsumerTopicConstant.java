package com.mqttsnet.thinglinks.common.core.mqs;

/**
 * @Description: 消费者主题常量（队列）
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/15$ 15:53$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/15$ 15:53$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
public interface ConsumerTopicConstant {

    /**
     * MQTT Broker 监听主题
     */
    interface Mqtt {
        /**
         * MQTT设备消息监听主题——》MQTT消息——》MQS
         */
        String THINGLINKS_MQS_MQTT_MSG = "thinglinks-pro-mqs-mqttMsg";

        /**
         * 设备上线
         */
        String THINGLINKS_CLIENT_CONNECTED_TOPIC = "client.connected.topic";

        /**
         * 客户端设备离线
         */
        String THINGLINKS_CLIENT_DISCONNECTED_TOPIC = "client.disconnect.topic";

        /**
         * 服务端设备离线
         */
        String THINGLINKS_SERVER_CONNECTED_TOPIC = "server.disconnect.topic";

        /**
         * 设备离线
         */
        String THINGLINKS_DEVICE_KICKED_TOPIC = "device.kicked.topic";

        /**
         * 消息订阅
         */
        String THINGLINKS_SUBSCRIPTION_ACKED_TOPIC = "subscription.acked.topic";

        /**
         * 取消订阅
         */
        String THINGLINKS_UNSUBSCRIPTION_ACKED_TOPIC = "unsubscription.acked.topic";

        /**
         * 消息分发错误
         */
        String THINGLINKS_DISTRIBUTION_ERROR_TOPIC = "distribution.error.topic";

        /**
         * 消息分发
         */
        String THINGLINKS_DISTRIBUTION_COMPLETED_TOPIC = "distribution.completed.topic";


        /**
         * PING 请求
         */
        String THINGLINKS_PING_REQ_TOPIC = "ping.req.topic";

    }

    interface Link {

        /**
         * 产品服务
         */
        String THINGLINKS_PRO_PRODUCT_SERVICE_MSG = "thinglinks-pro-product-service-msg";

        /**
         * 产品服务属性
         */
        String THINGLINKS_PRO_PRODUCT_PROPERTY_MSG = "thinglinks-pro-product-property-msg";
    }

    interface Rule {

        /**
         * 规则引擎触发器规则动作监听主题
         */
        String THINGLINKS_RULE_TRIGGER = "thinglinks_rule_trigger";
    }

    interface Tdengine {

        /**
         * TDengine超级表创键修改动作监听主题
         */
        String PRODUCTSUPERTABLE_CREATEORUPDATE = "productSuperTable-createOrUpdate";
    }

}
