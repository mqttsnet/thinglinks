package com.mqttsnet.thinglinks.bridge.consumer;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.rocketmq.listener.AbstractTenantAwareRocketmqListener;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.service.execution.trigger.DeviceLatestSnapshotService;
import com.mqttsnet.thinglinks.service.execution.trigger.RuleTriggerIndexService;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 规则引擎事件触发消费者 ── 设备事件到达即评估,替代"XXL-Job 轮询 + 数据收集池"的准实时模型。
 *
 * <p>与 {@link BridgeDeviceEventConsumer}(桥接 Sink 分发)同 topic 不同消费组,各收全量事件:
 * <ul>
 *   <li><b>PUBLISH + THING_MODEL</b> → 维护设备最新快照 → 反查设备属性触发规则 → 逐规则评估
 *       (左值取消息内值,见 {@code DevicePropertiesPolicy#resolveEvaluationSource});</li>
 *   <li><b>PUBLISH + RAW</b> → 跳过(同一次上报的另一形态,避免双评估);</li>
 *   <li><b>生命周期事件</b>(CONNECT/DISCONNECT/…)→ 反查设备动作触发规则 → 逐规则评估
 *       (取值沿用设备动作池路径,池由 link 在动作落库时维护,不在 mqs 热路径)。</li>
 * </ul>
 *
 * <p>无命中规则的事件只消耗一次索引查询(进程内缓存兜底,见 {@link RuleTriggerIndexService});
 * 单规则评估失败不抛出(不触发 MQ 重投,动作重放风险大于漏评估,由执行日志兜底排查)。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(
    topic = BizMqRouteConstant.Bridge.DEVICE_EVENT,
    consumerGroup = BizMqRouteConstant.Groups.RULE_TRIGGER_EVENT,
    selectorExpression = "*",
    messageModel = MessageModel.CLUSTERING,
    consumeMode = ConsumeMode.CONCURRENTLY
)
public class RuleTriggerEventConsumer extends AbstractTenantAwareRocketmqListener<BridgeMessageEnvelope> {

    /** PUBLISH 动作(设备数据上报),与 DeviceActionTypeEnum.PUBLISH.getValue() 对齐 */
    private static final String ACTION_PUBLISH = "PUBLISH";

    private final RuleTriggerIndexService ruleTriggerIndexService;
    private final DeviceLatestSnapshotService deviceLatestSnapshotService;
    private final RuleService ruleService;

    @Override
    protected Class<BridgeMessageEnvelope> getBodyClass() {
        return BridgeMessageEnvelope.class;
    }

    @Override
    protected void onTenantMessage(BridgeMessageEnvelope envelope, MessageExt raw) {
        if (envelope == null || StrUtil.hasBlank(envelope.getProductIdentification(), envelope.getDeviceIdentification())) {
            log.warn("[RuleTriggerEvent] invalid envelope, skip. msgId={} traceId={} product={} device={} action={} payloadKind={} topic={}",
                    raw.getMsgId(),
                    envelope == null ? null : envelope.getTraceId(),
                    envelope == null ? null : envelope.getProductIdentification(),
                    envelope == null ? null : envelope.getDeviceIdentification(),
                    envelope == null ? null : envelope.getActionType(),
                    envelope == null ? null : envelope.getPayloadKind(),
                    envelope == null ? null : envelope.getTopic());
            return;
        }
        String actionType = envelope.getActionType();
        log.info("[RuleTriggerEvent] consume msgId={} traceId={} product={} device={} action={} payloadKind={} topic={}",
                raw.getMsgId(), envelope.getTraceId(), envelope.getProductIdentification(),
                envelope.getDeviceIdentification(), actionType, envelope.getPayloadKind(), envelope.getTopic());
        if (ACTION_PUBLISH.equalsIgnoreCase(actionType)) {
            // 同一次上报出站两路形态,按用途分流评估、互不重复:
            //   THING_MODEL(物模型匹配成功才有)→ 属性触发;
            //   RAW(每次上报恰好一条,不依赖物模型)→ 动作触发("动作==数据上行(PUBLISH)"是合法条件)。
            if (BridgeMessageEnvelope.PAYLOAD_KIND_THING_MODEL.equals(envelope.getPayloadKind())) {
                onThingModelReport(envelope);
            } else {
                onLifecycleAction(envelope);
            }
            return;
        }
        onLifecycleAction(envelope);
    }

    /** 设备属性上报:快照维护 + 属性触发规则评估 */
    private void onThingModelReport(BridgeMessageEnvelope envelope) {
        if (StrUtil.isBlank(envelope.getRawMessage())) {
            log.warn("[RuleTriggerEvent] THING_MODEL without rawMessage, skip. device={}",
                    envelope.getDeviceIdentification());
            return;
        }
        Long eventUtc = envelope.getEventUtc() != null ? envelope.getEventUtc() : envelope.getTs();
        // 最新快照供"定时规则里的属性条件/跨设备条件"读取,写在 rule 侧,mqs 热路径零负担
        deviceLatestSnapshotService.updateLatest(envelope.getProductIdentification(),
                envelope.getDeviceIdentification(), eventUtc, envelope.getRawMessage());

        List<String> rules = ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue(),
                envelope.getProductIdentification(), envelope.getDeviceIdentification());
        if (CollUtil.isEmpty(rules)) {
            log.info("[RuleTriggerEvent] no property rules traceId={} product={} device={} action={} payloadKind={}",
                    envelope.getTraceId(), envelope.getProductIdentification(),
                    envelope.getDeviceIdentification(), envelope.getActionType(), envelope.getPayloadKind());
            return;
        }

        ProductResultVO thingModel;
        try {
            thingModel = JSON.parseObject(envelope.getRawMessage(), ProductResultVO.class);
        } catch (Exception e) {
            log.warn("[RuleTriggerEvent] parse THING_MODEL failed device={} err={}",
                    envelope.getDeviceIdentification(), e.getMessage());
            return;
        }
        TriggerEventDTO triggerEvent = TriggerEventDTO.builder()
                .productIdentification(envelope.getProductIdentification())
                .deviceIdentification(envelope.getDeviceIdentification())
                .eventUtc(eventUtc)
                .actionType(envelope.getActionType())
                .payloadKind(envelope.getPayloadKind())
                .rawMessage(envelope.getRawMessage())
                .thingModel(thingModel)
                .build();
        dispatch(rules, ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue(), triggerEvent, envelope);
    }

    /** 设备生命周期事件:动作触发规则评估(取值沿用动作池路径,事件仅提供实时触发时机) */
    private void onLifecycleAction(BridgeMessageEnvelope envelope) {
        List<String> rules = ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue(),
                envelope.getProductIdentification(), envelope.getDeviceIdentification());
        if (CollUtil.isEmpty(rules)) {
            log.info("[RuleTriggerEvent] no action rules traceId={} product={} device={} action={} payloadKind={}",
                    envelope.getTraceId(), envelope.getProductIdentification(),
                    envelope.getDeviceIdentification(), envelope.getActionType(), envelope.getPayloadKind());
            return;
        }
        TriggerEventDTO triggerEvent = TriggerEventDTO.builder()
                .productIdentification(envelope.getProductIdentification())
                .deviceIdentification(envelope.getDeviceIdentification())
                .eventUtc(envelope.getEventUtc() != null ? envelope.getEventUtc() : envelope.getTs())
                .actionType(envelope.getActionType())
                .payloadKind(envelope.getPayloadKind())
                .build();
        dispatch(rules, ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue(), triggerEvent, envelope);
    }

    private void dispatch(List<String> rules, Integer conditionType, TriggerEventDTO triggerEvent,
                          BridgeMessageEnvelope envelope) {
        Long tenantId = ContextUtil.getTenantId();
        log.info("[RuleTriggerEvent] matched {} rules traceId={} device={} action={} type={}",
                rules.size(), envelope.getTraceId(), envelope.getDeviceIdentification(),
                envelope.getActionType(), conditionType);
        for (String ruleIdentification : rules) {
            ruleService.triggerRulePolicyForEvent(tenantId, ruleIdentification, conditionType, triggerEvent);
        }
    }
}
