package com.mqttsnet.thinglinks.mqs.uplink.handler;

import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;

/**
 * @program: thinglinks-cloud
 * @description:
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 22:45
 **/
public interface TopicHandler {

    /**
     * 处理MQTT消息事件源
     * <p>
     * 该方法用于处理从MQTT代理接收到的消息事件源。
     * 实现类需要根据事件源中的主题（topic）和服务质量等级（qos）来执行适当的业务逻辑。
     * <p>
     * 实现类可以根据事件源中的其他属性（如payloadBytes）来执行更复杂的业务逻辑。
     *
     * @param eventSource the MQTT message event source.
     */
    void handle(UplinkMessageEventSource eventSource);

    /**
     * 本处理器完整匹配的 topic 正则表达式。
     * <p>
     * 工厂在启动时会扫描所有 {@link TopicHandler} Bean，凡返回非空正则者即自动注册到路由表；
     * 因此新增厂商 topic 处理器只需实现本方法并声明为 Spring Bean，无需改动工厂代码。
     * <p>
     * 返回 {@code null} 或空串表示本处理器不参与正则匹配（用作兜底处理器）。
     *
     * @return 完整匹配的 topic 正则;null/空表示不参与正则匹配
     * @author mqttsnet
     * @since 2026-06-03
     */
    default String topicPattern() {
        return null;
    }

    /**
     * 匹配优先级，数值越小越先参与匹配。
     * <p>
     * 当多个正则可能命中同一 topic 时，可借此控制先后顺序;默认正则之间互斥时无需关心。
     *
     * @return 匹配优先级,默认 100
     * @author mqttsnet
     * @since 2026-06-03
     */
    default int order() {
        return 100;
    }
}
