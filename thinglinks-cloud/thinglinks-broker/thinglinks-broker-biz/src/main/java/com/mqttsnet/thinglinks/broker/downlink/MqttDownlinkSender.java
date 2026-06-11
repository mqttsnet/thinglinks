package com.mqttsnet.thinglinks.broker.downlink;

import java.util.Optional;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.broker.mqtt.service.MqttBrokerService;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT 下行 ── 直调 broker 的 {@link MqttBrokerService}(经 BifroMQ,按 topic 路由到订阅设备)。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttDownlinkSender implements DownlinkChannelSender {

    private final MqttBrokerService mqttBrokerService;

    @Override
    public String supportedProtocol() {
        return DownlinkProtocols.MQTT;
    }

    @Override
    public R<?> send(DownlinkCommand command) {
        PublishMessageRequestVO vo = new PublishMessageRequestVO()
            .setReqId(command.getReqId())
            .setTenantId(command.getTenantId())
            .setTopic(command.getTopic())
            .setQos(command.getQos())
            .setClientType(Optional.ofNullable(command.getClientType()).orElse("web"))
            .setExpirySeconds(Optional.ofNullable(command.getExpirySeconds()).orElse("3600"));
        // 负载:显式指定 forceBase64Decode 则照办;否则自动探测(等价历史 setPayloadData(message))
        if (command.getForceBase64Decode() != null) {
            vo.setPayload(command.getPayload()).setForceBase64Decode(command.getForceBase64Decode());
        } else {
            vo.setPayloadData(command.getPayload());
        }
        try {
            return R.success(mqttBrokerService.publishMessage(vo));
        } catch (BizException e) {
            log.error("[downlink.mqtt] send failed topic={} err={}", command.getTopic(), e.getMessage());
            return R.fail(e.getMessage());
        }
    }
}
