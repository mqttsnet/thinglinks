package com.mqttsnet.thinglinks.productversion.event.publisher;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionPublishedEvent;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionPurgeRequestedEvent;
import com.mqttsnet.thinglinks.productversion.event.ProductVersionRolledBackEvent;
import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 产品版本事件发布器。
 *
 * <p>当前使用 Spring {@link ApplicationEventPublisher},事务提交后由
 * @TransactionalEventListener 异步消费,后续替换 RocketMQ 时
 * 切换发布器实现即可,事件 source 保持不变。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductVersionEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布"版本发布"事件。
     *
     * @param source 事件载体
     */
    public void publishPublished(ProductVersionLifecycleEventSource source) {
        log.info("[ProductVersion] published event source={}", JSON.toJSONString(source));
        applicationEventPublisher.publishEvent(new ProductVersionPublishedEvent(source));
    }

    /**
     * 发布"版本回滚"事件。
     *
     * @param source 事件载体
     */
    public void publishRolledBack(ProductVersionLifecycleEventSource source) {
        log.info("[ProductVersion] rolled-back event source={}", JSON.toJSONString(source));
        applicationEventPublisher.publishEvent(new ProductVersionRolledBackEvent(source));
    }

    /**
     * 发布"历史清理"事件。
     *
     * @param source 事件载体
     */
    public void publishPurgeRequested(ProductVersionLifecycleEventSource source) {
        log.info("[ProductVersion] purge-requested event source={}", JSON.toJSONString(source));
        applicationEventPublisher.publishEvent(new ProductVersionPurgeRequestedEvent(source));
    }
}
