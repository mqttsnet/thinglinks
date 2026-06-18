package com.mqttsnet.thinglinks.productversion.publish.strategy;

import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 全量发布策略:产品下所有设备的 bound_product_version_no 改到新版本(不论此前绑哪个版本统一覆盖)。
 * 改绑走 {@link DeviceRebindStreamer} 游标流式分批(恒定内存 / 小事务 / 有界 IN),支撑大设备量产品;
 * 幂等(SET 到同一目标值,retry 兜底反复跑安全)。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FullDeviceRebindStrategy implements DeviceRebindStrategy {

    private final DeviceRebindStreamer deviceRebindStreamer;

    @Override
    public ProductPublishStrategyEnum supports() {
        return ProductPublishStrategyEnum.FULL;
    }

    @Override
    public int rebind(String productIdentification, String newVersion, String canaryConfigJson) {
        int affected = deviceRebindStreamer.streamFull(productIdentification, newVersion);
        log.info("[publish-strategy:FULL] product={} newVersion={} devicesRebound={}",
            productIdentification, newVersion, affected);
        return affected;
    }
}
