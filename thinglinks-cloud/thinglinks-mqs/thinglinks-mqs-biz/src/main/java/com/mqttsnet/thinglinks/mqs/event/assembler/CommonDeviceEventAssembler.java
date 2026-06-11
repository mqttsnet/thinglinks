package com.mqttsnet.thinglinks.mqs.event.assembler;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 协议事件 → 领域事件装配器 ── 把 bus 已规范化的 {@link DeviceProtocolEvent} 直映射为 {@link CommonDeviceEvent}。
 * 身份字段(appId/deviceId/productId)已由 bus DeviceCacheEnricher 富化进 event,本类不再 parse JSON / 查缓存。
 *
 * @author mqttsnet
 * @since 2026-06-02
 */
@Component
@Slf4j
public class CommonDeviceEventAssembler {

    /**
     * 装配领域事件;eventType / tenantId 缺失或 actionType 未知返 {@link Optional#empty()}。
     *
     * @param event bus 协议事件(身份字段已富化)
     * @return 领域事件,非法返空
     */
    public Optional<CommonDeviceEvent> assemble(DeviceProtocolEvent event) {
        if (event == null || StrUtil.isBlank(event.getEventType()) || StrUtil.isBlank(event.getTenantId())) {
            log.warn("[CommonDeviceEventAssembler] eventType or tenantId missing, skip eventType={}",
                event == null ? null : event.getEventType());
            return Optional.empty();
        }
        Optional<DeviceActionTypeEnum> typeOpt = DeviceActionTypeEnum.fromValue(event.getEventType());
        if (typeOpt.isEmpty()) {
            log.info("[CommonDeviceEventAssembler] unknown actionType={}, event dropped", event.getEventType());
            return Optional.empty();
        }
        return Optional.of(CommonDeviceEvent.builder()
            .protocolType(ProtocolTypeEnum.fromValue(event.getProtocolType()).orElse(null))
            .actionType(typeOpt.get())
            .clientId(event.getClientId())
            .tenantId(event.getTenantId())
            .appId(event.getAppId())
            .deviceIdentification(event.getDeviceIdentification())
            .productIdentification(event.getProductIdentification())
            .topic(event.getTopic())
            .qos(event.getQos())
            .payload(event.getPayload())
            .payloadHex(event.getPayloadHex())
            .encoding(event.getEncoding())
            .originalSize(event.getOriginalSize())
            .ts(event.getEventUtc())
            .eventHlc(event.getEventHlc())
            .eventUtc(event.getEventUtc())
            .rawMessage(event.getRawMessage())
            .deviceCache(event.getDeviceCache())
            .build());
    }
}
