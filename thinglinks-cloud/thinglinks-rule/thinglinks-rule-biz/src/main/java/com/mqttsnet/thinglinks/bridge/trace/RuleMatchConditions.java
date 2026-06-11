package com.mqttsnet.thinglinks.bridge.trace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeMatchConfig;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RULE_MATCH trace step 的条件明细收集器,产出形如
 * <pre>{@code
 * { "hit": true, "kind": "PRODUCT", "matcher": "WILDCARD",
 *   "field": "productIdentifications", "expected": ["*"], "actual": "productA",
 *   "reason": "通配符 '*' 放行所有" }
 * }</pre>
 * 写入 {@code extensionJson.matchedConditions};{@code actual} / {@code reason} 可缺省。
 *
 * @author mqttsnet
 * @since 2026-05-12
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleMatchConditions {

    /**
     * 匹配维度(前后端常量约定)。前端按 kind 选图标 + 国际化标签。
     */
    public enum Kind {
        PRODUCT, ACTION_TYPE, TOPIC,
        DEVICE_ID, DEVICE_TAG, DEVICE_GROUP,
        PAYLOAD_FILTER, TIME_WINDOW
    }

    /**
     * 匹配方式(前后端常量约定)。前端按 matcher 渲染 chip 颜色 + 说明 tooltip。
     */
    public enum Matcher {
        EXACT, WILDCARD, ANY_IN_LIST,
        MQTT_TOPIC, JSON_PATH, SPEL, GROOVY, CRON
    }

    /**
     * 收集本次匹配命中的条件列表。失败 / 无配置 → 空列表;调用方拿到的是<b>不可变</b>列表。
     */
    public static List<Map<String, Object>> collect(DataBridgeCacheVO rule,
                                                    BridgeMessageEnvelope envelope) {
        if (rule == null) {
            return Collections.emptyList();
        }
        BridgeMatchConfig cfg = parseConfig(rule.getMatchConfigJson(), rule.getId());
        if (cfg == null) {
            return Collections.emptyList();
        }
        String productId = envelope == null ? null : envelope.getProductIdentification();
        String actionType = envelope == null ? null : envelope.getActionType();
        String topic = envelope == null ? null : envelope.getTopic();
        String deviceId = envelope == null ? null : envelope.getDeviceIdentification();
        List<Map<String, Object>> out = new ArrayList<>(8);
        try {
            appendListCondition(out, Kind.PRODUCT, "productIdentifications",
                    cfg.getProductIdentifications(), productId);
            appendListCondition(out, Kind.ACTION_TYPE, "actionTypes",
                    cfg.getActionTypes(), actionType);
            appendTopicCondition(out, cfg.getTopicPatterns(), topic);
            appendDeviceFilter(out, cfg.getDeviceFilter(), deviceId);
            appendPayloadFilter(out, cfg.getPayloadFilter());
            appendTimeWindow(out, cfg.getTimeWindow());
        } catch (Exception e) {
            // 单次收集失败不影响主链路,trace 显示当前已收集到的部分维度
            log.debug("[RuleMatchConditions] collect failed ruleId={}: {}",
                    rule.getId(), e.getMessage());
        }
        return Collections.unmodifiableList(out);
    }

    // ============================== 维度装配 ==============================

    /**
     * 列表型维度(产品 / 事件类型):含通配符 → WILDCARD;单元素 → EXACT;多元素 → ANY_IN_LIST。
     */
    private static void appendListCondition(List<Map<String, Object>> out,
                                            Kind kind, String field,
                                            List<String> expected, String actual) {
        if (CollUtil.isEmpty(expected)) {
            return;
        }
        String wildcard = expected.stream().filter(BridgeMatchConfig::isWildcard).findFirst().orElse(null);
        Matcher matcher = wildcard != null ? Matcher.WILDCARD
                : expected.size() == 1 ? Matcher.EXACT
                : Matcher.ANY_IN_LIST;
        String reason = wildcard != null
                ? "通配符 '" + wildcard + "' 放行所有"
                : "实际值 '" + StrUtil.nullToDefault(actual, "-") + "' 命中配置 " + expected;
        out.add(condition(kind, matcher, field, expected, actual, reason));
    }

    private static void appendTopicCondition(List<Map<String, Object>> out,
                                             List<String> patterns, String actual) {
        if (CollUtil.isEmpty(patterns)) {
            return;
        }
        String reason = StrUtil.isBlank(actual)
                ? "生命周期事件无 topic,topic 过滤不适用"
                : "topic '" + actual + "' 命中模式集合 " + patterns;
        out.add(condition(Kind.TOPIC, Matcher.MQTT_TOPIC, "topicPatterns",
                patterns, actual, reason));
    }

    private static void appendDeviceFilter(List<Map<String, Object>> out,
                                           BridgeMatchConfig.DeviceFilter df,
                                           String deviceId) {
        if (df == null) {
            return;
        }
        if (CollUtil.isNotEmpty(df.getDeviceIdentifications())) {
            out.add(condition(Kind.DEVICE_ID, Matcher.ANY_IN_LIST,
                    "deviceFilter.deviceIdentifications",
                    df.getDeviceIdentifications(), deviceId,
                    "设备 '" + StrUtil.nullToDefault(deviceId, "-") + "' 命中白名单"));
        }
        if (CollUtil.isNotEmpty(df.getTagsAny())) {
            out.add(condition(Kind.DEVICE_TAG, Matcher.ANY_IN_LIST,
                    "deviceFilter.tagsAny", df.getTagsAny(), null,
                    "设备含任一标签 " + df.getTagsAny()));
        }
        if (CollUtil.isNotEmpty(df.getGroupIds())) {
            out.add(condition(Kind.DEVICE_GROUP, Matcher.ANY_IN_LIST,
                    "deviceFilter.groupIds", df.getGroupIds(), null,
                    "设备属于分组 " + df.getGroupIds() + " 任一"));
        }
    }

    private static void appendPayloadFilter(List<Map<String, Object>> out,
                                            BridgeMatchConfig.PayloadFilter pf) {
        if (pf == null || StrUtil.isBlank(pf.getExpression())) {
            return;
        }
        Matcher matcher = parsePayloadMatcher(pf.getType());
        out.add(condition(Kind.PAYLOAD_FILTER, matcher, "payloadFilter",
                pf.getExpression(), null,
                StrUtil.blankToDefault(pf.getType(), "(未指定)") + " 表达式求值为真"));
    }

    private static void appendTimeWindow(List<Map<String, Object>> out,
                                         BridgeMatchConfig.TimeWindow tw) {
        if (tw == null || StrUtil.isBlank(tw.getCronExpr())) {
            return;
        }
        out.add(condition(Kind.TIME_WINDOW, Matcher.CRON, "timeWindow.cronExpr",
                tw.getCronExpr(), null,
                "当前时间落入 cron 窗口 " + tw.getCronExpr()));
    }

    // ============================== 工具 ==============================

    private static BridgeMatchConfig parseConfig(String json, Long ruleId) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JsonUtil.parse(json, BridgeMatchConfig.class);
        } catch (Exception e) {
            log.debug("[RuleMatchConditions] parse matchConfigJson failed ruleId={}: {}",
                    ruleId, e.getMessage());
            return null;
        }
    }

    /**
     * payloadFilter.type → Matcher 枚举;未知值降级为 SPEL,仅影响 UI 标签不影响匹配。
     */
    private static Matcher parsePayloadMatcher(String type) {
        if (StrUtil.isBlank(type)) {
            return Matcher.SPEL;
        }
        String normalized = StrUtil.replace(type.trim(), "_", "").toUpperCase();
        if ("JSONPATH".equals(normalized)) return Matcher.JSON_PATH;
        if ("GROOVY".equals(normalized)) return Matcher.GROOVY;
        return Matcher.SPEL;
    }

    /**
     * 构造单条 condition;LinkedHashMap 保证字段顺序固定,actual/reason 空值不进 JSON。
     */
    private static Map<String, Object> condition(Kind kind, Matcher matcher, String field,
                                                 Object expected, Object actual, String reason) {
        Map<String, Object> c = MapUtil.<String, Object>builder(new LinkedHashMap<>())
                .put("hit", true)
                .put("kind", kind.name())
                .put("matcher", matcher.name())
                .put("field", field)
                .put("expected", expected)
                .build();
        if (actual != null) {
            c.put("actual", actual);
        }
        if (StrUtil.isNotBlank(reason)) {
            c.put("reason", reason);
        }
        return c;
    }
}
