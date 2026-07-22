package com.mqttsnet.thinglinks.bus.stage;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import lombok.Getter;

/**
 * Stage 间共享上下文,替代隐式 ThreadLocal,请求级显式作用域。
 * 内部 ConcurrentHashMap,允许 PRE/CORE 同步 + POST 异步并发读写。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
public class StageContext {

    private final DeviceProtocolEvent event;
    private final long startNanos;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>(8);

    private StageContext(DeviceProtocolEvent event, long startNanos) {
        this.event = event;
        this.startNanos = startNanos;
    }

    public static StageContext create(DeviceProtocolEvent event) {
        return new StageContext(event, System.nanoTime());
    }

    /**
     * 写属性,链式风格。value=null 移除。
     */
    public StageContext put(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
        return this;
    }

    /**
     * 读属性,类型不匹配返 empty。
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(attributes.get(key))
            .filter(type::isInstance)
            .map(type::cast);
    }

    /**
     * 只读视图。
     */
    public Map<String, Object> attributesView() {
        return MapUtil.isEmpty(attributes) ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
    }

    /**
     * 入口到当前的累计时延(ms)。
     */
    public long elapsedMs() {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
