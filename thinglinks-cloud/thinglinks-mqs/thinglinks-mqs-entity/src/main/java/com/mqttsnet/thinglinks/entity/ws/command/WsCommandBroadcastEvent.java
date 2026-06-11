package com.mqttsnet.thinglinks.entity.ws.command;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WS 下行命令广播事件(平台 → 设备)。
 *
 * <h3>消息流</h3>
 * <pre>
 *   业务侧下发命令 → 任一 broker 节点
 *     WebSocketBrokerServiceImpl.publishMessage(vo)
 *       ① 编码 ws 子协议报文(encodedMessage)
 *       ② 在线校验(查 Redis session 信息;离线则直接返回"设备不在线")
 *       ③ RocketMQ broadcast(本事件)
 *
 *   每个 broker 节点 WsCommandDownlinkListener 收到广播:
 *     本节点持有 clientId 的 session(即持有该设备 TCP 的那一个节点)→ session.sendText(encodedMessage)
 *     否则跳过
 * </pre>
 *
 * <h3>设计</h3>
 * 报文在 producer 侧一次性编码后广播(各节点直推同一份字节,messageId 一致);
 * 放 mqs-entity 共享包 ── broker 之间互相广播通过 mqs-entity 引用本类(broker 都依赖 mqs-entity 已确立)。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsCommandBroadcastEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID(字符串形式,与 session 信息 / 心跳事件命名空间一致)。
     */
    private String tenantId;

    /**
     * 目标设备 clientId(各节点据此查本地 session 表)。
     */
    private String clientId;

    /**
     * 已编码的 ws 子协议下行报文(持有 session 的节点直接 sendText 此字符串;messageId 已含在内)。
     */
    private String encodedMessage;

    /**
     * 下发时间戳(毫秒)── 仅日志追踪用。
     */
    private Long ts;
}
