package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigHolder;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.gb28181.cmd.QueryCommander;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 设备上线后异步自动查询服务。
 * <p>
 * 使用 ContextAwareExecutor + videoDefaultExecutor 异步执行，自动传递租户上下文。
 * 额外恢复 TenantSipConfigHolder（SIP 发送需要租户 SIP 配置）。
 *
 * @author mqttsnet
 * @since 2026-04-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class DeviceAutoQueryService {

    private final QueryCommander queryCommander;
    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;

    /**
     * 异步查询设备信息和通道目录。
     *
     * @param device       设备信息
     * @param tenantConfig 租户 SIP 配置（从调用方 ThreadLocal 传入，ContextAwareExecutor 不传递自定义 ThreadLocal）
     */
    public void autoQueryDevice(VideoDeviceResultVO device, TenantSipConfig tenantConfig) {
        if (device == null || device.getDeviceIdentification() == null) {
            return;
        }

        contextAwareExecutor.executeWithContext(() -> {
            // ContextAwareExecutor 自动恢复 ContextUtil，但 TenantSipConfigHolder 是自定义 ThreadLocal 需手动恢复
            if (tenantConfig != null) {
                TenantSipConfigHolder.set(tenantConfig);
            }
            try {
                doQuery(device);
            } finally {
                TenantSipConfigHolder.remove();
            }
            return null;
        }, videoDefaultExecutor);
    }

    private static final int MAX_QUERY_RETRY = 3;
    private static final long[] QUERY_RETRY_DELAYS_MS = {2000, 3000, 5000};

    private void doQuery(VideoDeviceResultVO device) {
        String deviceId = device.getDeviceIdentification();
        String host = device.getHost();
        int port = device.getPort() != null ? device.getPort() : 5060;
        String transport = device.getTransport() != null ? device.getTransport() : "UDP";

        try {
            // 延迟 2 秒，给设备和 SIP 栈留出准备时间
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        sendWithRetry(deviceId, host, port, transport, "DeviceInfo",
                () -> queryCommander.deviceInfoQuery(deviceId, host, port, transport, GbProtocolVersionEnum.GB2016));

        sendWithRetry(deviceId, host, port, transport, "Catalog",
                () -> queryCommander.catalogQuery(deviceId, host, port, transport, GbProtocolVersionEnum.GB2016));
    }

    private void sendWithRetry(String deviceId, String host, int port, String transport,
                               String queryType, Runnable queryAction) {
        for (int i = 0; i < MAX_QUERY_RETRY; i++) {
            try {
                log.info("[自动查询] 设备: {}, 发送 {} 查询{}", deviceId, queryType,
                        i > 0 ? " (第" + (i + 1) + "次重试)" : "");
                queryAction.run();
                return; // 成功则退出
            } catch (Exception e) {
                log.warn("[自动查询] 设备: {}, {} 查询失败(第{}次): {}", deviceId, queryType, i + 1, e.getMessage());
                if (i < MAX_QUERY_RETRY - 1) {
                    try {
                        Thread.sleep(QUERY_RETRY_DELAYS_MS[i]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        log.error("[自动查询] 设备: {}, {} 查询全部重试失败，放弃", deviceId, queryType);
    }
}
