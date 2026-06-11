package com.mqttsnet.thinglinks.productversion.publish.strategy;

import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 影子发布策略:<b>不改绑任何设备</b>,产品当前激活版本指针也不切(在 publish service 层决定)。
 *
 * <p>语义:新版本仅在 TD时序数据库 建好 super table 用于旁路写入,设备真实流量仍按原绑定版本解析。
 * 适合上线前用真实流量"双跑"对照新旧物模型差异,验证完再正式发布 FULL/CANARY。</p>
 *
 * <p>本策略 {@link #rebind} 永远返 0,这是设计意图,不是 bug ── 影子模式的本质就是
 * "不影响生产",任何设备改绑都会破坏这个语义。</p>
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
