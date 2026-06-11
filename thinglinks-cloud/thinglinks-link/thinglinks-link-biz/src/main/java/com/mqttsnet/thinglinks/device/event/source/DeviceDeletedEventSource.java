package com.mqttsnet.thinglinks.device.event.source;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Description:
 * 设备删除事件源。
 * <p>携带被删设备的关键标识，用于下游消费者（设备分组关系、规则引擎、告警、审计等）按需清理引用。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-27
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDeletedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备主键ID
     */
    private Long deviceId;

    /**
     * 设备标识（业务唯一键，关系表清理依赖此字段）
     */
    private String deviceIdentification;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 上下文快照(tenantId 等)── 供异步缓存失效监听器恢复租户上下文,
     * 避免 @DS(BASE_TENANT) 切错库 / cacheKey 拼错租户。
     */
    private Map<String, String> contextMap;
}
