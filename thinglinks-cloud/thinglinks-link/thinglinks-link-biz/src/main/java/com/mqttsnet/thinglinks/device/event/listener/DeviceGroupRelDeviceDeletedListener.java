package com.mqttsnet.thinglinks.device.event.listener;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.device.event.DeviceDeletedEvent;
import com.mqttsnet.thinglinks.device.event.source.DeviceDeletedEventSource;
import com.mqttsnet.thinglinks.device.service.group.DeviceGroupRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 设备删除事件 → 设备分组关系清理监听器。
 * <p>
 * <b>同步执行约束（重要，请勿改异步）：</b>
 * 本监听器使用 {@link EventListener} 同步注解，调用线程与发布者一致，
 * 与发布事件的 {@code DeviceServiceImpl} 处于<b>同一事务</b>。任一清理失败将整体回滚 device 删除，
 * 杜绝"设备已删除但分组仍持有指针"的孤儿数据。
 * </p>
 * <p>
 * <b>DS 兼容性约束：</b>
 * 本监听器只能调用同 {@code @DS(BASE_TENANT)} 的 Service 方法。若未来引入跨 DS 的清理，
 * 必须改用 {@link org.springframework.transaction.event.TransactionalEventListener}
 * 的 {@code AFTER_COMMIT} 阶段，让原事务结束后再执行，避免 dynamic-datasource 切库失败。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceGroupRelDeviceDeletedListener {

    private final DeviceGroupRelService deviceGroupRelService;

    @EventListener
    public void onDeviceDeleted(DeviceDeletedEvent event) {
        DeviceDeletedEventSource source = Optional.ofNullable(event)
                .map(DeviceDeletedEvent::getEventSource)
                .orElse(null);
        if (source == null) {
            log.warn("Skip device_group_rel cleanup, event or eventSource is null");
            return;
        }
        Optional.of(source)
                .map(DeviceDeletedEventSource::getDeviceIdentification)
                .filter(StrUtil::isNotBlank)
                .ifPresentOrElse(
                        identification -> {
                            log.info("Handle DeviceDeletedEvent → clean device_group_rel, deviceId={}, deviceIdentification={}, clientId={}",
                                    source.getDeviceId(), identification, source.getClientId());
                            deviceGroupRelService.removeByDeviceIdentification(identification);
                        },
                        () -> log.warn("Skip device_group_rel cleanup, deviceIdentification missing, deviceId={}, clientId={}",
                                source.getDeviceId(), source.getClientId())
                );
    }
}
