package com.mqttsnet.thinglinks.dto.linkage.execution;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则事件触发上下文 ── 设备上报/生命周期事件驱动规则评估时,携带触发消息本身的数据。
 *
 * <p>事件路径下条件左值优先从本对象取(消息内值,零缓存读);
 * 为 null 时表示定时/API 触发路径,策略回退到最新快照/数据池读取。
 *
 * @author mqttsnet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerEventDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 产品标识 */
    private String productIdentification;

    /** 设备标识 */
    private String deviceIdentification;

    /** 事件时间(epoch ms,取 envelope.eventUtc,缺省回退 ts) */
    private Long eventUtc;

    /** 设备动作类型(envelope.actionType:PUBLISH/CONNECT/DISCONNECT…) */
    private String actionType;

    /** 报文形态(RAW / THING_MODEL) */
    private String payloadKind;

    /** 原始报文 JSON(THING_MODEL 时为 ProductResultVO 序列化结果,防抖首值快照存储用) */
    private String rawMessage;

    /**
     * 解析后的物模型结构化对象(实际类型 {@code ProductResultVO})。
     * <p>用 Object 承载:rule-entity 不依赖 link-entity,消费端解析一次、策略端强转使用,避免逐规则重复反序列化。
     */
    @JsonIgnore
    private transient Object thingModel;
}
