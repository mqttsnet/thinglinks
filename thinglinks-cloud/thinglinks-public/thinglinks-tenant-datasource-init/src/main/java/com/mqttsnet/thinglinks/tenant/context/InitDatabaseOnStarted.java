package com.mqttsnet.thinglinks.tenant.context;

import com.mqttsnet.thinglinks.tenant.service.DatasourceInitDataSourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 启动时初始化租户数据源 ── 在所有单例 bean 就绪后、SmartLifecycle.start 之前触发。
 * <p>早于 MQ Container({@code DefaultRocketMQListenerContainer} / {@code ConcurrentMessageListenerContainer} 等)
 * 启动消费,确保 consumer 切库时租户 pool 已注册,无需 Guard / 阻塞兜底。
 *
 * @author mqttsnet
 */
@Slf4j
@AllArgsConstructor
public class InitDatabaseOnStarted implements SmartInitializingSingleton {

    private final DatasourceInitDataSourceService datasourceInitDataSourceService;

    @Override
    public void afterSingletonsInstantiated() {
        log.info("[InitDatabase] start initializing tenant datasources (before SmartLifecycle.start)");
        datasourceInitDataSourceService.initDataSource();
        log.info("[InitDatabase] tenant datasources initialized");
    }
}
