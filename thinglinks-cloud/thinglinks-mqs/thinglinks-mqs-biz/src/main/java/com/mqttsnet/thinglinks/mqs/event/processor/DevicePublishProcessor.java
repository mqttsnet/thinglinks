package com.mqttsnet.thinglinks.mqs.event.processor;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;
import com.mqttsnet.thinglinks.mqs.uplink.handler.TopicHandler;
import com.mqttsnet.thinglinks.mqs.uplink.handler.factory.TopicHandlerFactory;
import com.mqttsnet.thinglinks.mqs.transform.InboundScriptTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 设备 PUBLISH 处理器 ── 按 topic 路由到 {@link TopicHandlerFactory} 匹配的 {@link TopicHandler},
 * 完成持久化 + 物模型解析 + 时序入库。
 *
 * <p>只做数据上报持久化,不触发任何桥接出站:桥接外推是独立业务(由 biz-bus 主流程 BridgeRelayStage 的
 * POST 旁路负责),与数据上报解耦,避免桥接入站数据被二次外推。
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DevicePublishProcessor implements DeviceEventProcessor {

    private final TopicHandlerFactory mqttTopicHandlerFactory;
    private final InboundScriptTransformer inboundScriptTransformer;

    @Override
    public boolean supports(DeviceActionTypeEnum type) {
        return DeviceActionTypeEnum.PUBLISH == type;
    }

    @Override
    public void process(CommonDeviceEvent event) {
        // 仅做数据上报持久化。上行计数不在此处:真实设备上行由 bus 主流程 DeviceBizDispatchStage 收口计数,
        // 桥接入站(BridgeIngressRocketmqConsumerHandler)直调本方法、按约定不计上行。
        firePersistenceAndModel(event);
    }

    /**
     * 按 topic 路由到对应 {@link TopicHandler} ── 持久化 + 物模型解析 + 时序入库;无匹配 handler 时 warn 跳过。
     *
     * @param event 设备通用事件
     */
    private void firePersistenceAndModel(CommonDeviceEvent event) {
        if (StrUtil.isBlank(event.getTopic())) {
            log.warn("[DevicePublish] missing topic, skip persistence clientId={}", event.getClientId());
            return;
        }
        // 前置转换:厂商私有 topic/报文经绑定的 Groovy 脚本转为平台标准报文(改写 topic + payload);
        // 未命中绑定脚本则原样透传。转换异常一律降级透传,不阻断上行。
        UplinkMessageEventSource source = inboundScriptTransformer.resolveEventSource(event);
        String topic = source.getTopic();
        TopicHandler handler = mqttTopicHandlerFactory.findMatchingHandler(topic);
        if (handler == null) {
            log.warn("[DevicePublish] no topic handler matched, skip clientId={} topic={}", event.getClientId(), topic);
            return;
        }
        log.info("[DevicePublish] dispatch handler={} protocol={} topic={} qos={} payloadLen={}",
            handler.getClass().getSimpleName(), event.getProtocolType(), topic, event.getQos(),
            StringUtils.length(source.getPayload()));
        handler.handle(source);
    }
}
