package com.mqttsnet.thinglinks.mqs.bus.protocol;

import com.mqttsnet.thinglinks.bus.adapter.AbstractProtocolEdgeAdapter;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.SourceTopicHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 边缘适配器抽象基类。子类只需声明协议类型 + {@code @TopicRoute}。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Slf4j
public abstract class AbstractKafkaEdgeAdapter extends AbstractProtocolEdgeAdapter {

    @Override
    protected DeviceProtocolEvent doCanonicalize(Object rawSource) {
        if (!(rawSource instanceof String json)) {
            throw new IllegalArgumentException("[" + supports().getValue() + ".adapter] expects String JSON, got "
                + (rawSource == null ? "null" : rawSource.getClass().getName()));
        }
        return ProtocolKafkaPayloadParser.parse(json, SourceTopicHolder.current().orElse(null), supports());
    }
}
