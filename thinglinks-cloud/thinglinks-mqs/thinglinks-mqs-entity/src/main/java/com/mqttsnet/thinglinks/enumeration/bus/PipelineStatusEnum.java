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
 * Pipeline 整体执行状态 枚举 ── 用于 outcome 摘要 + 日志 + 指标 label。
 * </p>
 *
 * <h3>状态流转</h3>
 * <ul>
 *   <li>SUCCESS:全流程跑完且 PRE/CORE 全过,POST 已提交(POST 自身成败不影响)</li>
 *   <li>PARTIAL:PRE/CORE 至少一个 stage 跳过(supports=false 不计)</li>
 *   <li>FAILED:PRE/CORE 任一 stage 抛异常导致 phase 终止</li>
 *   <li>DROPPED:interceptor 主动丢弃,日志 INFO 不告警</li>
 *   <li>NO_ROUTE:topic 未注册 adapter,事件被忽略</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "PipelineStatusEnum", description = "协议总线-Pipeline 整体执行状态 枚举")
public enum PipelineStatusEnum {

    /**
     * 全部成功 ── PRE/CORE 全过 + 已提交 POST(POST 是否成功不影响此状态)
     */
    SUCCESS("00", "成功"),

    /**
     * 失败 ── PRE/CORE 任一 stage 抛异常导致 phase 终止
     */
    FAILED("01", "失败"),

    /**
     * 部分成功 ── PRE/CORE 至少一个 stage 跳过(supports=false 不计)
     */
    PARTIAL("02", "部分成功"),

    /**
     * 主动丢弃 ── interceptor 抛 DropException,日志 INFO 不告警
     */
    DROPPED("04", "主动丢弃"),

    /**
     * 路由未命中 ── topic 未注册 adapter,事件被忽略
     */
    NO_ROUTE("05", "路由未命中");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举。
     *
     * @param value 状态码(00 / 01 / 02 / 04 / 05)
     * @return 对应枚举的 Optional;空字符串或未匹配返回 {@link Optional#empty()}
     */
    public static Optional<PipelineStatusEnum> fromValue(String value) {
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
