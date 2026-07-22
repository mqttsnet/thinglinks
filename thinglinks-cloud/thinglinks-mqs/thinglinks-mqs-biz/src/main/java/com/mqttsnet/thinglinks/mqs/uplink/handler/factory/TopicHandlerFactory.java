package com.mqttsnet.thinglinks.mqs.uplink.handler.factory;


import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.mqs.uplink.handler.DefaultHandler;
import com.mqttsnet.thinglinks.mqs.uplink.handler.TopicHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud
 * @description: 设备上行 Topic 处理工厂类(协议中立:MQTT / WS / TCP 上行经统一主流程均路由至此)。
 * 通过 Spring 自动发现所有 {@link TopicHandler} Bean,凡 {@link TopicHandler#topicPattern()} 非空者
 * 即按 {@link TopicHandler#order()} 升序自动注册到路由表;新增厂商 topic 处理器只需新增一个
 * {@code @Component}/{@code @Service} 实现并声明正则,无需改动本工厂。{@link DefaultHandler} 不参与
 * 正则匹配,作为未命中时的显式兜底处理器。
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:00
 **/
@Slf4j
@Component
public class TopicHandlerFactory {

    private final List<TopicHandler> topicHandlers;
    private final DefaultHandler defaultHandler;
    private List<TopicHandlerEntry> topicHandlerEntries;

    public TopicHandlerFactory(List<TopicHandler> topicHandlers, DefaultHandler defaultHandler) {
        this.topicHandlers = topicHandlers;
        this.defaultHandler = defaultHandler;
    }

    // Use the @PostConstruct annotation to initialize the handler entries list after the dependencies are injected.
    @PostConstruct
    public void initTopicHandlerEntries() {
        topicHandlerEntries = topicHandlers.stream()
            .filter(handler -> StrUtil.isNotBlank(handler.topicPattern()))
            .sorted(Comparator.comparingInt(TopicHandler::order))
            .map(handler -> new TopicHandlerEntry(Pattern.compile(handler.topicPattern()), handler))
            .collect(Collectors.toList());
        log.info("[TopicHandlerFactory] registered {} topic handler(s) by auto-discovery", topicHandlerEntries.size());
    }

    // This method searches for a matching handler based on the topic.
    public TopicHandler findMatchingHandler(String topic) {
        for (TopicHandlerEntry handlerEntry : topicHandlerEntries) {
            Matcher matcher = handlerEntry.getPattern().matcher(topic);
            if (matcher.matches()) {
                return handlerEntry.getHandler();
            }
        }
        return defaultHandler;
    }

    private static final class TopicHandlerEntry {
        private final Pattern pattern;
        private final TopicHandler handler;

        public TopicHandlerEntry(Pattern pattern, TopicHandler handler) {
            this.pattern = pattern;
            this.handler = handler;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public TopicHandler getHandler() {
            return handler;
        }
    }
}
