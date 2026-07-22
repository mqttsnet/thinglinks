package com.mqttsnet.thinglinks.broker.downlink;

import java.util.Optional;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.broker.ws.service.WebSocketBrokerService;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket 下行 ── 直调 broker 的 {@link WebSocketBrokerService}(按 clientId 编码后 RocketMQ 广播,
 * 由持有该设备 session 的 broker 节点 sendText 到 ws session)。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDownlinkSender implements DownlinkChannelSender {

    private final WebSocketBrokerService webSocketBrokerService;

    @Override
    public String supportedProtocol() {
        return DownlinkProtocols.WEBSOCKET;
    }

    @Override
    public R<?> send(DownlinkCommand command) {
        PublishWebSocketMessageRequestVO vo = new PublishWebSocketMessageRequestVO()
            .setReqId(command.getReqId())
            .setTenantId(command.getTenantId())
            .setTopic(command.getTopic())
            .setClientId(command.getClientId())
            .setClientType(Optional.ofNullable(command.getClientType()).orElse("web"))
            .setPayload(command.getPayload());
        try {
            return R.success(webSocketBrokerService.publishMessage(vo));
        } catch (BizException e) {
            log.warn("[downlink.ws] send failed clientId={} topic={} err={}",
                command.getClientId(), command.getTopic(), e.getMessage());
            return R.fail(e.getMessage());
        }
    }
}
