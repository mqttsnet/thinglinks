package com.mqttsnet.thinglinks.device.event.source;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 设备改绑版本事件载荷。目标设备二选一:按产品改绑填 productIdentification,按设备列表改绑填 deviceIdentifications。
 *
 * @author mqttsnet
 */
@Data
@Builder
public class DeviceRebindEventSource {

    /** 产品标识 ── 按产品全量 / 回滚改绑时填,失效该产品下所有设备缓存。 */
    private String productIdentification;

    /** 设备标识列表 ── 按设备列表(灰度白名单)改绑时填,失效这些设备缓存。 */
    private List<String> deviceIdentifications;

    /** 改绑目标版本号(boundProductVersionNo)── 当前仅随事件携带,为后续版本下发类监听器预留。 */
    private String toVersion;

    /**
     * 发布时的上下文快照(tenantId 等)── 改绑在发布异步线程触发,监听器据此恢复租户上下文,
     * 避免 {@code @DS(BASE_TENANT)} 切错库 / cacheKey 拼错租户。
     */
    private Map<String, String> contextMap;
}
