package com.mqttsnet.thinglinks.broker.downlink;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TCP 下行 ── 占位实现。broker 暂无 TCP session 下行推送通道,先记录待接入(TODO),不阻断业务。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class TcpDownlinkSender implements DownlinkChannelSender {

    @Override
    public String supportedProtocol() {
        return DownlinkProtocols.TCP;
    }

    @Override
    public R<?> send(DownlinkCommand command) {
        // TODO TCP 下行暂未实现:待 broker 接入 TCP session 推送通道后,在此组 VO 调对应 service 投递
        log.warn("[downlink.tcp] TCP 下行暂未实现(TODO),丢弃下行 clientId={} topic={} tenant={}", command.getClientId(), command.getTopic(), command.getTenantId());
        return R.fail("TCP downlink not supported yet");
    }
}
