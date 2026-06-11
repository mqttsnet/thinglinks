package com.mqttsnet.thinglinks.video.empowerment.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * SIP协议命令类型枚举 (GB/T 28181)
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/21
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SipCommandTypeEnum", description = "SIP协议命令类型枚举")
public enum SipCommandTypeEnum {
    // ==================== SIP核心方法 ====================
    INVITE("INVITE", "发起会话请求"),
    ACK("ACK", "确认响应"),
    BYE("BYE", "终止会话"),
    CANCEL("CANCEL", "取消未完成请求"),
    REGISTER("REGISTER", "设备注册"),
    MESSAGE("MESSAGE", "即时消息传输"),
    SUBSCRIBE("SUBSCRIBE", "订阅事件通知"),
    NOTIFY("NOTIFY", "事件通知"),
    OPTIONS("OPTIONS", "查询服务器能力"),

    // ==================== GB/T 28181扩展方法 ====================
    CATALOG("CATALOG", "设备目录查询"),
    DEVICE_INFO("DEVICE_INFO", "设备信息查询"),
    DEVICE_STATUS("DEVICE_STATUS", "设备状态查询"),
    ALARM("ALARM", "报警通知"),
    BROADCAST("BROADCAST", "语音广播"),
    RECORD("RECORD", "录像操作"),
    PLAY("PLAY", "实时点播"),
    PLAYBACK("PLAYBACK", "录像回放"),
    DOWNLOAD("DOWNLOAD", "文件下载"),
    PTZ("PTZ", "云台控制"),
    KEEPALIVE("KEEPALIVE", "设备保活"),
    PRESET_QUERY("PRESET_QUERY", "预置位查询"),
    PRESET_CONTROL("PRESET_CONTROL", "预置位控制"),
    CONFIG_DOWNLOAD("CONFIG_DOWNLOAD", "配置下载"),
    MOBILE_POSITION("MOBILE_POSITION", "移动设备位置订阅");

    private String value;
    private String desc;


    /**
     * 根据value获取对应的枚举
     *
     * @param value 产生源类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<SipCommandTypeEnum> fromValue(String value) {
        return Stream.of(SipCommandTypeEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }

}
