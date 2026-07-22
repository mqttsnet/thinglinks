package com.mqttsnet.thinglinks.video.service.media;

/**
 * 流媒体节点心跳检测 Service。
 *
 * <p>包一层是为了符合架构规范：boot-impl（Facade 实现）只能调 {@code service} 包下的类，
 * 不允许直接注入 {@code *StatusManager}（带 Manager 后缀的类被视为数据访问层）。
 * 内部 delegate 给具体协议的 StatusManager，承担 {@code @DS} 多租户切库职责。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
public interface VideoMediaHeartbeatService {

    /**
     * 触发指定租户下所有 ZLMediaKit 节点的心跳检测。
     */
    void zlmHeartbeat(Long tenantId);

    /**
     * 触发指定租户下所有 ABLMediaServer 节点的心跳检测。
     */
    void ablHeartbeat(Long tenantId);

    /**
     * 一次性刷新所有流媒体节点（ZLM + ABL）。
     */
    void refreshAll(Long tenantId);
}
