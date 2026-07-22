package com.mqttsnet.thinglinks.productversion.publish.strategy;

import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;

/**
 * 产品版本发布的设备改绑策略 ── 按 {@link ProductPublishStrategyEnum} 拆分成可插拔实现,新增策略加 @Component
 * 实现本接口即被 Spring 自动收集。
 *
 * @author mqttsnet
 */
public interface DeviceRebindStrategy {

    /**
     * 本策略支持的发布类型,与 {@link ProductPublishStrategyEnum} 一一对应;Orchestrator 据此在启动时把
     * List 收敛为 Map 做 O(1) 路由。
     */
    ProductPublishStrategyEnum supports();

    /**
     * 按策略改绑产品下设备的 bound_product_version_no。要求:幂等(retry job 兜底反复调结果一致,不能多绑 /
     * 错绑);失败可恢复(命中为空 / 配置非法等业务可接受失败返 0 + log.warn,仅 DB 异常等系统级错误才抛出由
     * 调用方写 failedReason)。productIdentification / newVersion 不能为空;canaryConfigJson 仅 CANARY 用。
     */
    int rebind(String productIdentification, String newVersion, String canaryConfigJson);
}
