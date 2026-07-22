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
 * 客户端被系统踢下线事件
 * ============================================================================
 *
 * @date 2025/3/9 14:59
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "KickedEvent", description = "客户端被系统踢下线事件")
public class KickedEvent extends BaseEvent {
    @Schema(description = "客户端地址（IP:PORT）")
    private String address;

    @Schema(description = "触发踢下线的原因",
        allowableValues = {"DUPLICATE_CONN", "ADMIN_OPERATION"})
    private String kickReason;
}

