package com.mqttsnet.thinglinks.enumeration.bus;

import java.util.Optional;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Topic 匹配模式 枚举 ── 对应 {@code TopicRoute#mode()} 取值。
 * </p>
 *
 * <h3>性能差异</h3>
 * <ul>
 *   <li>{@link #EXACT}:O(1) HashMap 查找,首选</li>
 *   <li>{@link #PREFIX}:O(N) 线性扫描,但 String#startsWith 极快</li>
 *   <li>{@link #ANT}:Spring AntPathMatcher,支持 *? 等通配符</li>
 *   <li>{@link #REGEX}:Java 正则,最灵活但最慢</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MatchModeEnum", description = "协议总线-Topic 匹配模式 枚举")
public enum MatchModeEnum {

    /**
     * 精确匹配:topic.equals(pattern),O(1) HashMap
     */
    EXACT("EXACT", "精确匹配"),

    /**
     * 前缀匹配:topic.startsWith(pattern)
     */
    PREFIX("PREFIX", "前缀匹配"),

    /**
     * Ant 风格通配:* 单级 / ** 多级 / ? 单字符
     */
    ANT("ANT", "Ant 通配"),

    /**
     * 正则:Pattern.matches(pattern, topic)
     */
    REGEX("REGEX", "正则匹配");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举。
     *
     * @param value 匹配模式 value(EXACT / PREFIX / ANT / REGEX)
     * @return 对应枚举的 Optional;空字符串或未匹配返回 {@link Optional#empty()}
     */
    public static Optional<MatchModeEnum> fromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }
        return Stream.of(values())
            .filter(e -> e.value.equalsIgnoreCase(value))
            .findFirst();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
