package com.mqttsnet.thinglinks.bridge.matcher;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 桥接规则匹配条件 DTO({@code data_bridge_rule.match_config_json} 反序列化目标)。
 *
 * <p>所有字段都是可选,空字段表示"不约束此维度,全部命中"。
 * <ul>
 *   <li>出站:productIdentifications / actionTypes / topicPatterns 等</li>
 *   <li>入站:subscriptionSourceIds / messageFilter(结构异构)</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BridgeMatchConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ============================== 出站规则字段 ==============================

    /**
     * 产品标识列表。{@code ["productA","productB"]} 任一命中即可;
     * {@code ["*"]} / {@code ["all"]} 通配;空 / null 表示"未约束"。
     */
    private List<String> productIdentifications;

    /**
     * 设备动作类型列表(复用字典 {@code LINK_DEVICE_ACTION_TYPE})。
     * 任一命中即可;{@code ["*"]} / {@code ["all"]} 通配。
     */
    private List<String> actionTypes;

    /**
     * 报文数据形态列表 ── {@code RAW}(原始报文)/ {@code THING_MODEL}(物模型匹配后的结构化数据)。
     * <p>null / 空 = 默认只消费 {@code RAW}(避免存量规则把物模型事件重复处理);
     * 要桥接物模型数据须显式声明含 {@code THING_MODEL}。
     */
    private List<String> payloadKinds;

    /**
     * Topic 模式列表(对齐 ACL {@code DeviceAclRule.topicPattern})。
     * <p>null / 空 = 不约束;非空 = envelope.topic 命中任一模式即通过。
     * 每条模式支持:字面量精确 / MQTT 通配符({@code +} 单层 / {@code #} 末尾多层) /
     * 平台占位符({@code ${app_id}} / {@code ${device_identification}} 等,统一替换为 {@code +})。
     */
    private List<String> topicPatterns;

    /**
     * 设备维度过滤器(高级)。
     */
    private DeviceFilter deviceFilter;

    /**
     * 内容过滤器(高级;JSONPath / SpEL / Groovy)。
     */
    private PayloadFilter payloadFilter;

    /**
     * 时间窗口(高级;cron 表达式)。
     */
    private TimeWindow timeWindow;

    // ============================== 入站规则字段 ==============================

    /**
     * 关联订阅源 ID 列表。
     */
    private List<Long> subscriptionSourceIds;

    /**
     * 入站消息过滤器(高级)。
     */
    private PayloadFilter messageFilter;

    // ============================== 内嵌 DTO ==============================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceFilter implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 指定设备标识(任一命中)。
         */
        private List<String> deviceIdentifications;
        /**
         * 设备标签(任一命中)。
         */
        private List<String> tagsAny;
        /**
         * 设备分组(任一命中)。
         */
        private List<Long> groupIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayloadFilter implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 过滤类型:JSON_PATH / SPEL / GROOVY。
         */
        private String type;
        /**
         * 过滤表达式(按 type 解释)。
         */
        private String expression;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeWindow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * cron 表达式(如 {@code "0 0 9-18 ? * MON-FRI"} 工作日 9-18 点)。
         */
        private String cronExpr;
    }

    // ============================== 工具 ==============================

    /**
     * 通配符 {@code "*"}(MQTT 行业惯例)。{@link BizConstant#ALL} 是项目全局通配。
     */
    public static final String WILDCARD_STAR = "*";

    /**
     * 是否为通配符:{@code "*"} 或 {@link BizConstant#ALL}({@code "all"})。
     */
    public static boolean isWildcard(String v) {
        return WILDCARD_STAR.equals(v) || BizConstant.ALL.equals(v);
    }

    /**
     * 列表-字符串 any-match 语义统一封装:
     * 通配符直接放行 → actual 空白不命中 → expected 包含 actual(过滤 null)。
     */
    public static boolean matchesAny(List<String> expected, String actual) {
        if (expected == null || expected.isEmpty()) {
            return false;
        }
        if (expected.stream().filter(Objects::nonNull).anyMatch(BridgeMatchConfig::isWildcard)) {
            return true;
        }
        if (actual == null || actual.isBlank()) {
            return false;
        }
        return expected.stream().filter(Objects::nonNull).anyMatch(actual::equals);
    }

    public Optional<List<String>>  topicPatternsOpt() { return Optional.ofNullable(topicPatterns).filter(l -> !l.isEmpty()); }
    public Optional<DeviceFilter>  deviceFilterOpt()  { return Optional.ofNullable(deviceFilter); }
    public Optional<PayloadFilter> payloadFilterOpt() { return Optional.ofNullable(payloadFilter); }
    public Optional<TimeWindow>    timeWindowOpt()    { return Optional.ofNullable(timeWindow); }
}
