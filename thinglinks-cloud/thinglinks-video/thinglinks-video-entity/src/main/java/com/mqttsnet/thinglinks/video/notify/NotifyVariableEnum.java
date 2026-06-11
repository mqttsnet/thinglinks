package com.mqttsnet.thinglinks.video.notify;

import java.util.Arrays;
import java.util.List;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * 通知模板变量注册表（统一枚举维护，前端直接读）。
 * <p>
 * 前端渠道编辑页根据 eventType 过滤可用变量，在模板编辑框旁提供快捷插入。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Getter
@AllArgsConstructor
public enum NotifyVariableEnum {

    // ===== 系统变量 (分发器自动注入) =====
    SYS_DOMAIN("sys.domain", "部署域名", BizConstant.ALL),
    SYS_APP_PATH("sys.appPath", "应用路径", BizConstant.ALL),

    // ===== 通用业务变量 =====
    BIZ_ID("bizId", "业务ID", BizConstant.ALL),
    BIZ_TYPE("bizType", "业务类型", BizConstant.ALL),
    EVENT_TIME("eventTime", "事件时间", BizConstant.ALL),

    // ===== 设备变量 =====
    DEVICE_IDENTIFICATION("deviceIdentification", "设备编号", "ALARM,DEVICE_ONLINE,DEVICE_OFFLINE"),
    DEVICE_NAME("deviceName", "设备名称", "ALARM,DEVICE_ONLINE,DEVICE_OFFLINE"),
    CHANNEL_IDENTIFICATION("channelIdentification", "通道编号", "ALARM"),
    ONLINE_STATUS("onlineStatus", "在线状态", "DEVICE_ONLINE,DEVICE_OFFLINE"),

    // ===== 告警变量 =====
    ALARM_PRIORITY("alarmPriority", "告警级别", "ALARM"),
    ALARM_TYPE("alarmType", "告警类型", "ALARM"),
    ALARM_DESCRIPTION("alarmDescription", "告警描述", "ALARM"),

    // ===== 流事件变量 =====
    STREAM_URL("streamUrl", "流地址", "STREAM_CLOSE"),
    STREAM_TYPE("streamType", "流类型", "STREAM_CLOSE"),
    ;

    /**
     * 变量名 (模板中用 ${key})
     */
    private final String key;

    /**
     * 中文说明 (前端显示)
     */
    private final String label;

    /**
     * 适用事件类型 (all = 全部, 逗号分隔)
     */
    private final String eventTypes;

    /**
     * 按事件类型过滤可用变量列表
     */
    public static List<NotifyVariableEnum> getByEventType(String eventType) {
        return Arrays.stream(values())
            .filter(v -> BizConstant.ALL.equals(v.eventTypes) || v.eventTypes.contains(eventType))
            .toList();
    }
}
