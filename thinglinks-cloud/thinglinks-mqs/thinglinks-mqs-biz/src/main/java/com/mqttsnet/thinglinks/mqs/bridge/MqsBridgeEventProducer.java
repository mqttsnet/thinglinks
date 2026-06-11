package com.mqttsnet.thinglinks.mqs.bridge;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 桥接事件旁路投递器(mqs 端).
 *
 * <h3>定位</h3>
 * 在 mqs 现有事件链路(MQTT / WebSocket / TCP 协议处理)尾部以"旁路 + best-effort"方式
 * 投递 {@link BridgeMessageEnvelope} 到 RocketMQ {@link BizMqRouteConstant.Bridge#DEVICE_EVENT},
 * 由 thinglinks-rule 的 BridgeDeviceEventConsumer 消费做规则匹配 + 桥接分发.
 *
 * <h3>容错策略(Outbox 变体)</h3>
 * <ul>
 *   <li>异步发送:调用方 0 阻塞,asyncSend 后立即返回</li>
 *   <li>失败仅 warn 不抛:mqs 主链路(设备数据持久化)已稳定运行多年,不能因 RocketMQ 故障让设备主数据丢失</li>
 *   <li>桥接故障最坏 = 设备数据没桥接出去(设备会重发,可补偿);主链路 100% 不受影响</li>
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

    private final ObjectProvider<RocketmqTemplate> rocketmqTemplateProvider;

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
     * 旁路投递桥接事件(异步、best-effort).
     * <p>失败只 warn,不抛异常,保证不影响 mqs 主链路.
     *
     * @param envelope 桥接消息体(业务侧已构造完毕)
     */
    public void publishBridgeEvent(BridgeMessageEnvelope envelope) {
        if (envelope == null) {
            log.warn("[MqsBridgeEventProducer] envelope is null, skip");
            return;
        }
        RocketmqTemplate rocketmqTemplate = rocketmqTemplateProvider.getIfAvailable();
        if (rocketmqTemplate == null) {
            // 启动 selfCheck 已对此场景 warn 过,运行时再吞调用,不重复刷屏
            return;
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
        if (log.isDebugEnabled()) {
            log.debug("[MqsBridgeEventProducer] about to asyncSend dest={} traceId={} clientId={} action={}",
                destination, envelope.getTraceId(), envelope.getClientId(), envelope.getActionType());
        }
        try {
            rocketmqTemplate.asyncSend(destination, envelope, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    if (log.isDebugEnabled()) {
                        log.debug("[MqsBridgeEventProducer] sent OK dest={} traceId={} clientId={} action={} msgId={} status={}",
                            destination, envelope.getTraceId(),
                            envelope.getClientId(), envelope.getActionType(),
                            sendResult.getMsgId(), sendResult.getSendStatus());
                    }
                }

                @Override
                public void onException(Throwable e) {
                    log.warn("[MqsBridgeEventProducer] send failed (non-blocking) dest={} traceId={} clientId={} action={} err={}",
                        destination, envelope.getTraceId(),
                        envelope.getClientId(), envelope.getActionType(), e.getMessage(), e);
                }
            });
        } catch (Throwable e) {
            // 例如 NameServer 完全不可用同步抛异常;同样吞掉不阻塞主链路
            log.warn("[MqsBridgeEventProducer] asyncSend invocation failed (non-blocking) dest={} traceId={} clientId={} action={}",
                destination, envelope.getTraceId(),
                envelope.getClientId(), envelope.getActionType(), e);
        }
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
