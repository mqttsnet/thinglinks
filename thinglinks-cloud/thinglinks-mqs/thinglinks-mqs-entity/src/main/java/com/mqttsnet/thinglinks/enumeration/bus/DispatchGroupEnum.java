package com.mqttsnet.thinglinks.enumeration.bus;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 业务分组 枚举 ── 决定事件走哪条 dispatch 通路,实现"相近 topic 不同业务流"的解耦。
 * </p>
 *
 * <h3>分组语义</h3>
 * <table border="1">
 *   <tr><th>Group</th><th>典型事件</th><th>Pipeline 路径</th></tr>
 *   <tr><td>DEVICE_DATA</td><td>设备 PUBLISH 数据上行(借 distribution.completed.topic 通道)</td><td>PRE → CORE → POST(全套)</td></tr>
 *   <tr><td>DEVICE_LIFECYCLE</td><td>CONNECT/DISCONNECT/KICKED/CLOSE/PING/HEART_TIMEOUT/ERROR</td><td>PRE → CORE(更新设备状态)+ POST(广播 + 指标)</td></tr>
 *   <tr><td>CONTROL_ACK</td><td>SUB_ACKED/UNSUB_ACKED</td><td>跳 CORE,仅 PRE + POST 的 audit + metric</td></tr>
 *   <tr><td>DISTRIBUTION_ACK</td><td>distribution.error(DISPATCH_ERROR,broker 下行投递失败)</td><td>跳 CORE,仅 PRE + POST,DistributionResultStage 记失败 stats</td></tr>
 *   <tr><td>ERROR_EVENT</td><td>(预留)协议异常专用通路</td><td>(暂未启用)</td></tr>
 * </table>
 *
 * <h3>命名注</h3>
 * group 名 {@code DISTRIBUTION_ACK} 是 pipeline 路由维度(沿用 broker 历史命名);
 * 它承载的 action {@code DISPATCH_ERROR} 是事件语义维度,两者独立,不强求同名。
 *
 * <h3>多 MQ 解耦</h3>
 * 不同 group 可配置不同下游 MQ:control_ack 量大走 Kafka,error_event 走 RocketMQ 告警。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "DispatchGroupEnum", description = "协议总线-业务分组 枚举")
public enum DispatchGroupEnum {

    /**
     * 设备数据上行(主数据流):走完整三相管道
     */
    DEVICE_DATA("DEVICE_DATA", "设备数据"),

    /**
     * 设备生命周期(连接 / 断开 / 顶号 / 心跳超时):状态更新 + 部分广播
     */
    DEVICE_LIFECYCLE("DEVICE_LIFECYCLE", "生命周期"),

    /**
     * 控制信令 ack(订阅 / 取消订阅 / 下行 ack):仅审计 + 指标,不入业务
     */
    CONTROL_ACK("CONTROL_ACK", "控制信令"),

    /**
     * Broker 分发失败回执通路:承载 DISPATCH_ERROR(distribution.error.topic),DistributionResultStage 记失败 stats.
     * 成功回执(distribution.completed.topic 承载 PUBLISH 报文)已被 DEVICE_DATA 主流程独占.
     */
    DISTRIBUTION_ACK("DISTRIBUTION_ACK", "Broker分发回执"),

    /**
     * 错误事件:预留专用告警通路,暂未启用(协议异常 ERROR 当前仍走 DEVICE_LIFECYCLE).
     */
    ERROR_EVENT("ERROR_EVENT", "错误事件");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举。
     *
     * @param value 业务分组 value(枚举常量名,如 DEVICE_DATA / DEVICE_LIFECYCLE)
     * @return 对应枚举的 Optional;空字符串或未匹配返回 {@link Optional#empty()}
     */
    public static Optional<DispatchGroupEnum> fromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Stream.of(values())
            .filter(e -> e.value.equalsIgnoreCase(value))
            .findFirst();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
