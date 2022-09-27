package com.mqttsnet.thinglinks.broker.service;

import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceActionService;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Mqtt 设备动作拦截处理
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/16$ 10:33$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/16$ 10:33$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@Component
public class DeviceActionInterceptor implements Interceptor {

    private static DeviceActionInterceptor DeviceActionInterceptor;

    @Autowired
    private RemoteDeviceService deviceService;

    @Autowired
    private RemoteDeviceActionService deviceActionService;

    @Autowired
    private RedisService redisService;


    @PostConstruct
    public void init() {
        DeviceActionInterceptor = this;
        DeviceActionInterceptor.deviceService = this.deviceService;
        DeviceActionInterceptor.deviceActionService = this.deviceActionService;
        DeviceActionInterceptor.redisService = this.redisService;
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
            //TODO MQTT设备心跳处理
            List<MqttMessageType> mqttMessageType = Collections.singletonList(MqttMessageType.PINGREQ);
            if (!smqttMessage.getIsCluster() && mqttMessageType.contains(message.fixedHeader().messageType())) {
                CompletableFuture<R<Device>> comp = CompletableFuture.supplyAsync(() -> DeviceActionInterceptor.deviceService.findOneByClientId(mqttChannel.getClientIdentifier()));
                R<Device> deviceServiceOneByClientId =  comp.get();
                if (null != deviceServiceOneByClientId.getData() ) {
                    Device device = deviceServiceOneByClientId.getData();
                    //缓存设备信息
                    DeviceActionInterceptor.redisService.setCacheObject(Constants.DEVICE_RECORD_KEY+device.getClientId(),device,60L+ Long.parseLong(DateUtils.getRandom(1)), TimeUnit.SECONDS);
//                    DeviceActionInterceptor.redisService.expire(Constants.DEVICE_RECORD_KEY+device.getDeviceIdentification(),60L+ Long.parseLong(DateUtils.getRandom(1)), TimeUnit.SECONDS);
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
     * 值越大权重越高
     *
     * @return 排序
     */
    @Override
    public int sort() {
        return 0;
    }
}
