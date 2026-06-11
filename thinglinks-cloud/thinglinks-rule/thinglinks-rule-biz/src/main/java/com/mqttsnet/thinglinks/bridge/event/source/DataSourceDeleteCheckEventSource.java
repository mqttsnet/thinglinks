package com.mqttsnet.thinglinks.bridge.event.source;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 数据源待删除校验事件源 ── {@code DataSourceDeleteCheckEvent} 实际承载的 payload。
 *
 * <p>语义:DataSource 被请求删除时发本事件;持有 DataSource 引用的各领域(规则 / 订阅源等)
 * 监听后自查是否有引用,有则抛 BizException 阻止删除。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDeleteCheckEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待删除的数据源主键 ID
     */
    private Long dataSourceId;
}
