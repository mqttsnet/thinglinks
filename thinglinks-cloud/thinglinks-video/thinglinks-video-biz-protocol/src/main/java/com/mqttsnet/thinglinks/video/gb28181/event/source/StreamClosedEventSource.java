package com.mqttsnet.thinglinks.video.gb28181.event.source;

import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 流关闭事件源。
 * 当流被关闭时发布，携带关闭原因。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamClosedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceIdentification;
    private String channelIdentification;
    private String mediaIdentification;
    private String app;
    private String stream;
    private String callId;
    private String closeReason;
    /**
     * 是否用户主动触发的关闭。
     * <p>true 表示用户在 UI 点了停止/关闭播放、回放、下载等显式操作；
     * false（默认）表示设备 BYE / ZLM 断流 / 异常关闭等非用户意图。
     * 用于 {@code StreamAutoRecoveryListener} 准确判定是否需要自动重试，
     * 避免对 closeReason 字符串做语义猜测（中英文混合时极易漏判）。
     */
    private boolean userInitiated;
    /** 租户ID（通用字段，监听方可直接恢复上下文） */
    private Long tenantId;
}
