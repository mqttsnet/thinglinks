package com.mqttsnet.thinglinks.entity.protocol.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Description:
 * 消息分发完成事件
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "DistedEvent", description = "消息分发完成事件")
public class DistedEvent extends BaseEvent {
    @Schema(description = "消息主题", example = "/lastwill/device01")
    private String topic;

    @Schema(description = "消息报文ID")
    private Long messageId;

    @Schema(description = "服务质量级别（QoS）", allowableValues = {"0", "1", "2"})
    private Integer qos;

    @Schema(description = "消息过期间隔（秒）")
    private Long expiryInterval;

    @Schema(description = "原始负载数据（HEX字符串）")
    private String payload;

    @Schema(description = "原始负载十六进制(二进制无损)")
    private String payloadHex;

    @Schema(description = "负载编码")
    private String encoding;

    @Schema(description = "原始字节数")
    private Integer originalSize;
}
