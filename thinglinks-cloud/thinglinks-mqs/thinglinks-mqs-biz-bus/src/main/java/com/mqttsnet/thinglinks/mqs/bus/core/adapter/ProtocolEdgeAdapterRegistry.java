package com.mqttsnet.thinglinks.mqs.bus.core.adapter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.bus.adapter.ProtocolEdgeAdapter;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 协议适配器注册表,启动时按 {@link ProtocolTypeEnum} 索引,运行时 O(1) 查找。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
public class ProtocolEdgeAdapterRegistry {

    private final Map<ProtocolTypeEnum, ProtocolEdgeAdapter> registry = new EnumMap<>(ProtocolTypeEnum.class);

    public ProtocolEdgeAdapterRegistry(List<ProtocolEdgeAdapter> adapters) {
        if (CollUtil.isEmpty(adapters)) {
            log.info("[bus.adapter.registry] no ProtocolEdgeAdapter bean found, "
                + "device-bus pipeline will be skipped");
            return;
        }
        for (ProtocolEdgeAdapter adapter : adapters) {
            ProtocolTypeEnum type = adapter.supports();
            ProtocolEdgeAdapter old = registry.put(type, adapter);
            if (old != null) {
                log.warn("[bus.adapter.registry] duplicate adapter for protocol={}: {} replaced {}", type, adapter.getClass().getSimpleName(), old.getClass().getSimpleName());
            }
        }
        log.info("[bus.adapter.registry] registered {} adapters: {}", registry.size(), registry.keySet());
    }

    /**
     * 按协议类型查找;未注册返 empty。
     */
    public Optional<ProtocolEdgeAdapter> find(ProtocolTypeEnum type) {
        return Optional.ofNullable(type).map(registry::get);
    }
}
