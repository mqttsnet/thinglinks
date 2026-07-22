package com.mqttsnet.thinglinks.constant;

/**
 * 公共常量类，定义系统中广泛使用的常量字段
 *
 * @author mqttsnet
 */
public class CommonConstants {

    /**
     * 成功标识
     */
    public static final String SUCCESS = "success";

    /**
     * 失败标识
     */
    public static final String FAILURE = "failure";

    /**
     * 事件类型字段名，用于JSON消息中标识事件类型
     * 例如：MQTT事件、WebSocket事件等各类事件的类型标识
     */
    public static final String EVENT_TYPE = "eventType";

    /**
     * 事件时间字段名 ── broker plugin 调度 {@code enrichEventData} 瞬间的
     * {@link System#currentTimeMillis()}.
     * <p><b>语义</b>:plugin 处理事件的时间(64 worker 取值瞬间);多 worker 池下与
     * broker 内核事件发生时刻可能有几 ms 误差.
     * <b>仅人读 / debug</b> 用,与 {@link #EVENT_TIME_STR} 配套展示;
     * <b>禁止用于因果排序 / 状态机单调写</b>(因果排序请用 {@link #EVENT_HLC}).
     */
    public static final String EVENT_TIME = "eventTime";

    /**
     * 事件时间字符串字段名,与 {@link #EVENT_TIME} 同源 millis 的人类可读格式化版本.
     */
    public static final String EVENT_TIME_STR = "eventTimeStr";

    /**
     * 事件发生时刻的物理 UTC ms ── 来自 {@code Event.utc()}.
     * <p><b>语义</b>:跨节点物理时间锚点,反映事件真实发生瞬间(节点内单调,跨节点受 NTP 影响).
     * 业务展示推荐用此字段而非 {@link #EVENT_TIME}.
     */
    public static final String EVENT_UTC = "eventUtc";

    /**
     * 事件因果时钟 ── 来自 {@code Event.hlc()},64-bit Hybrid Logical Clock.
     * <p><b>语义</b>:跨节点 <b>严格因果保序</b> 的单调时钟 ──
     * 高 48 位 utc,低 16 位 logical counter.
     * <b>下游状态机单调写(connect_status 等)的唯一权威排序键</b>.
     */
    public static final String EVENT_HLC = "eventHlc";

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
     * 用户ID字段名，用于标识用户的唯一标识符
     * 在认证、授权等场景中用于识别特定用户
     */
    public static final String USER_ID = "userId";


    /**
     * 版本号
     */
    public static final String VERSION = "version";


    /**
     * ACL规则字段名，用于标识ACL（访问控制列表）规则
     * 在授权决策中用于判断客户端是否有权限执行特定操作
     */
    public static final String ACL_RULE = "aclRule";


    /**
     * 主题字段名，用于标识MQTT消息的主题
     * 在发布、订阅等操作中用于指定消息的目标主题
     */
    public static final String TOPIC = "topic";
}