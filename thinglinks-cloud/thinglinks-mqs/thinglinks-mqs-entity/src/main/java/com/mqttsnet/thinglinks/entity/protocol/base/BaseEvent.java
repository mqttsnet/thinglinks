package com.mqttsnet.thinglinks.entity.protocol.base;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.basic.utils.HybridLogicalClockUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Description:
 * <p>
 * 事件基类
 * ============================================================================
 *
 * @author mqttsnet
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@Schema(title = "BaseEvent", description = "事件基类")
public abstract class BaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantId;

    @Schema(description = "客户端ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientId;

    @Schema(description = "操作结果状态", allowableValues = {"success", "fail"})
    private String success;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String event;


    @Schema(description = "消息时间戳（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    /**
     * 上游因果时钟 HLC（单调递增,LWW CAS 单调写键）。
     * <p>接入层(broker)产出,语义对齐 BifroMQ event 插件;下游 {@code DeviceConnectStatusSyncHook} /
     * {@code DevicePingProcessor} 据此防乱序回退。<b>数值远超 epoch ms,禁止当时间戳用。</b>
     * <p>{@code @Builder.Default}:broker build 事件时自动盖戳;MQTT 反序列化走 BifroMQ 的 JSON 值。
     */
    @Schema(description = "上游因果时钟 HLC（单调递增;数值远超 epoch ms,禁止当时间戳用）")
    @Builder.Default
    private Long eventHlc = HybridLogicalClockUtil.nextHlc();

    /**
     * 事件真实发生瞬间（epoch ms,跨节点物理时间锚点）。
     * <p>权威业务时间 ── 业务展示 / 时序入库 / 桥接下游数据源时间字段应一律采用此字段。
     */
    @Schema(description = "事件真实发生瞬间（epoch ms）")
    @Builder.Default
    private Long eventUtc = System.currentTimeMillis();


}
