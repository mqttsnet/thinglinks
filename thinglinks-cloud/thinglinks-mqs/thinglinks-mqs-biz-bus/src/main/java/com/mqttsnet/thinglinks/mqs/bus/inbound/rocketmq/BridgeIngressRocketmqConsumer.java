package com.mqttsnet.thinglinks.mqs.bus.inbound.rocketmq;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.rocketmq.listener.AbstractTenantAwareRocketmqListener;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenInnerFacade;
import com.mqttsnet.thinglinks.mqs.event.processor.DevicePublishProcessor;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.rule.facade.RuleJobHandlerFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 桥接入站消费器（mqs 端）。从 RocketMQ topic {@link BizMqRouteConstant.Bridge#INGRESS} 消费,按 Tag 分流:
 * MQTT_FORWARD 构造 {@link CommonDeviceEvent} 调 {@link DevicePublishProcessor#process} 复用数据上报持久化链路(与出站桥接解耦);
 * RAW_INSERT 构造 {@link DeviceActionSaveVO} 调 Feign 直写 device_action 表;
 * RULE_TRIGGER 调 {@link RuleJobHandlerFacade#triggerRulePolicy} 触发场景联动。
 *
 * <p>父类 {@link AbstractTenantAwareRocketmqListener#onMessage(MessageExt)} 自动从 message header 恢复
 * ContextUtil.LocalMap,下游 @DS / Feign / MyBatis 拦截器都能正确切库。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(
    topic = BizMqRouteConstant.Bridge.INGRESS,
    consumerGroup = BizMqRouteConstant.Groups.BRIDGE_INGRESS,
    selectorExpression = "*",
    messageModel = MessageModel.CLUSTERING,
    consumeMode = ConsumeMode.CONCURRENTLY
)
public class BridgeIngressRocketmqConsumer extends AbstractTenantAwareRocketmqListener<BridgeMessageEnvelope> {

    private final DevicePublishProcessor devicePublishProcessor;
    private final DeviceOpenInnerFacade deviceOpenInnerFacade;
    private final RuleJobHandlerFacade ruleJobHandlerFacade;

    @Override
    protected Class<BridgeMessageEnvelope> getBodyClass() {
        return BridgeMessageEnvelope.class;
    }

    @Override
    protected void onTenantMessage(BridgeMessageEnvelope envelope, MessageExt raw) {
        if (envelope == null) {
            log.warn("[BridgeIngressConsumer] received null envelope, msgId={}", raw.getMsgId());
            return;
        }
        String targetHandler = StrUtil.nullToDefault(raw.getTags(),
            BizMqRouteConstant.Tags.INGRESS_MQTT_FORWARD);
        long start = System.currentTimeMillis();
        try {
            switch (targetHandler) {
                case BizMqRouteConstant.Tags.INGRESS_MQTT_FORWARD:
                    handleMqttForward(envelope);
                    break;
                case BizMqRouteConstant.Tags.INGRESS_RAW_INSERT:
                    handleRawInsert(envelope);
                    break;
                case BizMqRouteConstant.Tags.INGRESS_RULE_TRIGGER:
                    handleRuleTrigger(envelope);
                    break;
                default:
                    // 未知 tag 应抛出让 RocketMQ broker 重投 → 超限后进死信队列，便于运维排查 + 不丢消息
                    throw new IllegalArgumentException(
                        "unknown targetHandler tag=" + targetHandler
                            + " traceId=" + envelope.getTraceId()
                            + "; expected MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER");
            }
            if (log.isDebugEnabled()) {
                log.debug("[BridgeIngressConsumer] handled targetHandler={} traceId={} latencyMs={}",
                    targetHandler, envelope.getTraceId(), System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            // 让 RocketMQ 触发 broker 重投（不超过 max-reconsume-times 进死信 group）
            log.error("[BridgeIngressConsumer] handle failed targetHandler={} msgId={} traceId={}",
                targetHandler, raw.getMsgId(), envelope.getTraceId(), e);
            throw e;
        }
    }

    // ============================== MQTT_FORWARD ==============================

    /**
     * 处理 MQTT_FORWARD:把 envelope 伪装成本平台设备 publish 注入 mqs 主流程,后续业务对入站完全透明。
     * 直接调 {@link DevicePublishProcessor#process} 而非 dispatcher ── 入站桥接无连接生命周期,也不算真实设备上行,
     * 跳过 hook / uplink 计数。
     *
     * @param envelope 桥接消息封装
     */
    private void handleMqttForward(BridgeMessageEnvelope envelope) {
        String topic = envelope.getTopic();
        if (StrUtil.isBlank(topic)) {
            log.warn("[BridgeIngressConsumer] MQTT_FORWARD missing topic traceId={} skip",
                envelope.getTraceId());
            return;
        }
        // 处理 rawMessage：BridgeMessageEnvelope.rawMessage 是上游整原 JSON 报文 String(纯透传);
        // MQTT 协议链路要求 payload 字段是 Base64(保证二进制安全),payloadHex 是 16 进制。
        byte[] rawBytes = envelope.getRawMessage() == null
            ? new byte[0]
            : envelope.getRawMessage().getBytes(StandardCharsets.UTF_8);
        String payloadBase64 = Base64.encode(rawBytes);
        String payloadHex = HexUtil.encodeHexStr(rawBytes);
        Long ts = Optional.ofNullable(envelope.getTs()).orElseGet(System::currentTimeMillis);

        // 重建 mqtt 风格 rawMessage(下游 sink 审计 / bridgeIngressTraceId 追溯用)
        JSONObject mqttMsg = new JSONObject();
        mqttMsg.put(CommonIotConstants.EVENT_TYPE, DeviceActionTypeEnum.PUBLISH.name());
        mqttMsg.put(CommonIotConstants.TENANT_ID, envelope.getTenantId());
        mqttMsg.put(CommonIotConstants.TOPIC, topic);
        mqttMsg.put(CommonIotConstants.QOS, "1");
        mqttMsg.put(CommonIotConstants.PAYLOAD, payloadBase64);
        mqttMsg.put(CommonIotConstants.PAYLOAD_HEX, payloadHex);
        mqttMsg.put(CommonIotConstants.ORIGINAL_SIZE, String.valueOf(rawBytes.length));
        mqttMsg.put(CommonIotConstants.ENCODING, "BASE64");
        mqttMsg.put(CommonIotConstants.EVENT_UTC, ts);
        mqttMsg.put("bridgeIngressTraceId", envelope.getTraceId());

        CommonDeviceEvent event = CommonDeviceEvent.builder()
            .source(this)
            .protocolType(ProtocolTypeEnum.MQTT)
            .actionType(DeviceActionTypeEnum.PUBLISH)
            .clientId(envelope.getClientId())
            .tenantId(envelope.getTenantId())
            .appId(envelope.getAppId())
            .productIdentification(envelope.getProductIdentification())
            .deviceIdentification(envelope.getDeviceIdentification())
            .topic(topic)
            .qos(1)
            .payload(payloadBase64)
            .payloadHex(payloadHex)
            .encoding("BASE64")
            .originalSize(rawBytes.length)
            .ts(ts)
            .rawMessage(JSON.toJSONString(mqttMsg))
            .build();

        // 桥接入站只复用"数据上报持久化"链路;出站桥接是独立业务,不在入站触发(两条流解耦)
        devicePublishProcessor.process(event);

        log.info("[BridgeIngressConsumer] MQTT_FORWARD dispatched traceId={} topic={} payloadBytes={}",
            envelope.getTraceId(), topic, rawBytes.length);
    }

    // ============================== RAW_INSERT ==============================

    /**
     * 处理 RAW_INSERT:直接调 link-api Feign 写 device_action 表(旁路 protocol handler 链路)。
     * 适合外部历史数据回填等场景 ── 不走物模型解析 / 时序入库,仅留 audit 痕迹。
     *
     * @param envelope 桥接消息封装
     */
    private void handleRawInsert(BridgeMessageEnvelope envelope) {
        if (StrUtil.isBlank(envelope.getDeviceIdentification())) {
            log.warn("[BridgeIngressConsumer] RAW_INSERT missing deviceIdentification traceId={} skip",
                envelope.getTraceId());
            return;
        }
        DeviceActionSaveVO vo = new DeviceActionSaveVO();
        vo.setDeviceIdentification(envelope.getDeviceIdentification());
        // 入站固定标记，方便筛选审计；同时把 envelope.actionType（如 INBOUND）带在 message 里
        vo.setActionType("BRIDGE_INBOUND");
        vo.setMessage(StrUtil.maxLength(StrUtil.nullToEmpty(envelope.getRawMessage()), 4000));
        vo.setStatus(1);
        vo.setRemark(StrUtil.format("trace={}", envelope.getTraceId()));

        R<?> result = deviceOpenInnerFacade.saveDeviceAction(vo);
        if (result == null || !result.getIsSuccess()) {
            String msg = result == null ? "null result" : result.getMsg();
            log.warn("[BridgeIngressConsumer] RAW_INSERT Feign saveDeviceAction failed traceId={} deviceId={} msg={}",
                envelope.getTraceId(), vo.getDeviceIdentification(), msg);
            // 抛出让 RocketMQ 重投（在 onTenantMessage 外层捕获）
            throw BizException.wrap("RAW_INSERT failed: " + msg);
        }
        log.info("[BridgeIngressConsumer] RAW_INSERT saved traceId={} deviceId={}",
            envelope.getTraceId(), vo.getDeviceIdentification());
    }

    // ============================== RULE_TRIGGER ==============================

    /**
     * 处理 RULE_TRIGGER:调 rule-api Feign 触发场景联动规则。
     * ruleIdentification 优先取 triggerRuleIdentification,兜底 triggerRuleId(见 resolveTriggerRule);
     * tenantId 来自 ContextUtil(已由父类恢复)。
     *
     * @param envelope 桥接消息封装
     */
    private void handleRuleTrigger(BridgeMessageEnvelope envelope) {
        String ruleIdentification = resolveTriggerRule(envelope);
        if (StrUtil.isBlank(ruleIdentification)) {
            log.warn("[BridgeIngressConsumer] RULE_TRIGGER missing triggerRuleIdentification traceId={} rawMessage={}",
                envelope.getTraceId(),
                envelope.getRawMessage());
            return;
        }
        Long tenantId = ContextUtil.getTenantId();
        if (tenantId == null && StrUtil.isNotBlank(envelope.getTenantId())) {
            try {
                tenantId = Long.valueOf(envelope.getTenantId());
            } catch (NumberFormatException ignore) {
                // 解析失败用 null，下游 Feign 自行处理
            }
        }
        try {
            R<?> result = ruleJobHandlerFacade.triggerRulePolicy(tenantId, ruleIdentification);
            if (result == null || !result.getIsSuccess()) {
                String msg = result == null ? "null result" : result.getMsg();
                log.warn("[BridgeIngressConsumer] RULE_TRIGGER Feign failed traceId={} rule={} msg={}",
                    envelope.getTraceId(), ruleIdentification, msg);
                return;  // 触发失败不重投（避免规则反复触发的副作用）
            }
            log.info("[BridgeIngressConsumer] RULE_TRIGGER fired traceId={} tenantId={} rule={}",
                envelope.getTraceId(), tenantId, ruleIdentification);
        } catch (Exception e) {
            log.error("[BridgeIngressConsumer] RULE_TRIGGER exception traceId={} rule={}",
                envelope.getTraceId(), ruleIdentification, e);
            // 不抛 ── 规则触发的副作用不可重复执行（重投放大故障）
        }
    }

    /**
     * 从 envelope.rawMessage(JSON)顶层解析触发规则标识:优先 triggerRuleIdentification(业务唯一编码),
     * 兜底 triggerRuleId(数字主键转字符串);无则返回 null。
     *
     * @param envelope 桥接消息封装
     * @return 触发规则标识;无则返回 null
     */
    private String resolveTriggerRule(BridgeMessageEnvelope envelope) {
        String rawMessage = envelope.getRawMessage();
        if (StrUtil.isBlank(rawMessage) || !JSON.isValidObject(rawMessage)) {
            return null;
        }
        JSONObject json = JSON.parseObject(rawMessage);
        String ident = json.getString("triggerRuleIdentification");
        if (StrUtil.isNotBlank(ident)) {
            return ident;
        }
        Object id = json.get("triggerRuleId");
        if (id != null) {
            return String.valueOf(id);
        }
        return null;
    }
}
