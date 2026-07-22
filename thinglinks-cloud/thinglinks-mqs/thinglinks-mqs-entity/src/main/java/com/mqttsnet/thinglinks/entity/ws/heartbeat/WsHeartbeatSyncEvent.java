package com.mqttsnet.thinglinks.entity.ws.heartbeat;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WS 心跳跨节点同步事件。
 *
 * <h3>消息流</h3>
 * <pre>
 *   设备上行报文 → 持有 session 的 broker 节点 A
 *     A.WsHeartbeatTracker.update(tenantId, clientId)
 *       ① subject.lastActiveTime = now     (本地 session)
 *       ② RocketMQ broadcast(本事件)       (多节点同步)
 *
 *   各 broker 节点收到广播:
 *     本节点持有 clientId session(重连漂移场景) → 更新本地 lastActiveTime;否则跳过
 *     (不区分是否自己发的:发送方再处理一次也只是同值 touch,幂等无害)
 * </pre>
 *
 * <h3>设计</h3>
 * 放在 mqs-entity 共享包 ── broker 之间互相广播也通过 mqs-entity 引用本类(broker 都依赖
 * mqs-entity 已确立)。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsHeartbeatSyncEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID(Long 类型,与 owner / heartbeat key 命名空间一致)
     */
    private Long tenantId;

    /**
     * 设备 clientId
     */
    private String clientId;

    /**
     * 心跳时间戳(毫秒)
     */
    private Long timestamp;
}
