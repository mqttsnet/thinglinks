package com.mqttsnet.thinglinks.productversion.publish.strategy;

import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 全量发布策略:产品下所有设备的 bound_product_version_no 一刀切到新版本(不论此前绑哪个版本统一覆盖)。
 * 幂等(SET 到同一目标值,retry 兜底反复跑安全)。跨域必须走 {@link DeviceService}(带 @DS(BASE_TENANT))切
 * 租户库,不能直接调 DeviceManager,否则 fallback 到 primary 默认库 → 跨租户串味。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FullDeviceRebindStrategy implements DeviceRebindStrategy {

    private final DeviceService deviceService;

    @Override
    public ProductPublishStrategyEnum supports() {
        return ProductPublishStrategyEnum.FULL;
    }

    @Override
    public int rebind(String productIdentification, String newVersion, String canaryConfigJson) {
        int affected = deviceService.bulkRebindByProduct(productIdentification, newVersion);
        log.info("[publish-strategy:FULL] product={} newVersion={} devicesRebound={}",
            productIdentification, newVersion, affected);
        return affected;
    }
}
