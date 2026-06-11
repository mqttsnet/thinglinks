package com.mqttsnet.thinglinks.mqs.bus.support;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Bus stage 通用辅助工具,集中"reused-across-stages"操作,避免散落 if/else。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@UtilityClass
@Slf4j
public class BusStageSupport {

    /**
     * 从 ctx 取 enricher 预填的 {@link DeviceCacheVO}。
     */
    public Optional<DeviceCacheVO> deviceCache(StageContext context) {
        return Optional.ofNullable(context)
            .flatMap(c -> c.get(BusConstants.Ctx.DEVICE_CACHE, DeviceCacheVO.class));
    }

    /**
     * 事件类型是否在指定枚举集合内(stage supports() 用)。
     */
    public boolean actionTypeIn(DeviceProtocolEvent event, DeviceActionTypeEnum... candidates) {
        return event != null && actionTypeIn(event.getEventType(), candidates);
    }

    public boolean actionTypeIn(String actionType, DeviceActionTypeEnum... candidates) {
        if (StrUtil.isBlank(actionType) || candidates == null || candidates.length == 0) {
            return false;
        }
        return Arrays.stream(candidates)
            .map(DeviceActionTypeEnum::getValue)
            .anyMatch(actionType::equals);
    }

    /**
     * 单类型快速判断。
     */
    public boolean matchesAction(DeviceProtocolEvent event, DeviceActionTypeEnum target) {
        return event != null && target != null && target.getValue().equals(event.getEventType());
    }

    /**
     * 安全提取 tenantId Long,null-safe。
     */
    public Optional<Long> tenantIdLong(DeviceCacheVO cache) {
        return Optional.ofNullable(cache).map(DeviceCacheVO::getTenantId);
    }

    /**
     * 安全提取 tenantId String;桥接 envelope / Redis key 需要 String 形态。
     */
    public Optional<String> tenantIdStr(DeviceCacheVO cache) {
        return tenantIdLong(cache).map(String::valueOf);
    }

    /**
     * 旁路操作 try/catch + warn,主链路不被吞。
     */
    public void safeRun(String taskName, Runnable task) {
        if (task == null) {
            return;
        }
        try {
            task.run();
        } catch (Exception e) {
            log.warn("[bus.support] {} threw (non-blocking) err={}", taskName, e.getMessage(), e);
        }
    }

    /**
     * 旁路操作带返回值,失败返兜底值。
     */
    public <T> T safeReturn(String taskName, Supplier<T> task, T fallback) {
        if (task == null) {
            return fallback;
        }
        try {
            return task.get();
        } catch (Exception e) {
            log.warn("[bus.support] {} threw (non-blocking) err={}, fallback={}",
                taskName, e.getMessage(), fallback, e);
            return fallback;
        }
    }
}
