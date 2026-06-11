package com.mqttsnet.thinglinks.mqs.bus.core.route;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.adapter.ProtocolEdgeAdapter;
import com.mqttsnet.thinglinks.bus.route.RouteEntry;
import com.mqttsnet.thinglinks.bus.route.TopicRoute;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * Topic 路由解析器,启动扫描 {@link TopicRoute} adapter 建索引:EXACT O(1) + 模式 O(N) 遍历。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TopicRouteResolver {

    private final ApplicationContext applicationContext;

    private final Map<String, RouteEntry> exactIndex = new HashMap<>();
    private final List<RouteEntry> patternList = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        Map<String, ProtocolEdgeAdapter> adapters = applicationContext.getBeansOfType(ProtocolEdgeAdapter.class);
        if (CollUtil.isEmpty(adapters)) {
            log.info("[bus.route.resolver] no ProtocolEdgeAdapter bean, route table empty");
            return;
        }
        List<RouteEntry> patterns = new ArrayList<>();
        for (ProtocolEdgeAdapter adapter : adapters.values()) {
            TopicRoute annotation = AnnotationUtils.findAnnotation(adapter.getClass(), TopicRoute.class);
            if (annotation == null) {
                log.debug("[bus.route.resolver] adapter {} has no @TopicRoute, skip indexing",
                    adapter.getClass().getSimpleName());
                continue;
            }
            for (String topic : annotation.value()) {
                if (StrUtil.isBlank(topic)) {
                    continue;
                }
                RouteEntry entry = RouteEntry.of(topic, annotation.mode(), annotation.group(),
                    adapter, annotation.order());
                if (annotation.mode() == MatchModeEnum.EXACT) {
                    RouteEntry old = exactIndex.put(topic, entry);
                    if (old != null) {
                        log.warn("[bus.route.resolver] duplicate exact topic={} : {} replaced {}",
                            topic, adapter.getClass().getSimpleName(),
                            old.getAdapter().getClass().getSimpleName());
                    }
                } else {
                    patterns.add(entry);
                }
            }
        }
        patterns.sort(Comparator.comparingInt(RouteEntry::getOrder));
        patternList.addAll(patterns);
        log.info("[bus.route.resolver] indexed {} exact + {} pattern routes",
            exactIndex.size(), patternList.size());
    }

    /**
     * 按 topic 查找路由,EXACT 优先模式 fallback;未命中返 empty。
     */
    public Optional<RouteEntry> resolve(String topic) {
        if (StrUtil.isBlank(topic)) {
            return Optional.empty();
        }
        RouteEntry exact = exactIndex.get(topic);
        if (exact != null) {
            return Optional.of(exact);
        }
        for (RouteEntry entry : patternList) {
            if (entry.matches(topic)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }
}
