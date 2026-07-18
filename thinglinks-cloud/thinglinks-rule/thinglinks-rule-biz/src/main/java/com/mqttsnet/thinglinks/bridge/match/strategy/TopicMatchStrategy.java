package com.mqttsnet.thinglinks.bridge.match.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.utils.topic.MqttTopicMatcher;
import com.mqttsnet.basic.utils.topic.TopicPlaceholders;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Topic 维度策略 ── 对齐 ACL {@code DeviceAclRule.topicPattern} 设计:
 * <ol>
 *   <li>{@link BridgeMatchConfig#getTopicPatterns()} 是用户在前端通过"选择主题"按钮多选产品下
 *       ProductTopic 后绑定的 MQTT 主题模板列表(可含 {@code +}/{@code #} 通配符 + {@code ${}} 占位符)</li>
 *   <li>空 / null 列表 → 不约束 topic,默认放行</li>
 *   <li>非空 → 对每条 pattern 用 {@link TopicPlaceholders#replaceWithWildcard} 替换占位符为
 *       MQTT 单层通配符 {@code +},再用 {@link MqttTopicMatcher} 与 envelope.topic 比对;
 *       任一命中即视为通过</li>
 * </ol>
 *
 * <p><b>设计原则</b>:与 BifroMQ 认证插件 ACL 鉴权使用同一份 {@link MqttTopicMatcher} +
 * {@link TopicPlaceholders}(ThingLinks Util 下沉),前后端语义 1:1 对齐,避免重复实现。
 *
 * @author mqttsnet
 * @since 2026-05-09
 */
@Component
@Order(BridgeMatchOrder.TOPIC)
public class TopicMatchStrategy implements BridgeMatchStrategy {

    private static final String STRATEGY = "TOPIC";

    @Override
    public String name() {
        return STRATEGY;
    }

    @Override
    public boolean appliesTo(BridgeMatchConfig cfg) {
        return Optional.ofNullable(cfg)
                .flatMap(BridgeMatchConfig::topicPatternsOpt)
                .filter(CollUtil::isNotEmpty)
                .isPresent();
    }

    @Override
    public MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        return MatchResult.safe(STRATEGY, () -> {
            List<String> patterns = cfg.getTopicPatterns();
            String topic = Optional.ofNullable(env).map(BridgeMessageEnvelope::getTopic).orElse(null);

            if (CollUtil.isEmpty(patterns)) {
                // 不约束 topic,直接命中
                return MatchResult.hit("no topicPatterns configured, pass-through");
            }
            // 无 topic 的生命周期事件(CONNECT / DISCONNECT / CLOSE / PING / HEART_TIMEOUT / ...)
            // 协议层就没 topic;此时 topic 过滤不适用,放行 ── 让 ActionTypeMatchStrategy 控制是否命中。
            // 否则用户配 topicPatterns(为约束 PUBLISH 事件)会误杀同规则下的 DISCONNECT/CONNECT 等事件。
            if (StrUtil.isBlank(topic)) {
                return MatchResult.hit("envelope has no topic (lifecycle event), topic filter N/A");
            }

            // 逐条 pattern: 占位符 ${...} → + → MQTT 通配匹配
            for (String pattern : patterns) {
                if (StrUtil.isBlank(pattern)) {
                    continue;
                }
                String mqttPattern = TopicPlaceholders.replaceWithWildcard(pattern);
                if (MqttTopicMatcher.match(mqttPattern, topic)) {
                    return MatchResult.hit("pattern '" + pattern + "' (resolved: '" + mqttPattern
                            + "') matched topic '" + topic + "'");
                }
            }
            return MatchResult.miss("none of " + patterns.size() + " topicPatterns matched topic '" + topic + "'");
        });
    }
}
