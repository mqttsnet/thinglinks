//package net.mqtts.broker.service;
//
//import cn.hutool.core.date.LocalDateTimeUtil;
//import io.github.quickmsg.common.channel.MqttChannel;
//import io.github.quickmsg.common.config.Configuration;
//import io.github.quickmsg.common.context.ReceiveContext;
//import io.github.quickmsg.common.interceptor.Interceptor;
//import io.github.quickmsg.common.interceptor.Invocation;
//import io.github.quickmsg.common.message.HeapMqttMessage;
//import io.github.quickmsg.common.message.SmqttMessage;
//import io.github.quickmsg.common.rule.DslExecutor;
//import io.github.quickmsg.common.utils.MessageUtils;
//import io.netty.handler.codec.mqtt.*;
//import lombok.extern.slf4j.Slf4j;
//import net.mqtts.link.api.RemoteMqttsDeviceActionService;
//import net.mqtts.link.api.RemoteMqttsDeviceService;
//import net.mqtts.link.api.domain.MqttsDevice;
//import net.mqtts.link.api.domain.MqttsDeviceAction;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * @Description: Mqtt 设备动作拦截处理
// * @Author: ShiHuan Sun
// * @E-mail: 13733918655@163.com
// * @Website: http://mqtts.net
// * @CreateDate: 2021/11/16$ 10:33$
// * @UpdateUser: ShiHuan Sun
// * @UpdateDate: 2021/11/16$ 10:33$
// * @UpdateRemark: 修改内容
// * @Version: 1.0
// */
//@Service
//@Slf4j
//@Component
//public class DeviceActionInterceptor implements Interceptor {
//
//    private static DeviceActionInterceptor DeviceActionInterceptor;
//
//    @Autowired
//    private RemoteMqttsDeviceService mqttsDeviceService;
//
//    @Autowired
//    private RemoteMqttsDeviceActionService mqttsDeviceActionService;
//
//
//    @PostConstruct
//    public void init() {
//        DeviceActionInterceptor = this;
//        DeviceActionInterceptor.mqttsDeviceService = this.mqttsDeviceService;
//        DeviceActionInterceptor.mqttsDeviceActionService = this.mqttsDeviceActionService;
//    }
//
//    /**
//     * 拦截目标参数
//     *
//     * @param invocation {@link Invocation}
//     * @return Object
//     */
//    @Override
//    public Object intercept(Invocation invocation) {
//        try {
//            MqttChannel mqttChannel = (MqttChannel) invocation.getArgs()[0];
//            SmqttMessage<MqttMessage> smqttMessage = (SmqttMessage<MqttMessage>) invocation.getArgs()[1];
//            ReceiveContext<Configuration> mqttReceiveContext = (ReceiveContext<Configuration>) invocation.getArgs()[2];
//            DslExecutor dslExecutor = mqttReceiveContext.getDslExecutor();
//            MqttMessage message = smqttMessage.getMessage();
//            //TODO MQTT动作数据处理
//            List<MqttMessageType> mqttMessageType = Arrays.asList(MqttMessageType.PUBLISH, MqttMessageType.DISCONNECT, MqttMessageType.PINGRESP, MqttMessageType.SUBSCRIBE, MqttMessageType.UNSUBSCRIBE);
//            if (!smqttMessage.getIsCluster() && mqttMessageType.contains(message.fixedHeader().messageType())) {
//                MqttPublishMessage publishMessage = (MqttPublishMessage) message;
//                HeapMqttMessage heapMqttMessage = this.clusterMessage(publishMessage, mqttChannel, smqttMessage.getTimestamp());
//                MqttsDeviceAction mqttsDeviceAction = new MqttsDeviceAction();
//                mqttsDeviceAction.setDevice_id(mqttChannel.getClientIdentifier());
//                mqttsDeviceAction.setAction_type(message.fixedHeader().messageType().toString());
//                mqttsDeviceAction.setStatus(message.decoderResult().toString());
//                mqttsDeviceAction.setMessage(heapMqttMessage.getTopic());
//                mqttsDeviceAction.setCreate_time(LocalDateTimeUtil.now());
//                DeviceActionInterceptor.mqttsDeviceActionService.add(mqttsDeviceAction);
//                DeviceActionInterceptor.mqttsDeviceService.updateConnectStatusByClientId(new MqttsDevice(mqttChannel.getStatus().toString(), mqttChannel.getClientIdentifier()));
//            }
//            // 拦截业务
//            return invocation.proceed(); // 放行
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 构建消息体
//     *
//     * @param message   {@link MqttPublishMessage}
//     * @param timestamp
//     * @return {@link HeapMqttMessage}
//     */
//    private HeapMqttMessage clusterMessage(MqttPublishMessage message, MqttChannel channel, long timestamp) {
//        MqttPublishVariableHeader header = message.variableHeader();
//        MqttFixedHeader fixedHeader = message.fixedHeader();
//        return HeapMqttMessage.builder()
//                .timestamp(timestamp)
//                .clientIdentifier(channel.getClientIdentifier())
//                .message(MessageUtils.copyReleaseByteBuf(message.payload()))
//                .topic(header.topicName())
//                .retain(fixedHeader.isRetain())
//                .qos(fixedHeader.qosLevel().value())
//                .build();
//    }
//
//    /**
//     * 排序
//     * 值越大权重越高
//     *
//     * @return 排序
//     */
//    @Override
//    public int sort() {
//        return 1;
//    }
//}
