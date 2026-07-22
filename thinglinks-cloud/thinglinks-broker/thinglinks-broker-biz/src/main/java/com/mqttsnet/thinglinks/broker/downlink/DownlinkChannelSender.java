package com.mqttsnet.thinglinks.broker.downlink;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.query.DownlinkCommand;

/**
 * 下行通道发送策略 ── 每种协议一个实现,自管自己的请求 VO 并直调 broker 对应的下发 service。
 *
 * <p>新增协议只需新增一个 {@code @Component} 实现并声明 {@link #supportedProtocol()},
 * {@link DeviceDownlinkDispatchService} 启动期自动发现注册,无需改派发服务。
 *
 * @author mqttsnet
 */
public interface DownlinkChannelSender {

    /**
     * 本通道支持的协议(取值同 {@code ProtocolTypeEnum.getValue()},见 {@link DownlinkProtocols})。
     *
     * @return 协议标识
     */
    String supportedProtocol();

    /**
     * 把下行命令投到该协议通道。投递成败以 {@link R} 体现,不抛异常阻断。
     *
     * @param command 协议无关下行命令
     * @return broker 投递结果
     */
    R<?> send(DownlinkCommand command);
}
