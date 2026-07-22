package com.mqttsnet.thinglinks.mqs.event.report;

import org.springframework.core.Ordered;

/**
 * 数据上报后置处理器(SPI)── 设备上行 PUBLISH 经物模型匹配落库后触发的扩展点。
 *
 * <p>默认实现 {@link DataReportBridgePostProcessor} 把物模型结构化数据桥接出站;
 * 二开可再注册 {@code @Component implements DeviceDataReportPostProcessor} 实现自定义输出
 * (推送自有系统 / 实时分析等),无需改动上报落库主链路。
 *
 * <p>触发处统一 try/catch 包裹,任一处理器异常只 warn 不阻断主链路。
 *
 * @author mqttsnet
 * @since 2026-06-03
 */
public interface DeviceDataReportPostProcessor extends Ordered {

    /**
     * 是否处理本次上报(默认全部处理)。
     *
     * @param ctx 上报上下文
     * @return {@code true} 处理
     */
    default boolean supports(DeviceDataReportContext ctx) {
        return true;
    }

    /**
     * 物模型匹配落库后回调。
     *
     * @param ctx 上报上下文(含物模型结构化数据)
     */
    void onReported(DeviceDataReportContext ctx);

    @Override
    default int getOrder() {
        return 100;
    }
}
