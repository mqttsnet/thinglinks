package com.mqttsnet.thinglinks.productversion.event.source;

import java.io.Serial;
import java.io.Serializable;

import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品物模型版本生命周期事件载体。
 *
 * <p>用作 {@link com.mqttsnet.thinglinks.productversion.event.ProductVersionPublishedEvent} /
 * {@link com.mqttsnet.thinglinks.productversion.event.ProductVersionRolledBackEvent} /
 * {@link com.mqttsnet.thinglinks.productversion.event.ProductVersionPurgeRequestedEvent}
 * 三个事件的共用 payload,后续替换为 RocketMQ 时直接复用。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVersionLifecycleEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品标识。
     */
    private String productIdentification;

    /**
     * 目标版本号(发布:新版本;回滚:回滚到的版本;清理:被清理版本)。
     */
    private String targetVersion;

    /**
     * 源版本号(发布:上一版,可为 null;回滚:回滚前的版本;清理:同 targetVersion)。
     */
    private String sourceVersion;

    /**
     * 发布策略(全量 / 灰度 / 影子);回滚 / 清理 可为 null。
     */
    private ProductPublishStrategyEnum publishStrategy;

    /**
     * 关联的发布记录 ID,异步执行结果回写到这条记录。
     */
    private Long publishRecordId;
}
