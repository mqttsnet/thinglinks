package net.mqtts.broker.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.context.ReceiveContext;
import io.github.quickmsg.common.interceptor.Interceptor;
import io.github.quickmsg.common.interceptor.Invocation;
import io.github.quickmsg.common.message.HeapMqttMessage;
import io.github.quickmsg.common.message.SmqttMessage;
import io.github.quickmsg.common.rule.DslExecutor;
import io.github.quickmsg.common.utils.MessageUtils;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import net.mqtts.link.api.RemoteMqttsDeviceDatasService;
import net.mqtts.link.api.domain.MqttsDeviceDatas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @Description: mqtt消息拦截处理
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/19$ 21:25$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/19$ 21:25$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@Component
public class DeviceDatasInterceptor implements Interceptor {

    private static DeviceDatasInterceptor DeviceDatasInterceptor;

    @Autowired
    private RemoteMqttsDeviceDatasService remoteMqttsDeviceDatasService;


    @PostConstruct
    public void init() {
        DeviceDatasInterceptor = this;
        DeviceDatasInterceptor.remoteMqttsDeviceDatasService = this.remoteMqttsDeviceDatasService;
    }

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
            //TODO 发布消息类型处理（业务数据）
            if (!smqttMessage.getIsCluster() && message instanceof MqttPublishMessage && message.fixedHeader().messageType() == MqttMessageType.PUBLISH) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) message;
                HeapMqttMessage heapMqttMessage = this.clusterMessage(publishMessage, mqttChannel, smqttMessage.getTimestamp());
                log.info("Topic->{}" + heapMqttMessage.getTopic() + "Message->{}" + new String(heapMqttMessage.getMessage()));
                MqttsDeviceDatas mqttsDeviceDatas = new MqttsDeviceDatas();
                mqttsDeviceDatas.setDevice_id(heapMqttMessage.getClientIdentifier());
                mqttsDeviceDatas.setTopic(heapMqttMessage.getTopic());
                mqttsDeviceDatas.setMessage_id(String.valueOf(heapMqttMessage.getTimestamp()));
                mqttsDeviceDatas.setMessage(new String(heapMqttMessage.getMessage(), "UTF-8").trim());
                mqttsDeviceDatas.setStatus(message.decoderResult().toString());
                mqttsDeviceDatas.setCreate_time(LocalDateTimeUtil.now());
                DeviceDatasInterceptor.remoteMqttsDeviceDatasService.add(mqttsDeviceDatas);
               /* if (mqttReceiveContext.getConfiguration().getClusterConfig().isEnable()) {
                    mqttReceiveContext.getClusterRegistry().spreadPublishMessage(heapMqttMessage).subscribeOn(Schedulers.boundedElastic()).subscribe();
                }
                if (dslExecutor.isExecute()) {
                    dslExecutor.executeRule(mqttChannel, heapMqttMessage, mqttReceiveContext);
                }*/
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
     * 值越大权重越高
     *
     * @return 排序
     */
    @Override
    public int sort() {
        return 0;
    }
}
