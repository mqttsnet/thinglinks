package com.mqttsnet.thinglinks.video.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Description:
 * 通知请求 DTO，由各事件监听器构建后传给分发器。
 * <p>
 * 不含业务语义，分发器只关心 eventType + variables + 接收人。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件类型: ALARM / DEVICE_ONLINE / DEVICE_OFFLINE / STREAM_CLOSE
     */
    private String eventType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 优先级/级别 (告警事件用, 可选)
     */
    private String priority;

    /**
     * 模板变量 Map (key=变量名, value=变量值)
     */
    private Map<String, String> variables;

    /**
     * 源数据创建人 (站内信兜底接收人)
     */
    private Long sourceCreatedBy;

    /**
     * 源数据创建人所属组织
     */
    private Long sourceCreatedOrgId;

    /**
     * 业务类型标识: VIDEO_ALARM / VIDEO_DEVICE
     */
    private String bizType;

    /**
     * 业务ID: 告警ID / 设备编号
     */
    private String bizId;

    /**
     * 租户上下文 (@Async 传递用)
     * <p>
     * 调用方: Map localMap = ContextUtil.getLocalMap();
     * 被调方: ContextUtil.setLocalMap(contextLocalMap);
     */
    private Map<String, String> contextLocalMap;
}
