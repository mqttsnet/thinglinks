package com.mqttsnet.thinglinks.productversion.publish.strategy;

import java.util.List;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.productversion.canary.CanaryRuleMatcher;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import com.mqttsnet.thinglinks.productversion.vo.canary.CanaryConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 灰度发布策略:按 {@link CanaryConfigDTO#getMode()} 二级路由 —— MODE_WHITELIST 取 deviceIdentifications 数组
 * IN 批量改绑;MODE_PERCENT 拉全量设备经 {@link CanaryRuleMatcher#pickByPercent} 一致性哈希取子集再 IN 批量改绑
 * (同设备 + 同百分比结果稳定,30%→50% 渐进放量时已命中设备不会被踢出)。
 *
 * <p>边界:canaryConfigJson 缺失 / mode 非法 / percent &gt;= 100 均抛 {@link BizException};whitelist 为空 /
 * percent 命中数为 0 返 0 + log.warn 不抛(允许"配错就空跑")。</p>
 *
 * <p>跨域必须走 {@link DeviceService} / {@link DeviceQueryService}(带 @DS(BASE_TENANT))触发切租户库,
 * 不能直接调 DeviceManager,否则 fallback 到 primary "0" 默认库 → 跨租户数据串味。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CanaryDeviceRebindStrategy implements DeviceRebindStrategy {

    private final DeviceService deviceService;
    private final DeviceQueryService deviceQueryService;
    private final CanaryRuleMatcher canaryRuleMatcher;

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

        String mode = config.getMode();
        return switch (mode == null ? "" : mode) {
            case CanaryConfigDTO.MODE_PERCENT -> rebindByPercent(productIdentification, newVersion, config);
            case CanaryConfigDTO.MODE_WHITELIST -> rebindByWhitelist(productIdentification, newVersion, config);
            default -> throw BizException.wrap(
                "Unsupported canary mode: '" + mode + "' (expected '"
                    + CanaryConfigDTO.MODE_WHITELIST + "' or '" + CanaryConfigDTO.MODE_PERCENT
                    + "'), config=" + canaryConfigJson);
        };
    }

    /**
     * 按显式白名单批量改绑(前端"按分组" / "按设备识别码"两种来源最终都拍平成这种格式)。
     *
     * @param productIdentification 产品标识
     * @param newVersion            目标版本号
     * @param config                灰度配置
     * @return 改绑设备数
     */
    private int rebindByWhitelist(String productIdentification, String newVersion, CanaryConfigDTO config) {
        List<String> whitelist = config.safeDeviceIdentifications();
        if (whitelist.isEmpty()) {
            log.warn("[publish-strategy:CANARY-whitelist] empty whitelist, no device rebound. product={}",
                productIdentification);
            return 0;
        }
        int affected = deviceService.bulkRebindByDeviceIdentifications(whitelist, newVersion);
        log.info("[publish-strategy:CANARY-whitelist] product={} newVersion={} whitelistSize={} devicesRebound={}",
            productIdentification, newVersion, whitelist.size(), affected);
        return affected;
    }

    /**
     * 按百分比一致性哈希挑选设备子集 → 批量改绑。区间 1~99;&gt;= 100(语义=全量)显式抛出,避免静默退化为
     * 空集合让用户误以为发布成功。用 {@link DeviceQueryService#listDeviceIdentificationsByProduct} 只 select 单列,
     * 单产品设备数 &lt; 5 万时无压力。
     * TODO 大产品(&gt; 5 万设备)把一致性哈希筛选下沉到 SQL(如 WHERE MOD(CRC32(device_identification), 100) &lt; ?),
     *   只把命中的 N% 返给 JVM,避免全量 select + 内存排序 + Job 反复全量拉。
     *
     * @param productIdentification 产品标识
     * @param newVersion            目标版本号
     * @param config                灰度配置
     * @return 改绑设备数
     */
    private int rebindByPercent(String productIdentification, String newVersion, CanaryConfigDTO config) {
        Integer percentRaw = config.getCanaryPercent();
        if (percentRaw == null || percentRaw <= 0) {
            log.warn("[publish-strategy:CANARY-percent] invalid percent={}, no device rebound. product={}",
                percentRaw, productIdentification);
            return 0;
        }
        if (percentRaw >= 100) {
            // 100% 语义上等同 FULL,前端本应拦截;后端兜底显式报错,避免"配错就静默不动设备"
            throw BizException.wrap(
                "CANARY percent=" + percentRaw
                    + " is invalid (>=100 means full rollout, please use FULL strategy instead). product="
                    + productIdentification);
        }
        int percent = percentRaw;
        List<String> all = deviceQueryService.listDeviceIdentificationsByProduct(productIdentification);
        List<String> hit = canaryRuleMatcher.pickByPercent(all, percent);
        if (hit.isEmpty()) {
            log.warn("[publish-strategy:CANARY-percent] percent={} but no device hit (productTotal={}). product={}",
                percent, all.size(), productIdentification);
            return 0;
        }
        int affected = deviceService.bulkRebindByDeviceIdentifications(hit, newVersion);
        log.info("[publish-strategy:CANARY-percent] product={} newVersion={} percent={} hit={}/{} devicesRebound={}",
            productIdentification, newVersion, percent, hit.size(), all.size(), affected);
        return affected;
    }
}
