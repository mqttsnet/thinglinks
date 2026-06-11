package com.mqttsnet.thinglinks.bridge.match.strategy;

/**
 * 桥接匹配策略执行顺序常量(集中管理,新增策略时统一调整)。
 *
 * <p>编排原则:**便宜的先做,贵的后做** ── 短路率最高 + 计算最简单的优先。
 *
 * @author mqttsnet
 * @since 2026-05-06
 */
public final class BridgeMatchOrder {

    private BridgeMatchOrder() {
    }

    /**
     * 产品标识(列表 contains,O(1) 命中率高)
     */
    public static final int PRODUCT = 100;

    /**
     * 动作类型(列表 contains,通常 1-3 项)
     */
    public static final int ACTION_TYPE = 200;

    /**
     * Topic 过滤(MQTT 通配符 / SpEL 表达式,中等开销)
     */
    public static final int TOPIC = 300;

    /**
     * 设备维度过滤(列表 contains 简单;tagsAny / groupIds 暂未实现)
     */
    public static final int DEVICE = 400;

    /**
     * 内容过滤(SpEL / JSON_PATH 解析,开销最大)
     */
    public static final int PAYLOAD = 500;

    /**
     * 时间窗口(cron 表达式判定;放最后避免无效计算)
     */
    public static final int TIME_WINDOW = 600;
}
