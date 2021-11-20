package net.mqtts.broker.service;

import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.context.ReceiveContext;
import io.github.quickmsg.common.interceptor.Interceptor;
import io.github.quickmsg.common.interceptor.Invocation;
import io.github.quickmsg.common.message.HeapMqttMessage;
import io.github.quickmsg.common.message.SmqttMessage;
import io.github.quickmsg.common.rule.DslExecutor;
import io.github.quickmsg.common.utils.MessageUtils;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;


/**
 * @Description: mqtt消息拦截器示例
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/11/3$ 18:47$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/3$ 18:47$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@Component
public class DemoMessageInterceptor implements Interceptor {
    /**
     * 拦截目标参数
     *
     * @param invocation {@link Invocation}
     * @return Object
     */
    @Override
    public Object intercept(Invocation invocation) {
        try {
            MqttChannel mqttChannel = (MqttChannel) invocation.getArgs()[0];
            SmqttMessage<MqttMessage> smqttMessage = (SmqttMessage<MqttMessage>) invocation.getArgs()[1];
            ReceiveContext<Configuration> mqttReceiveContext = (ReceiveContext<Configuration>) invocation.getArgs()[2];
            DslExecutor dslExecutor = mqttReceiveContext.getDslExecutor();
            MqttMessage message = smqttMessage.getMessage();
            if (!smqttMessage.getIsCluster() && message instanceof MqttPublishMessage) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) message;
                HeapMqttMessage heapMqttMessage = this.clusterMessage(publishMessage, mqttChannel, smqttMessage.getTimestamp());
                log.info("TOPIC-"+heapMqttMessage.getTopic()+"------Message:"+new String(heapMqttMessage.getMessage()));
                if (mqttReceiveContext.getConfiguration().getClusterConfig().isEnable()) {
                    mqttReceiveContext.getClusterRegistry().spreadPublishMessage(heapMqttMessage).subscribeOn(Schedulers.boundedElastic()).subscribe();
                }
                if (dslExecutor.isExecute()) {
                    dslExecutor.executeRule(mqttChannel, heapMqttMessage, mqttReceiveContext);
                }
            }
            return invocation.proceed(); // 放行
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构建消息体
     *
     * @param message   {@link MqttPublishMessage}
     * @param timestamp
     * @return {@link HeapMqttMessage}
     */
    private HeapMqttMessage clusterMessage(MqttPublishMessage message, MqttChannel channel, long timestamp) {
        MqttPublishVariableHeader header = message.variableHeader();
        MqttFixedHeader fixedHeader = message.fixedHeader();
        return HeapMqttMessage.builder()
                .timestamp(timestamp)
                .clientIdentifier(channel.getClientIdentifier())
                .message(MessageUtils.copyReleaseByteBuf(message.payload()))
                .topic(header.topicName())
                .retain(fixedHeader.isRetain())
                .qos(fixedHeader.qosLevel().value())
                .build();
    }

    /**
     * 排序
     *
     * @return 排序
     */
    @Override
    public int sort() {
        return 0;
    }
}
