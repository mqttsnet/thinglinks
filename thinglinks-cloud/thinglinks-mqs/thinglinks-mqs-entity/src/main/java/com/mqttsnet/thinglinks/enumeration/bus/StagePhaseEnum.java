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
 * 阶段相位 枚举 ── 控制 stage 的执行模式与失败语义。
 * </p>
 *
 * <h3>三相对照表</h3>
 * <table border="1">
 *   <tr><th>Phase</th><th>执行</th><th>失败</th><th>典型 Stage</th></tr>
 *   <tr><td>PRE</td><td>同步串行</td><td>失败终止 pipeline</td><td>enrich(查 Cache) / auth(ACL 校验)</td></tr>
 *   <tr><td>CORE</td><td>同步串行</td><td>各 stage 内 @Retryable;失败影响主链路</td><td>persist / TSL 解析 / TDS 入库</td></tr>
 *   <tr><td>POST</td><td>异步并发,各 stage 独立线程池</td><td>失败仅 warn,不影响其他 stage</td><td>relay / alarm / audit / metric</td></tr>
 * </table>
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "StagePhaseEnum", description = "协议总线-阶段相位 枚举")
public enum StagePhaseEnum {

    /**
     * 前置阶段:同步串行,失败终止整个 pipeline
     */
    PRE("PRE", "前置"),

    /**
     * 核心阶段:同步串行,主链路必跑,各 stage 内可单独 @Retryable
     */
    CORE("CORE", "核心"),

    /**
     * 后置阶段:异步并发,旁路投递,失败隔离不影响其他
     */
    POST("POST", "后置");

    private String value;
    private String desc;

    /**
     * 按 value 字符串反查枚举。
     *
     * @param value 阶段相位 value(PRE / CORE / POST)
     * @return 对应枚举的 Optional;空字符串或未匹配返回 {@link Optional#empty()}
     */
    public static Optional<StagePhaseEnum> fromValue(String value) {
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
