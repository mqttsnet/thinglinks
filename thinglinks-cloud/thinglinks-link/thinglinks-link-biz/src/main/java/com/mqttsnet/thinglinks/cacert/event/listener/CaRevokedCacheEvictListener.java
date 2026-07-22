package com.mqttsnet.thinglinks.cacert.event.listener;

import java.util.List;

import com.mqttsnet.thinglinks.cacert.event.CaRevokedEvent;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * CA 吊销后失效所有关联设备 cache ── 让下一次设备认证走 DB,命中新的 REVOKED 状态拒绝。
 *
 * <p>跨域调 device 域时走 {@link DeviceQueryService}(它带 @DS(BASE_TENANT) 切租户库),
 * 禁止直接调 DeviceManager ── 否则 fallback 到 primary "0" 默认库,跨租户数据串味。</p>
 *
 * @author mqttsnet
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CaRevokedCacheEvictListener {

    private final DeviceQueryService deviceQueryService;
    private final LinkCacheDataHelper linkCacheDataHelper;

    @Async
    @EventListener
    public void onCaRevoked(CaRevokedEvent event) {
        List<Device> devices = deviceQueryService.listByCertSerialNumber(event.getCaSerialNumber());
        if (devices.isEmpty()) {
            log.info("[CaRevoked] no device bound to CA serial={},skip cache evict", event.getCaSerialNumber());
            return;
        }
        devices.forEach(d -> {
            try {
                linkCacheDataHelper.deleteDeviceCacheVO(d.getDeviceIdentification());
            } catch (Exception e) {
                log.warn("[CaRevoked] evict device cache failed deviceId={}",
                        d.getDeviceIdentification(), e);
            }
        });
        log.info("[CaRevoked] evicted {} device cache for CA serial={}",
                devices.size(), event.getCaSerialNumber());
    }
}
