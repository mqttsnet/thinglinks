package com.mqttsnet.thinglinks.device.event.listener;

import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.event.DeviceDeletedEvent;
import com.mqttsnet.thinglinks.device.event.DeviceRebindEvent;
import com.mqttsnet.thinglinks.device.event.source.DeviceDeletedEventSource;
import com.mqttsnet.thinglinks.device.event.source.DeviceRebindEventSource;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 设备缓存失效监听器 ── 设备删除 / 改绑后,在事务提交后失效设备缓存。
 *
 * <p>设备缓存按 deviceIdentification 与 clientId 两个维度各存一份,失效需双 key 删除;用
 * {@link DeviceQueryService}(只读 leaf,不引入 DeviceService 避免环)查 clientId。AFTER_COMMIT 保证
 * DB 已落库再清缓存,回滚不脏清。改绑由发布异步线程触发、租户上下文可能缺失,故事件 source 自带
 * contextMap 快照,当前线程无租户时从 source 恢复(删 key 拼租户 / @DS 切租户库都依赖该上下文)。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCacheEvictListener {

    private final LinkCacheDataHelper linkCacheDataHelper;
    private final DeviceQueryService deviceQueryService;

    /**
     * 设备删除 → 失效该设备缓存(载荷已含 deviceIdentification + clientId,零查询)。
     *
     * @param event 设备删除事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDeviceDeleted(DeviceDeletedEvent event) {
        DeviceDeletedEventSource src = event.getEventSource();
        if (src == null) {
            return;
        }
        runWithContext(src.getContextMap(), () -> {
            evict(src.getDeviceIdentification());
            evict(src.getClientId());
        }, "delete", src.getDeviceIdentification());
    }

    /**
     * 设备改绑版本 → 失效缓存,否则上报链路仍读旧 boundProductVersionNo。
     *
     * @param event 设备改绑事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDeviceRebind(DeviceRebindEvent event) {
        DeviceRebindEventSource src = event.getEventSource();
        if (src == null) {
            return;
        }
        runWithContext(src.getContextMap(), () -> {
            if (CollUtil.isNotEmpty(src.getDeviceIdentifications())) {
                src.getDeviceIdentifications().forEach(this::evictByDeviceIdentification);
            } else if (StrUtil.isNotBlank(src.getProductIdentification())) {
                List<String> deviceIdentifications =
                        deviceQueryService.listDeviceIdentificationsByProduct(src.getProductIdentification());
                if (CollUtil.isNotEmpty(deviceIdentifications)) {
                    deviceIdentifications.forEach(this::evictByDeviceIdentification);
                }
            }
        }, "rebind", src.getProductIdentification());
    }

    /**
     * 在租户上下文内执行失效动作:当前线程无租户(异步线程)时从事件 contextMap 恢复,
     * 且只清理本方法恢复的上下文,不影响调用线程自身的 ThreadLocal。
     *
     * @param contextMap 事件携带的上下文快照
     * @param action     失效动作
     * @param tag        日志标签
     * @param key        日志中标识的业务 key
     */
    private void runWithContext(Map<String, String> contextMap, Runnable action, String tag, String key) {
        boolean restored = false;
        if (ContextUtil.getTenantId() == null && CollUtil.isNotEmpty(contextMap)) {
            ContextUtil.setLocalMap(contextMap);
            restored = true;
        }
        try {
            action.run();
        } catch (Exception e) {
            log.warn("[device-cache-evict] {} evict failed key={} err={}", tag, key, e.getMessage());
        } finally {
            if (restored) {
                ContextUtil.remove();
            }
        }
    }

    /**
     * 按设备标识查缓存 VO 拿 clientId,双 key 失效;查不到则只删 deviceIdentification 维度。
     *
     * @param deviceIdentification 设备标识
     */
    private void evictByDeviceIdentification(String deviceIdentification) {
        if (StrUtil.isBlank(deviceIdentification)) {
            return;
        }
        DeviceCacheVO vo = deviceQueryService.findDeviceCacheVO(deviceIdentification).orElse(null);
        if (vo != null) {
            evict(vo.getDeviceIdentification());
            evict(vo.getClientId());
        } else {
            evict(deviceIdentification);
        }
    }

    private void evict(String deviceIdOrClientId) {
        if (StrUtil.isNotBlank(deviceIdOrClientId)) {
            linkCacheDataHelper.deleteDeviceCacheVO(deviceIdOrClientId);
        }
    }
}
