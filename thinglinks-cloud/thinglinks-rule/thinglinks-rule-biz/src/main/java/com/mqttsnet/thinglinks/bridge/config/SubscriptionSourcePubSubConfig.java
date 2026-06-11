package com.mqttsnet.thinglinks.bridge.config;

import com.mqttsnet.thinglinks.bridge.event.listener.SubscriptionSourceRedisListener;
import com.mqttsnet.thinglinks.constants.BridgeChannelConstant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 订阅源变更 Redis Pub/Sub 配置(集群多副本跨节点失效)。
 * A 节点禁用 → 发 Redis pub → B/C 节点 LifecycleManager 停本地 KafkaConsumer/MqttClient。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
public class SubscriptionSourcePubSubConfig {

    public static final String CHANNEL = BridgeChannelConstant.SUBSCRIPTION_CHANGED;

    @Bean(destroyMethod = "stop")
    public RedisMessageListenerContainer subscriptionSourceRedisListenerContainer(
            RedisConnectionFactory connectionFactory, SubscriptionSourceRedisListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 反射调 listener.onRedisMessage(byte[] body, byte[] channel)
        container.addMessageListener(
                new MessageListenerAdapter(listener, "onRedisMessage"),
                new PatternTopic(CHANNEL));
        return container;
    }
}
