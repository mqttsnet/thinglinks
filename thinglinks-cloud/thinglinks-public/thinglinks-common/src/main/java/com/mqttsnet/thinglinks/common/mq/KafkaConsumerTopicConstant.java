package com.mqttsnet.thinglinks.common.mq;

/**
 * @Description: Kafka 消费者主题常量（队列）
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: <a href="https://mqttsnet.com">Official website</a>
 * @CreateDate: 2022/4/15$ 15:53$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/15$ 15:53$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
public interface KafkaConsumerTopicConstant {

    interface Mqs {

        /**
         * MQS MQTT Broker 监听主题
         */
        interface MqsMqtt {
            /**
             * 设备上线
             */
            String THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC = "mqtt.client.connected.topic";

            /**
             * 客户端设备离线
             */
            String THINGLINKS_MQTT_CLIENT_DISCONNECTED_TOPIC = "mqtt.client.disconnect.topic";

            /**
             * 服务端主动断开了与客户端的连接
             */
            String THINGLINKS_MQTT_SERVER_CONNECTED_TOPIC = "mqtt.server.disconnect.topic";

            /**
             * 设备离线
             */
            String THINGLINKS_MQTT_DEVICE_KICKED_TOPIC = "mqtt.device.kicked.topic";

            /**
             * 消息订阅
             */
            String THINGLINKS_MQTT_SUBSCRIPTION_ACKED_TOPIC = "mqtt.subscription.acked.topic";

            /**
             * 取消订阅
             */
            String THINGLINKS_MQTT_UNSUBSCRIPTION_ACKED_TOPIC = "mqtt.unsubscription.acked.topic";

            /**
             * 消息分发失败 (BifroMQ DIST_ERROR) ── 下行命令送达失败,
             * 走 {@code DispatchGroupEnum.DISTRIBUTION_ACK} 由 DistributionResultStage 记失败 stats.
             */
            String THINGLINKS_MQTT_DISTRIBUTION_ERROR_TOPIC = "mqtt.distribution.error.topic";

            /**
             * 消息分发 (BifroMQ DISTED) ── BifroMQ Standalone 部署下作为<b>设备 PUBLISH 上行</b>主流程:
             * plugin DISTED body 带完整 PUBLISH 报文(topic/qos/payload/publisher),
             * mqs {@code MqttDeviceDataEdgeAdapter} 把本 topic 路由到 {@code DispatchGroupEnum.DEVICE_DATA} →
             * PRE→CORE→POST 全套管道 → DevicePayloadDecodeStage → DeviceDatasHandler(物模型 + TDS 入库).
             * <p>下行命令(backend publisher)同样触发,在 DeviceCacheEnricher cache miss 后自然 skip,无需额外过滤.
             */
            String THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC = "mqtt.distribution.completed.topic";


            /**
             * PING 请求
             */
            String THINGLINKS_MQTT_PING_REQ_TOPIC = "mqtt.ping.req.topic";

            /**
             * MQTT 客户端认证失败 (BifroMQ NotAuthorizedClient) ── audit 用,mqs 侧仅 log 消费.
             */
            String THINGLINKS_MQTT_CLIENT_UNAUTHORIZED_TOPIC = "mqtt.client.unauthorized";

            /**
             * MQTT session 创建审计 (BifroMQ MQTT_SESSION_START) ── audit 用,mqs 侧仅 log 消费.
             */
            String THINGLINKS_MQTT_SESSION_START_TOPIC = "mqtt.session.start";

            /**
             * MQTT session 销毁审计 (BifroMQ MQTT_SESSION_STOP) ── audit 用,mqs 侧仅 log 消费.
             */
            String THINGLINKS_MQTT_SESSION_STOP_TOPIC = "mqtt.session.stop";

        }

        /**
         * MQS WebSocket Topics
         */
        interface MqsWebSocket {
            /**
             * Device online
             */
            String THINGLINKS_WEBSOCKET_CLIENT_CONNECTED_TOPIC = "websocket.client.connected.topic";

            /**
             * Client device offline
             */
            String THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC = "websocket.client.disconnect.topic";

            /**
             * Server device offline
             */
            String THINGLINKS_WEBSOCKET_SERVER_DISCONNECTED_TOPIC = "websocket.server.disconnect.topic";

            /**
             * Device kicked off
             */
            String THINGLINKS_WEBSOCKET_DEVICE_KICKED_TOPIC = "websocket.device.kicked.topic";

            /**
             * Message distribution error
             */
            String THINGLINKS_WEBSOCKET_DISTRIBUTION_ERROR_TOPIC = "websocket.distribution.error.topic";

            /**
             * Message distribution completed
             */
            String THINGLINKS_WEBSOCKET_DISTRIBUTION_COMPLETED_TOPIC = "websocket.distribution.completed.topic";

            /**
             * PING request
             */
            String THINGLINKS_WEBSOCKET_PING_REQ_TOPIC = "websocket.ping.req.topic";
        }

        /**
         * MQS Tcp Topics
         */
        interface MqsTcp {
            /**
             * Device online
             */
            String THINGLINKS_TCP_CLIENT_CONNECTED_TOPIC = "tcp.client.connected.topic";

            /**
             * Client device offline
             */
            String THINGLINKS_TCP_CLIENT_DISCONNECTED_TOPIC = "tcp.client.disconnect.topic";

            /**
             * Server device offline
             */
            String THINGLINKS_TCP_SERVER_DISCONNECTED_TOPIC = "tcp.server.disconnect.topic";

            /**
             * Device kicked off
             */
            String THINGLINKS_TCP_DEVICE_KICKED_TOPIC = "tcp.device.kicked.topic";

            /**
             * Message subscription acknowledgment
             */
            String THINGLINKS_TCP_SUBSCRIPTION_ACKED_TOPIC = "tcp.subscription.acked.topic";

            /**
             * Unsubscription acknowledgment
             */
            String THINGLINKS_TCP_UNSUBSCRIPTION_ACKED_TOPIC = "tcp.unsubscription.acked.topic";

            /**
             * Message distribution error
             */
            String THINGLINKS_TCP_DISTRIBUTION_ERROR_TOPIC = "tcp.distribution.error.topic";

            /**
             * Message distribution completed
             */
            String THINGLINKS_TCP_DISTRIBUTION_COMPLETED_TOPIC = "tcp.distribution.completed.topic";

            /**
             * PING request
             */
            String THINGLINKS_TCP_PING_REQ_TOPIC = "tcp.ping.req.topic";
        }
    }
}
