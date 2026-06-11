package com.mqttsnet.thinglinks.broker.ws.service;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;

/**
 * WebSocket Broker API ── 业务侧调本接口给在线 ws 设备下发命令。
 *
 * @author ShiHuan Sun
 * @since 2023-10-31
 */
public interface WebSocketBrokerService {

    /**
     * 给在线 ws 设备下发命令(子协议编码 + RocketMQ 广播到各节点,持有 session 的节点投递)。
     *
     * @param publishMessageRequestVO 命令参数(含 clientId / topic / payload / reqId)
     * @return 已编码并广播的子协议 JSON
     * @throws BizException 设备不在线 / RocketMQ 不可用
     */
    String publishMessage(PublishWebSocketMessageRequestVO publishMessageRequestVO);
}
