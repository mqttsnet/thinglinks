package com.mqttsnet.thinglinks.bridge.match.strategy;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 动作类型(actionType)维度策略。配置列表为空 → skip;含 {@code *} / {@code all}({@link BizConstant#ALL}) → hit;
 * 否则 envelope.actionType 必须在列表内。值域参考 {@code DeviceActionTypeEnum}。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Component
@Order(BridgeMatchOrder.ACTION_TYPE)
public class ActionTypeMatchStrategy implements BridgeMatchStrategy {

    private static final String STRATEGY = "ACTION_TYPE";

    @Override
    public String name() {
        return STRATEGY;
    }

    @Override
    public boolean appliesTo(BridgeMatchConfig cfg) {
        return cfg != null && CollUtil.isNotEmpty(cfg.getActionTypes());
    }

    @Override
    public MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        return MatchResult.safe(STRATEGY, () -> {
            String actual = Optional.ofNullable(env)
                    .map(BridgeMessageEnvelope::getActionType)
                    .orElse(null);
            return BridgeMatchConfig.matchesAny(cfg.getActionTypes(), actual)
                    ? MatchResult.hit("matched: " + actual)
                    : MatchResult.miss("'" + actual + "' not in " + cfg.getActionTypes());
        });
    }
}
