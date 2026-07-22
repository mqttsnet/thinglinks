package com.mqttsnet.thinglinks.device.event.source;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.thinglinks.device.enumeration.DeviceAclRuleLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ACL 规则变更事件源,携带维度三元组(ruleLevel + productId + deviceId)供下游按维度精确失效 redis 缓存。
 * 只带轻量元数据、不带整个 DeviceAclRule 实体,便于后续引入 MQ 序列化跨进程。
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAclRuleChangedEventSource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 规则 id ── 排查时定位用,失效逻辑不依赖。 */
    private Long ruleId;

    /**
     * 规则级别 ── 决定 evict 走 DEL hash(产品级)还是 HDEL field(设备级)。
     * Jackson 默认按 enum name 序列化;若上 MQ 需跟 DB 的 Integer value 一致,在 enum 加
     * {@code @JsonValue} / {@code @JsonCreator}。
     */
    private DeviceAclRuleLevelEnum ruleLevel;

    /** 产品标识 ── 必填,evict 维度的产品 cacheKey 部分。 */
    private String productIdentification;

    /** 设备标识 ── 设备级规则必填,产品级规则为 null。 */
    private String deviceIdentification;
}
