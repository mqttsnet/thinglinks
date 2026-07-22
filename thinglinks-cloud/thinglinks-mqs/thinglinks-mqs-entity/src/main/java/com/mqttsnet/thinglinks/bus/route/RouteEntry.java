package com.mqttsnet.thinglinks.bus.route;

import java.util.regex.Pattern;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.adapter.ProtocolEdgeAdapter;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.MatchModeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;

/**
 * 单条路由记录,持 topic 模式 + 匹配模式 + adapter + 分组 + 优先级。
 * 由 {@code TopicRouteResolver} 启动扫描 {@link TopicRoute} 构建,运行时只读。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@RequiredArgsConstructor
public class RouteEntry {

    private static final AntPathMatcher ANT_MATCHER = new AntPathMatcher();

    private final String pattern;
    private final MatchModeEnum mode;
    private final DispatchGroupEnum group;
    private final ProtocolEdgeAdapter adapter;
    private final int order;
    /**
     * REGEX 模式下预编译,其他模式 null。
     */
    private final Pattern regexPattern;

    public static RouteEntry of(String pattern, MatchModeEnum mode, DispatchGroupEnum group,
                                ProtocolEdgeAdapter adapter, int order) {
        Pattern regex = (mode == MatchModeEnum.REGEX) ? Pattern.compile(pattern) : null;
        return new RouteEntry(pattern, mode, group, adapter, order, regex);
    }

    /**
     * EXACT 模式由 Resolver 走 HashMap 直接命中,本方法主要给 PREFIX/ANT/REGEX 用。
     */
    public boolean matches(String topic) {
        if (StrUtil.isBlank(topic)) {
            return false;
        }
        return switch (mode) {
            case EXACT -> pattern.equals(topic);
            case PREFIX -> topic.startsWith(pattern);
            case ANT -> ANT_MATCHER.match(pattern, topic);
            case REGEX -> regexPattern != null && regexPattern.matcher(topic).matches();
        };
    }
}
