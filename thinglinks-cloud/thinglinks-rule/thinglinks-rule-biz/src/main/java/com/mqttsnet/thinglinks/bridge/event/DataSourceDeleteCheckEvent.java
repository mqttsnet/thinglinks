package com.mqttsnet.thinglinks.bridge.event;

import com.mqttsnet.thinglinks.bridge.event.source.DataSourceDeleteCheckEventSource;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 数据源待删除校验事件 ── 切断 DataSourceService → DataBridgeService / SubscriptionSourceService 的循环依赖。
 *
 * <p><b>语义</b>:DataSource 被请求删除时发本事件;持有 DataSource 引用的各领域(规则 / 订阅源等)
 * 监听后<b>自查</b>是否有引用,有则<b>抛 BizException 阻止删除</b>。
 *
 * <p><b>同步语义</b>:Spring {@code @EventListener} 默认同步执行,监听器抛异常会传播到
 * {@code publishEvent} 调用方 ── 正好实现"阻止删除"的语义,无需额外协调机制。
 *
 * <p><b>架构收益</b>:DataSource 是底层,不再依赖跨域 Service;各领域单向依赖 DataSource。
 *
 * @author mqttsnet
 * @since 2026-05-07
 */
@Getter
public class DataSourceDeleteCheckEvent extends ApplicationEvent {

    private final DataSourceDeleteCheckEventSource eventSource;

    public DataSourceDeleteCheckEvent(DataSourceDeleteCheckEventSource source) {
        super(source);
        this.eventSource = source;
    }
}
