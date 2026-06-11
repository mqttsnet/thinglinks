package com.mqttsnet.thinglinks.common.constant;

/**
 * 公共常量类，定义系统中广泛使用的常量字段
 *
 * @author thinglinks
 */
public class CommonConstants {

    /**
     * 事件类型字段名，用于JSON消息中标识事件类型
     * 例如：MQTT事件、WebSocket事件等各类事件的类型标识
     */
    public static final String EVENT_TYPE = "eventType";

    /**
     * 租户ID字段名，用于JSON消息中标识数据所属租户
     * 在多租户系统中用于数据隔离和权限控制
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * 客户端ID字段名，用于标识MQTT客户端的唯一标识符
     * 在连接、断开连接等事件中用于识别特定客户端
     */
    public static final String CLIENT_ID = "clientId";

    /**
     * 版本号
     */
    public static final String VERSION = "version";

    /**
     * 设备ID
     */
    public static final String DEVICE_ID = "deviceId";

}