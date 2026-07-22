package com.mqttsnet.thinglinks.ota.service.support;

import java.util.Collections;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OTA 驱动的物模型版本切换器。
 *
 * <p>把"设备绑定的产品版本序号"跟随 OTA 升级 / 固件软件版本上报自动迁移到升级包预先配置的目标产品版本
 * (影子版本)。影子发布只预建好新版本的 TD 超表占位,并不改动任何设备;真正的逐台迁入由本组件在 OTA
 * 事件触发时完成 ── 这样灰度跟着固件 / 软件版本走,而不是随机哈希。</p>
 *
 * <p>两个触发口径(对应 {@link com.mqttsnet.thinglinks.ota.service.impl.OtaUpgradeTasksServiceImpl}):</p>
 * <ol>
 *     <li>升级成功(hook A):某设备本次升级记录状态 = 升级成功,直接用升级包上的
 *         {@code productVersionNo} 切换该设备绑定版本。</li>
 *     <li>版本上报(hook B):设备上报固件 / 软件版本,反查"该产品 + 该上报版本"对应的升级包,
 *         取其 {@code productVersionNo} 切换 ── 兼容设备升级完成后直接上报新版本、不走人工确认的场景。</li>
 * </ol>
 *
 * <p>所有切换均 <b>幂等 + fail-soft</b>:底层
 * {@link DeviceService#switchBoundProductVersion(String, java.util.List, String)} 已做"目标版本须已发布 /
 * 灰度 / 影子"校验与重复切换幂等;本组件再包一层异常吞掉,确保目标版本非法 / 已被清理等情况只记日志,绝不中断
 * OTA 上报与升级记录落库。升级包未配置目标版本(为空)时直接跳过,不驱动切换。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtaModelVersionSwitcher {

    private final DeviceService deviceService;
    private final OtaUpgradesService otaUpgradesService;

    /**
     * hook A ── 设备升级成功后,把绑定的产品版本序号切到升级包配置的目标产品版本(影子版本)。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param productVersionNo      升级包配置的目标产品版本号(影子版本),为空则跳过
     */
    public void switchOnUpgradeSuccess(String productIdentification, String deviceIdentification, String productVersionNo) {
        doSwitch(productIdentification, deviceIdentification, productVersionNo, "upgrade-success");
    }

    /**
     * hook B ── 设备上报固件 / 软件版本后,反查对应升级包的目标产品版本(影子版本)并切换绑定。
     *
     * @param productIdentification 产品标识
     * @param deviceIdentification  设备标识
     * @param reportedVersion       设备上报的固件 / 软件版本(= 升级包版本号)
     * @param packageType           升级包类型(固件 / 软件,可空)
     */
    public void syncByReportedVersion(String productIdentification, String deviceIdentification,
                                      String reportedVersion, Integer packageType) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(deviceIdentification) || StrUtil.isBlank(reportedVersion)) {
            return;
        }
        String productVersionNo = otaUpgradesService.resolveProductVersionNo(productIdentification, reportedVersion, packageType);
        doSwitch(productIdentification, deviceIdentification, productVersionNo, "version-report");
    }

    /**
     * 幂等 + fail-soft 地把单台设备绑定版本切到目标版本。三要素任一为空则视为"不驱动切换"直接返回。
     *
     * @param trigger 触发来源标记,仅用于日志区分
     */
    private void doSwitch(String productIdentification, String deviceIdentification, String productVersionNo, String trigger) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(deviceIdentification) || StrUtil.isBlank(productVersionNo)) {
            return;
        }
        try {
            int affected = deviceService.switchBoundProductVersion(
                    productIdentification, Collections.singletonList(deviceIdentification), productVersionNo);
            log.info("[ota-model-switch] trigger={} product={} device={} toVersion={} affected={}",
                    trigger, productIdentification, deviceIdentification, productVersionNo, affected);
        } catch (Exception e) {
            // fail-soft:目标版本非法 / 已被清理(drop stable)等不应中断 OTA 主流程,仅告警
            log.warn("[ota-model-switch] trigger={} product={} device={} toVersion={} skipped: {}",
                    trigger, productIdentification, deviceIdentification, productVersionNo, e.getMessage());
        }
    }
}
