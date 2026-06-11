package com.mqttsnet.thinglinks.bridge.matcher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.bridge.match.BridgeMatchStrategyChain;
import com.mqttsnet.thinglinks.bridge.trace.BridgeTraceBuilder;
import com.mqttsnet.thinglinks.cache.helper.RuleCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.enumeration.bridge.BridgeDirectionEnum;
import com.mqttsnet.thinglinks.service.bridge.DataBridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 桥接规则匹配引擎 ── 薄外壳,各匹配维度委托 {@link BridgeMatchStrategyChain}。
 *
 * <p>从 cache 取当前租户/方向启用规则,逐条调 chain 匹配,返回命中规则(按 priority 升序)。
 * 无可变状态,线程安全。
 *
 * @author mqttsnet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BridgeRuleMatcher {

    private final RuleCacheDataHelper ruleCacheDataHelper;
    private final DataBridgeService dataBridgeService;
    private final BridgeMatchStrategyChain strategyChain;

    /**
     * 匹配出站规则(设备事件 → 第三方);按 priority 升序;永不返 null。
     */
    public List<DataBridgeCacheVO> matchOutbound(BridgeMessageEnvelope envelope) {
        if (envelope == null) {
            return List.of();
        }
        String appId = envelope.getAppId();
        String direction = BridgeDirectionEnum.OUTBOUND.getValue();
        List<DataBridgeCacheVO> rules = ruleCacheDataHelper.getBridgeEnabledRules(
                appId, direction,
                v -> dataBridgeService.getEnabledRules(appId, direction));
        if (CollUtil.isEmpty(rules)) {
            log.info("[BridgeRuleMatcher] no outbound rules tenantId={} appId={} clientId={} action={} topic={}",
                    envelope.getTenantId(), appId, envelope.getClientId(),
                    envelope.getActionType(), envelope.getTopic());
            return List.of();
        }
        List<DataBridgeCacheVO> hits = rules.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEnable()))
                .filter(r -> matchesEnvelope(r, envelope))
                .sorted(Comparator.comparingInt(r -> Optional.ofNullable(r.getPriority()).orElse(100)))
                .toList();
        if (hits.isEmpty()) {
            log.debug("[BridgeRuleMatcher] {} rules loaded but 0 matched tenantId={} appId={} clientId={} action={} topic={}",
                    rules.size(), envelope.getTenantId(), appId, envelope.getClientId(),
                    envelope.getActionType(), envelope.getTopic());
        }
        return hits;
    }

    /**
     * 单条规则匹配(热路径不写 trace)。
     */
    public boolean matchesEnvelope(DataBridgeCacheVO rule, BridgeMessageEnvelope envelope) {
        BridgeMatchConfig cfg = parseMatchConfig(rule.getMatchConfigJson(), rule.getId());
        return cfg != null && matchesPayloadKind(cfg, envelope) && strategyChain.match(envelope, cfg);
    }

    /**
     * 单条规则匹配 + 写 trace step(详情页"链路回放"用)。
     */
    public boolean matchesEnvelopeWithTrace(DataBridgeCacheVO rule, BridgeMessageEnvelope envelope,
                                            BridgeTraceBuilder trace) {
        BridgeMatchConfig cfg = parseMatchConfig(rule.getMatchConfigJson(), rule.getId());
        return cfg != null && matchesPayloadKind(cfg, envelope) && strategyChain.matchWithTrace(envelope, cfg, trace);
    }

    /**
     * 数据形态过滤 ── matchConfig.payloadKinds 缺省只吃 {@code RAW};声明了才吃对应形态。
     * 避免存量规则把新增的物模型({@code THING_MODEL})事件重复处理。
     */
    private boolean matchesPayloadKind(BridgeMatchConfig cfg, BridgeMessageEnvelope envelope) {
        String kind = StrUtil.blankToDefault(envelope.getPayloadKind(), BridgeMessageEnvelope.PAYLOAD_KIND_RAW);
        List<String> kinds = cfg.getPayloadKinds();
        if (CollUtil.isEmpty(kinds)) {
            return BridgeMessageEnvelope.PAYLOAD_KIND_RAW.equals(kind);
        }
        return kinds.contains(kind);
    }

    /**
     * matchConfigJson 反序列化;空配置 = 默认放行,异常 = 不命中。
     */
    private BridgeMatchConfig parseMatchConfig(String json, Long ruleId) {
        if (StrUtil.isBlank(json)) {
            return BridgeMatchConfig.builder().build();
        }
        try {
            return JsonUtil.parse(json, BridgeMatchConfig.class);
        } catch (Exception e) {
            log.error("[BridgeRuleMatcher] parse matchConfigJson failed ruleId={} json={}",
                    ruleId, StrUtil.maxLength(json, 200), e);
            return null;
        }
    }
}
