package com.mqttsnet.thinglinks.bridge.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 桥接发送的运行时策略快照(rule 级覆盖优先 → dataSource.default_* 兜底)。
 * 由 {@link BridgeRetryPolicyResolver} 计算后传给 dispatcher 使用,字段已是"生效值",
 * 业务直接读不必判 null。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BridgeRetryPolicy {

    /**
     * 可靠性级别(0/1/2)。
     */
    private int qos;

    /**
     * QPS 限流(0 = 不限)。
     */
    private int rateLimitQps;

    /**
     * 最大重试次数(不含首次发送,0 = 不重试)。
     */
    private int retryMaxTimes;

    /**
     * 初始退避时长 ms(指数倍增)。
     */
    private long retryBackoffMs;

    /**
     * 单次发送超时 ms。
     */
    private int timeoutMs;

    /**
     * 死信投递的数据源 ID(null = 不投死信仅写日志)。
     */
    private Long deadLetterDataSourceId;
}
