package com.mqttsnet.thinglinks.bridge.event.source;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 数据源变更/删除事件源(共用 payload)── {@code DataSourceChangedEvent}/{@code DataSourceDeletedEvent} 承载。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceChangedEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据源主键 ID
     */
    private Long dataSourceId;

    /**
     * 租户 ID(审计 / 跨节点 Pub/Sub 时用)
     */
    private String tenantId;
}
