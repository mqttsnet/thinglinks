package com.mqttsnet.thinglinks.bus.hook;

import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import org.springframework.core.Ordered;

/**
 * 事件富化器 SPI,PRE 阶段之前基于 clientId / topic 查 Cache / DB / Feign 补齐字段。
 * 全管道仅富化一次,后续 stage 直接读 event / context,无需重复 IO。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface DeviceEventEnricher extends Ordered {

    void enrich(DeviceProtocolEvent event, StageContext context);

    /**
     * 多 enricher 排序,数字越小越先(典型:DeviceCacheEnricher=100)。
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
