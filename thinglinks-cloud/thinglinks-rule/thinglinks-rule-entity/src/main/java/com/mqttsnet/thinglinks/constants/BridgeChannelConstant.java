package com.mqttsnet.thinglinks.constants;

/**
 * 桥接域 Redis Pub/Sub 频道常量。
 *
 * <p>放 rule-entity 模块统一管理 ── 发布方(rule-biz Service)、订阅方(rule-biz Listener)、
 * 任意调用方都能 import,避免常量散落或字符串硬编码。
 *
 * @author mqttsnet
 * @since 2026-05-07
 */
public final class BridgeChannelConstant {

    /**
     * 桥接规则变更 ── 含数据源(DS_*)、规则本身的 CRUD/启停
     */
    public static final String RULE_CHANGED = "bridge:rule:changed";

    /**
     * 订阅源变更 ── CRUD/启停,触发各节点 LifecycleManager 启停本地连接
     */
    public static final String SUBSCRIPTION_CHANGED = "bridge:subscription:changed";

    private BridgeChannelConstant() {
    }
}
