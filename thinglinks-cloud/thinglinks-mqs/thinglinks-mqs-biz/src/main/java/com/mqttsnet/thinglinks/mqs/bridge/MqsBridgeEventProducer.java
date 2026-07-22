package com.mqttsnet.thinglinks.mqs.bridge;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 桥接事件旁路投递器(mqs 端).
 *
 * <h3>定位</h3>
 * 在 mqs 现有事件链路(MQTT / WebSocket / TCP 协议处理)尾部以"旁路 + 同步确认"方式
 * 投递 {@link BridgeMessageEnvelope} 到 RocketMQ {@link BizMqRouteConstant.Bridge#DEVICE_EVENT},
 * 由 thinglinks-rule 的 BridgeDeviceEventConsumer 消费做规则匹配 + 桥接分发.
 *
 * <h3>容错策略</h3>
 * <ul>
 *   <li>同步确认:返回值明确表示消息是否已被 broker 确认接收</li>
 *   <li>有限重试:短暂网络抖动不立即丢事件</li>
 *   <li>失败不抛给主链路:设备数据落库仍优先完成,但调用方可按返回值记录失败指标</li>
 * </ul>
 *
 * <h3>上下文传播</h3>
 * 通过 {@link RocketmqTemplate} 自动塞 ContextUtil.getLocalMap() 整体到 header
 * (X-Thinglinks-LocalMap);rule 端 AbstractTenantAwareRocketmqListener 自动恢复,业务无感知.
 *
 * @author mqttsnet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MqsBridgeEventProducer {

    private static final long DEFAULT_SEND_TIMEOUT_MS = 5000L;
    private static final int DEFAULT_SEND_MAX_ATTEMPTS = 2;

    private final ObjectProvider<RocketmqTemplate> rocketmqTemplateProvider;

    @Value("${thinglinks.mqs.bridge-event.sync-send-timeout-ms:5000}")
    private long sendTimeoutMs;

    @Value("${thinglinks.mqs.bridge-event.sync-send-max-attempts:2}")
    private int sendMaxAttempts;

    /**
     * 启动时主动检查 RocketMQ 是否注入成功.
     * <p>在 mqs-server 部署且 thinglinks-rocketmq-starter 依赖在的前提下,
     * RocketmqTemplate 应该是必有 Bean.如果 null 大概率是:
     * <ul>
     *   <li>nacos 上 rocketmq.yml 没引(spring.config.import 漏配)</li>
     *   <li>rocketmq.name-server 为空,导致 RocketMQTemplate 没装配</li>
     *   <li>maven 依赖没引 thinglinks-rocketmq-starter</li>
     * </ul>
     * 启动期就报警,比每条事件丢失才发现强.
     */
    @PostConstruct
    public void selfCheck() {
        RocketmqTemplate t = rocketmqTemplateProvider.getIfAvailable();
        if (t == null) {
            log.warn("[MqsBridgeEventProducer] startup self-check: RocketmqTemplate Bean NOT available! " +
                "桥接旁路投递将被跳过 ── 检查:" +
                "(1) nacos rocketmq.yml 是否被 spring.config.import 引入;" +
                "(2) rocketmq.name-server 是否非空;" +
                "(3) thinglinks-rocketmq-starter 依赖是否在 pom 中.");
        } else {
            log.info("[MqsBridgeEventProducer] startup self-check OK: RocketmqTemplate ready");
        }
    }

    /**
     * 投递桥接事件并等待 broker 确认.
     * <p>失败只 warn 并返回 {@code false},保证不影响 mqs 主链路;调用方必须按返回值记录指标,
     * 避免把"调用过发送方法"误判为"消息已进入 RocketMQ".
     *
     * @param envelope 桥接消息体(业务侧已构造完毕)
     * @return {@code true}=已收到 broker SEND_OK;{@code false}=未确认投递成功
     */
    public boolean publishBridgeEvent(BridgeMessageEnvelope envelope) {
        if (envelope == null) {
            log.warn("[MqsBridgeEventProducer] envelope is null, skip");
            return false;
        }
        RocketmqTemplate rocketmqTemplate = rocketmqTemplateProvider.getIfAvailable();
        if (rocketmqTemplate == null) {
            log.warn("[MqsBridgeEventProducer] RocketmqTemplate unavailable, skip traceId={} clientId={} action={}",
                envelope.getTraceId(), envelope.getClientId(), envelope.getActionType());
            return false;
        }
        if (StrUtil.isBlank(envelope.getTraceId())) {
            envelope.setTraceId(ContextUtil.getLogTraceId());
        }
        if (StrUtil.isBlank(envelope.getTenantId())) {
            envelope.setTenantId(ContextUtil.getTenantIdStr());
        }
        if (envelope.getTs() == null) {
            envelope.setTs(System.currentTimeMillis());
        }

        String destination = BizMqRouteConstant.Bridge.DEVICE_EVENT + ":" + nullSafeTag(envelope.getActionType());
        int attempts = Math.max(1, sendMaxAttempts > 0 ? sendMaxAttempts : DEFAULT_SEND_MAX_ATTEMPTS);
        long timeoutMs = sendTimeoutMs > 0 ? sendTimeoutMs : DEFAULT_SEND_TIMEOUT_MS;
        try {
            for (int attempt = 1; attempt <= attempts; attempt++) {
                try {
                    SendResult sendResult = rocketmqTemplate.syncSend(destination, envelope, timeoutMs);
                    if (sendResult != null && SendStatus.SEND_OK == sendResult.getSendStatus()) {
                        log.info("[MqsBridgeEventProducer] sent OK dest={} traceId={} clientId={} action={} msgId={} attempt={}/{}",
                            destination, envelope.getTraceId(), envelope.getClientId(), envelope.getActionType(),
                            sendResult.getMsgId(), attempt, attempts);
                        return true;
                    }
                    log.warn("[MqsBridgeEventProducer] send not OK dest={} traceId={} clientId={} action={} status={} attempt={}/{}",
                        destination, envelope.getTraceId(), envelope.getClientId(), envelope.getActionType(),
                        sendResult == null ? null : sendResult.getSendStatus(), attempt, attempts);
                } catch (Throwable e) {
                    log.warn("[MqsBridgeEventProducer] send failed dest={} traceId={} clientId={} action={} attempt={}/{} timeoutMs={} err={}",
                        destination, envelope.getTraceId(), envelope.getClientId(), envelope.getActionType(),
                        attempt, attempts, timeoutMs, e.getMessage(), e);
                }
            }
        } catch (Throwable e) {
            log.warn("[MqsBridgeEventProducer] send invocation failed dest={} traceId={} clientId={} action={}",
                destination, envelope.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), e);
        }
        log.warn("[MqsBridgeEventProducer] send exhausted dest={} traceId={} clientId={} action={} attempts={}",
            destination, envelope.getTraceId(), envelope.getClientId(), envelope.getActionType(), attempts);
        return false;
    }

    /**
     * actionType 为空时记 warn(业务漏传字段是 bug),用 {@code NO_ACTION} 作为可识别 fallback tag.
     *
     * @param actionType 当前事件动作类型
     * @return 安全 tag(为空时返回 NO_ACTION fallback)
     */
    private String nullSafeTag(String actionType) {
        if (StrUtil.isBlank(actionType)) {
            log.warn("[MqsBridgeEventProducer] envelope.actionType is blank, falling back to '{}' tag", BizMqRouteConstant.Tags.BRIDGE_DEVICE_EVENT_NO_ACTION);
            return BizMqRouteConstant.Tags.BRIDGE_DEVICE_EVENT_NO_ACTION;
        }
        return actionType;
    }
}
