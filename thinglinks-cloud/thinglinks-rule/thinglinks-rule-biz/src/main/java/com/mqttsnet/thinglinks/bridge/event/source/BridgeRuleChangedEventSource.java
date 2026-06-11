package com.mqttsnet.thinglinks.bridge.event.source;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 桥接规则变更事件源 ── {@code BridgeRuleChangedEvent} 实际承载的 payload。
 *
 * <p>事件 {@code operation} 命名空间(prefix)区分领域:
 * <ul>
 *   <li>{@code CREATE / UPDATE / DELETE / ENABLE / DISABLE}:桥接规则本身</li>
 *   <li>{@code DS_*}:数据源(prefix 来自 DataSource)</li>
 *   <li>{@code GROOVY_*}:规则脚本(prefix 来自 RuleGroovyScript)</li>
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
public class BridgeRuleChangedEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 桥接规则 ID
     */
    private Long ruleId;

    /**
     * 桥接规则业务编码 ── BridgeRuleCache 用作 Redis hash field 精确失效用。
     * <p>非桥接规则变更场景(如 DS_* / GROOVY_*)填 null,触发 bucket / 全清失效。
     */
    private String ruleCode;

    /**
     * 应用 ID(按 app 维度失效缓存用);可为 null 表示批量变更
     */
    private String appId;

    /**
     * 方向(10/20);可为 null 表示两个方向都需失效
     */
    private String direction;

    /**
     * 租户 ID(用于精确失效本租户的缓存条目)
     */
    private String tenantId;
}
