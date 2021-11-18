package net.mqtts.link.service.impl;

import io.github.quickmsg.common.channel.MqttChannel;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.context.ReceiveContext;
import io.github.quickmsg.common.interceptor.Interceptor;
import io.github.quickmsg.common.interceptor.Invocation;
import io.github.quickmsg.common.message.SmqttMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import net.mqtts.common.core.utils.StringUtils;
import net.mqtts.link.service.device.IMqttsDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @Description: Mqtt 设备动作拦截处理
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
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
    private IMqttsDeviceService mqttsDeviceService;


    @PostConstruct
    public void init() {
        DeviceActionInterceptor = this;
        DeviceActionInterceptor.mqttsDeviceService = this.mqttsDeviceService;
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
            Object variableHeader = smqttMessage.getMessage().variableHeader();
            MqttConnectPayload payload = null;
            try {
                payload = (MqttConnectPayload) smqttMessage.getMessage().payload();
            } catch (Exception e) {
                log.error("MqttConnectPayload转换异常：{}", e.getMessage());
            }
            log.info(variableHeader.getClass().getName());
            // 设备上下线日志写入处理，更新设备在线状态信息
            String clientId = "";
            if (StringUtils.isNotEmpty(mqttChannel.getClientIdentifier())) {
                clientId = mqttChannel.getClientIdentifier();
            }
            if (null != payload && StringUtils.isNotEmpty(payload.clientIdentifier())) {
                clientId = payload.clientIdentifier();
            }
            log.info("设备ClientID:{},设备状态：{}", clientId, mqttChannel.getStatus().toString());
            DeviceActionInterceptor.mqttsDeviceService.updateConnectStatusByClientId(mqttChannel.getStatus().toString(), clientId);
            //QoS = PUBLISH报文的服务质量等级
            MqttFixedHeader mqttFixedHeader = smqttMessage.getMessage().fixedHeader();//固定报头
            // 拦截业务
            return invocation.proceed(); // 放行
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
