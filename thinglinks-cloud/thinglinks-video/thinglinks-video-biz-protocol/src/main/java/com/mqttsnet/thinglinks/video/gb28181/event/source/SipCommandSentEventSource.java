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
 * SIP 命令发送成功事件源。
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
public class SipCommandSentEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 命令类型（PLAY/PLAYBACK/DOWNLOAD/PTZ/CATALOG 等）
     */
    private String commandType;

    /**
     * 目标设备编号
     */
    private String deviceIdentification;

    /**
     * 目标通道编号
     */
    private String channelIdentification;

    /**
     * SIP Call-ID
     */
    private String callId;

    /**
     * 底层 SIP 方法（INVITE/BYE/MESSAGE/SUBSCRIBE/INFO）
     */
    private String sipMethod;
}
