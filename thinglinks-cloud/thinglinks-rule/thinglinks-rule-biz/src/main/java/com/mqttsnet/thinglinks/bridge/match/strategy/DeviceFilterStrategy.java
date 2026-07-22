package com.mqttsnet.thinglinks.bridge.match.strategy;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 设备维度策略 ── deviceIdentifications / tagsAny / groupIds 三选一(任一命中即可)。
 *
 * <p><b>fail-closed</b>:tagsAny / groupIds 当前未接通 link Feign,配了即拒绝,
 * 防止"配了但默默放行 = 误投"。运维侧应改用 {@code deviceIdentifications} 显式列表。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
@Slf4j
@Component
@Order(BridgeMatchOrder.DEVICE)
public class DeviceFilterStrategy implements BridgeMatchStrategy {

    private static final String STRATEGY = "DEVICE";

    @Override
    public String name() {
        return STRATEGY;
    }

    @Override
    public boolean appliesTo(BridgeMatchConfig cfg) {
        return Optional.ofNullable(cfg)
                .map(BridgeMatchConfig::getDeviceFilter)
                .map(f -> CollUtil.isNotEmpty(f.getDeviceIdentifications())
                        || CollUtil.isNotEmpty(f.getTagsAny())
                        || CollUtil.isNotEmpty(f.getGroupIds()))
                .orElse(false);
    }

    @Override
    public MatchResult match(BridgeMessageEnvelope env, BridgeMatchConfig cfg) {
        return MatchResult.safe(STRATEGY, () -> {
            BridgeMatchConfig.DeviceFilter filter = cfg.getDeviceFilter();
            // tagsAny / groupIds 暂未实现 → fail-closed
            if (CollUtil.isNotEmpty(filter.getTagsAny()) || CollUtil.isNotEmpty(filter.getGroupIds())) {
                log.warn("[DeviceFilterStrategy] tagsAny/groupIds 未接通 link Feign,fail-closed");
                return MatchResult.miss("tagsAny / groupIds not yet implemented; use deviceIdentifications");
            }
            List<String> ids = filter.getDeviceIdentifications();
            if (CollUtil.isEmpty(ids)) {
                // appliesTo 已保证不会到这;兜底防御
                return MatchResult.hit("device filter empty");
            }
            String actual = Optional.ofNullable(env)
                    .map(BridgeMessageEnvelope::getDeviceIdentification)
                    .orElse(null);
            if (actual == null || actual.isBlank()) {
                return MatchResult.miss("envelope.deviceIdentification is blank");
            }
            return ids.stream().filter(Objects::nonNull).anyMatch(actual::equals)
                    ? MatchResult.hit("device matched: " + actual)
                    : MatchResult.miss("'" + actual + "' not in deviceIdentifications");
        });
    }
}
