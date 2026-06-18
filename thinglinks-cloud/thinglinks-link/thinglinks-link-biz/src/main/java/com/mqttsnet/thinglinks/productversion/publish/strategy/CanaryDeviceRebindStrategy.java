package com.mqttsnet.thinglinks.productversion.publish.strategy;

import java.util.List;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import com.mqttsnet.thinglinks.productversion.vo.canary.CanaryConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 灰度发布策略:取 canaryConfigJson 的白名单(前端"按分组" / "按设备识别码"两种来源最终都拍平成
 * deviceIdentifications),经 {@link DeviceService#bulkRebindByIdentificationsIncludingSubDevices} 按网关粒度改绑
 * —— 白名单规模有界,命中网关连带其子设备,保证不变式 <b>子设备版本 = 所属网关版本</b> 不被拆裂。
 * 本策略仅作用于 CANARY,不影响 FULL / SHADOW。
 *
 * <p>边界:canaryConfigJson 缺失抛 {@link BizException};whitelist 为空返 0 + log.warn 不抛(允许"配错就空跑")。</p>
 *
 * <p>跨域必须走 {@link DeviceService}(底层带 @DS(BASE_TENANT))触发切租户库,不能直接调 DeviceManager,
 * 否则 fallback 到 primary "0" 默认库 → 跨租户数据串味。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanaryDeviceRebindStrategy implements DeviceRebindStrategy {

    private final DeviceService deviceService;

    @Override
    public ProductPublishStrategyEnum supports() {
        return ProductPublishStrategyEnum.CANARY;
    }

    @Override
    public int rebind(String productIdentification, String newVersion, String canaryConfigJson) {
        // 配置整段缺失 ── 单独识别,便于运维一眼区分"漏配 / 配错值"两种数据问题
        CanaryConfigDTO config = CanaryConfigDTO.parse(canaryConfigJson)
            .orElseThrow(() -> BizException.wrap(
                "CANARY strategy requires canaryConfigJson, but got null/blank. product=" + productIdentification));

        List<String> whitelist = config.safeDeviceIdentifications();
        if (whitelist.isEmpty()) {
            log.warn("[publish-strategy:CANARY] empty whitelist, no device rebound. product={}", productIdentification);
            return 0;
        }
        // 按网关粒度改绑:白名单含网关时连同其子设备一并改,保证子设备版本跟随网关
        int affected = deviceService.bulkRebindByIdentificationsIncludingSubDevices(
            whitelist, productIdentification, newVersion);
        log.info("[publish-strategy:CANARY] product={} newVersion={} whitelistSize={} devicesRebound={}",
            productIdentification, newVersion, whitelist.size(), affected);
        return affected;
    }
}
