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
 * 单 stage 执行状态 枚举 ── 用于 {@code StageRecord#getStatus()}。
 * </p>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "StageStatusEnum", description = "协议总线-单 stage 执行状态 枚举")
public enum StageStatusEnum {

    /**
     * 执行成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 执行失败(抛异常)
     */
    FAILED("FAILED", "失败"),

    /**
     * 主动跳过(supports=false / guard=false)
     */
    SKIPPED("SKIPPED", "跳过");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举。
     *
     * @param value stage 状态 value(SUCCESS / FAILED / SKIPPED)
     * @return 对应枚举的 Optional;空字符串或未匹配返回 {@link Optional#empty()}
     */
    public static Optional<StageStatusEnum> fromValue(String value) {
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
