package com.mqttsnet.thinglinks.broker.downlink;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备下行派发服务(broker-biz)── 按 {@link DownlinkCommand#getProtocolType()} 选 {@link DownlinkChannelSender}
 * 直调 broker 对应通道下发。业务侧只组命令、不碰协议分支;新增协议加一个 sender 即可,本服务零改动。
 *
 * <p>容错:协议空 / 未知 → 兜底 MQTT(历史默认行为);sender 异常 → 收敛为 {@link R#fail} 不向上抛。
 * 是否成功由调用方查 {@link R#getIsSuccess()} 决定后续。
 *
 * @author mqttsnet
 */
@Slf4j
@Service
public class DeviceDownlinkDispatchService {

    private final Map<String, DownlinkChannelSender> senderByProtocol;
    private final DownlinkChannelSender mqttFallback;

    public DeviceDownlinkDispatchService(List<DownlinkChannelSender> senders) {
        this.senderByProtocol = senders.stream().collect(Collectors.toMap(
            sender -> sender.supportedProtocol().toUpperCase(Locale.ROOT), Function.identity(), (a, b) -> a));
        this.mqttFallback = senderByProtocol.get(DownlinkProtocols.MQTT);
        log.info("[downlink] dispatch service 就绪,已注册协议通道: {}", senderByProtocol.keySet());
    }

    /**
     * 按协议分流下发。
     *
     * @param command 协议无关下行命令
     * @return broker 投递结果;失败以 {@link R#getIsSuccess()}=false 体现,不抛异常
     */
    public R<?> dispatch(DownlinkCommand command) {
        if (command == null) {
            return R.fail("downlink command is null");
        }
        String protocol = Optional.ofNullable(command.getProtocolType())
            .map(value -> value.toUpperCase(Locale.ROOT))
            .orElse(null);
        DownlinkChannelSender sender = Optional.ofNullable(protocol)
            .map(senderByProtocol::get)
            .orElse(null);
        if (sender == null) {
            // 协议解析不出 / 未注册 → 兜底 MQTT(保持历史默认行为),仅告警不阻断
            log.warn("[downlink] 协议[{}] 无对应通道,兜底 MQTT clientId={} topic={}",
                command.getProtocolType(), command.getClientId(), command.getTopic());
            sender = mqttFallback;
        }
        if (sender == null) {
            return R.fail("no downlink sender available for protocol: " + command.getProtocolType());
        }
        try {
            return sender.send(command);
        } catch (Exception e) {
            log.warn("[downlink] 发送异常 protocol={} clientId={} topic={} err={}",
                command.getProtocolType(), command.getClientId(), command.getTopic(), e.getMessage());
            return R.fail("downlink send failed: " + e.getMessage());
        }
    }
}
