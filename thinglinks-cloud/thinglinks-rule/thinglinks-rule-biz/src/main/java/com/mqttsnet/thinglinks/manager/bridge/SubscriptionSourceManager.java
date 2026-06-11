package com.mqttsnet.thinglinks.manager.bridge;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.vo.query.bridge.SubscriptionSourcePageQuery;

import java.util.List;

/**
 * <p>
 * 通用业务接口
 * 数据桥接-订阅源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface SubscriptionSourceManager extends SuperManager<SubscriptionSource> {

    List<SubscriptionSource> getSubscriptionSourceList(SubscriptionSourcePageQuery query);

    SubscriptionSource getByCode(String sourceCode);

    /**
     * 取启用中的订阅源（启动时拉到内存）
     */
    List<SubscriptionSource> getEnabledSources();
}
