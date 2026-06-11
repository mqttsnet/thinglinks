package com.mqttsnet.thinglinks.video.dto.device.event;

import lombok.Getter;

/**
 * 设备心跳事件。
 * <p>
 * 任何能证明设备还活着的信号都应发此事件，由统一的监听器刷新
 * {@code last_keepalive_time} 与 {@code online_status}：
 * <ul>
 *   <li>收到 SIP REGISTER（含存量设备重复 REGISTER）</li>
 *   <li>收到 SIP MESSAGE CmdType=Keepalive</li>
 *   <li>（将来可扩展：收到 Catalog / DeviceInfo 响应等）</li>
 * </ul>
 * <p>
 * 监听器语义："如果设备当前离线 → 转在线；始终刷新 keepalive 时间戳"。
 *
 * @author mqttsnet
 */
@Getter
public class DeviceKeepaliveEvent extends DeviceInfoBaseEventAbstract<String> {

    /** 心跳时间（当前实现传入时的 yyyy-MM-dd HH:mm:ss 字符串） */
    private final String keepaliveTime;

    /** 心跳来源标识，便于排查来源（"REGISTER" / "KEEPALIVE" 等） */
    private final String origin;

    public DeviceKeepaliveEvent(String deviceIdentification, String keepaliveTime, String origin) {
        super(deviceIdentification);
        this.keepaliveTime = keepaliveTime;
        this.origin = origin;
    }

    /**
     * 语义糖：事件源即设备标识。
     */
    public String getDeviceIdentification() {
        return getSource();
    }
}
