package com.mqttsnet.thinglinks.productversion.publish.strategy;

import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 影子发布策略:<b>不改绑任何设备</b>,产品当前激活版本指针也不切(在 publish service 层决定)。
 *
 * <p>设计语义(预建表 + 外部切流):影子发布只把新版本物模型在 TD 建好 super table;设备真实流量仍按各自
 * boundProductVersionNo 解析、写其绑定版本的表。<b>影子超表预建后保持空表是预期状态,不是缺口</b> ── 新版本的
 * "启用"由外部按需驱动:把目标设备的 bound_product_version_no 改到该影子版本,改绑后上报热路径
 * {@code DeviceDataProcessingServiceImpl} 按新版本路由,该设备数据自然落到预建好的影子表。切换入口见
 * {@link com.mqttsnet.thinglinks.device.service.DeviceService#switchBoundProductVersion}(对外 REST:PUT /device/switchBoundProductVersion)。</p>
 *
 * <p>故本策略 {@link #rebind} 永远返 0、发布时不自动改绑任何设备,是设计意图而非未实现 ── 自动改绑会破坏
 * "影子=不影响生产、由外部决定何时切流"的语义;系统也不在上报时旁路双写影子表。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class ShadowDeviceRebindStrategy implements DeviceRebindStrategy {

    @Override
    public ProductPublishStrategyEnum supports() {
        return ProductPublishStrategyEnum.SHADOW;
    }

    @Override
    public int rebind(String productIdentification, String newVersion, String canaryConfigJson) {
        log.info("[publish-strategy:SHADOW] product={} newVersion={} ── intentionally NOT rebinding any device", productIdentification, newVersion);
        return 0;
    }
}
