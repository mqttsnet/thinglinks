package com.mqttsnet.thinglinks.bridge.event.source;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 订阅源变更事件源 ── {@code SubscriptionSourceChangedEvent} 实际承载的 payload。
 *
 * <p>触发时机:SubscriptionSourceServiceImpl save / update / delete / changeStatus 后发布。
 * <p>消费端:{@code SubscriptionSourceLifecycleManager} 监听 → 重启 / 停止 Source 实例。
 *
 * <h3>operation 语义对照</h3>
 * <ul>
 *   <li>{@code CREATE} ── 新建(默认 enable=false,不会立即 start)</li>
 *   <li>{@code UPDATE} ── 配置更新(如启用中,先 stop 再 start 重新建连)</li>
 *   <li>{@code DELETE} ── 删除(先 stop)</li>
 *   <li>{@code ENABLE} ── 启用(start)</li>
 *   <li>{@code DISABLE} ── 禁用(stop)</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSourceChangedEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 操作类型:CREATE / UPDATE / DELETE / ENABLE / DISABLE
     */
    private String operation;

    /**
     * 订阅源业务编码(Source identifier 在 connection pool 内的 key)
     */
    private String sourceCode;

    /**
     * 变更后快照(CREATE/UPDATE/ENABLE 时填);DELETE/DISABLE 时可为 null
     */
    private SubscriptionSource snapshot;

    /**
     * 租户 ID ── 集群 Redis Pub/Sub 跨节点传递时的关键字段。
     * <p>B 节点 RedisListener 收到本事件后,需要 {@code ContextUtil.setTenantId(tenantId)}
     * 让本地 listener (startOne) 内的 {@code @DS(BASE_TENANT)} 切到对应租户库。
     * 单 JVM 本地 publishEvent 路径同样有效(冗余但安全)。
     */
    private Long tenantId;
}
