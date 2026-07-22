package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * SIP 协议命令类型枚举（GB/T 28181-2016 / 2022）。
 * 包含 SIP 核心方法和 GB28181 扩展的业务命令类型，
 * 每个枚举项通过 sipMethod 标识其底层使用的 SIP 方法。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SipCommandTypeEnum", description = "SIP协议命令类型枚举")
public enum SipCommandTypeEnum {

    // ==================== SIP 核心方法 ====================

    INVITE("INVITE", "发起会话请求", "INVITE"),
    ACK("ACK", "确认响应", "ACK"),
    BYE("BYE", "终止会话", "BYE"),
    CANCEL("CANCEL", "取消未完成请求", "CANCEL"),
    REGISTER("REGISTER", "设备注册", "REGISTER"),
    MESSAGE("MESSAGE", "即时消息传输", "MESSAGE"),
    SUBSCRIBE("SUBSCRIBE", "订阅事件通知", "SUBSCRIBE"),
    NOTIFY("NOTIFY", "事件通知", "NOTIFY"),
    OPTIONS("OPTIONS", "查询服务器能力", "OPTIONS"),
    INFO("INFO", "会话内信息传输", "INFO"),

    // ==================== GB/T 28181 扩展方法 ====================

    CATALOG("CATALOG", "设备目录查询", "MESSAGE"),
    DEVICE_INFO("DEVICE_INFO", "设备信息查询", "MESSAGE"),
    DEVICE_STATUS("DEVICE_STATUS", "设备状态查询", "MESSAGE"),
    ALARM("ALARM", "报警通知", "MESSAGE"),
    BROADCAST("BROADCAST", "语音广播", "INVITE"),
    RECORD("RECORD", "录像操作", "MESSAGE"),
    PLAY("PLAY", "实时点播", "INVITE"),
    PLAYBACK("PLAYBACK", "录像回放", "INVITE"),
    DOWNLOAD("DOWNLOAD", "文件下载", "INVITE"),
    PTZ("PTZ", "云台控制", "MESSAGE"),
    KEEPALIVE("KEEPALIVE", "设备保活", "MESSAGE"),
    PRESET_QUERY("PRESET_QUERY", "预置位查询", "MESSAGE"),
    PRESET_CONTROL("PRESET_CONTROL", "预置位控制", "MESSAGE"),
    CONFIG_DOWNLOAD("CONFIG_DOWNLOAD", "配置下载", "MESSAGE"),
    MOBILE_POSITION("MOBILE_POSITION", "移动设备位置订阅", "SUBSCRIBE"),
    ALARM_SUBSCRIBE("ALARM_SUBSCRIBE", "告警订阅", "SUBSCRIBE"),
    CATALOG_SUBSCRIBE("CATALOG_SUBSCRIBE", "目录订阅", "SUBSCRIBE"),

    // ==================== GB/T 28181-2022 新增 ====================

    DRAG_ZOOM("DRAG_ZOOM", "拉框放大/缩小", "MESSAGE"),
    HOME_POSITION("HOME_POSITION", "看守位控制", "MESSAGE"),
    CRUISE("CRUISE", "巡航控制", "MESSAGE"),
    SCAN("SCAN", "自动扫描", "MESSAGE"),
    AUXILIARY_SWITCH("AUXILIARY_SWITCH", "辅助开关控制", "MESSAGE"),
    FORCED_KEY_FRAME("FORCED_KEY_FRAME", "强制关键帧", "INFO"),
    PLAYBACK_CONTROL("PLAYBACK_CONTROL", "回放控制（暂停/恢复/倍速/拖拽）", "INFO"),
    TALK("TALK", "语音对讲", "INVITE"),
    ;

    /**
     * 命令类型标识
     */
    private String value;

    /**
     * 中文描述
     */
    private String desc;

    /**
     * 底层使用的 SIP 方法（INVITE/BYE/MESSAGE/SUBSCRIBE/INFO 等）
     */
    private String sipMethod;

    /**
     * 根据 value 查找枚举
     *
     * @param value 命令类型标识
     * @return 匹配的枚举实例
     */
    public static Optional<SipCommandTypeEnum> fromValue(String value) {
        return Stream.of(SipCommandTypeEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }

    /**
     * 判断该命令是否通过 INVITE 方法实现
     *
     * @return 是否为 INVITE 类命令
     */
    public boolean isInviteMethod() {
        return "INVITE".equals(this.sipMethod);
    }

    /**
     * 判断该命令是否通过 MESSAGE 方法实现
     *
     * @return 是否为 MESSAGE 类命令
     */
    public boolean isMessageMethod() {
        return "MESSAGE".equals(this.sipMethod);
    }

    /**
     * 判断该命令是否通过 SUBSCRIBE 方法实现
     *
     * @return 是否为 SUBSCRIBE 类命令
     */
    public boolean isSubscribeMethod() {
        return "SUBSCRIBE".equals(this.sipMethod);
    }
}
