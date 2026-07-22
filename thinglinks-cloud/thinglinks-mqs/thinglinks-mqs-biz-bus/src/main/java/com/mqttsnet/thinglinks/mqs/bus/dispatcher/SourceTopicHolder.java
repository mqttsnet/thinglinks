package com.mqttsnet.thinglinks.mqs.bus.dispatcher;

import java.util.Optional;

/**
 * 来源 topic ThreadLocal 持有者。Kafka consumer 入口 set,adapter 读,finally 必 clear。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public final class SourceTopicHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private SourceTopicHolder() {
    }

    public static void set(String topic) {
        HOLDER.set(topic);
    }

    public static Optional<String> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
