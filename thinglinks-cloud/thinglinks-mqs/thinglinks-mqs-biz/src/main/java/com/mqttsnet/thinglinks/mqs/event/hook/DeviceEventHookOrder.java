package com.mqttsnet.thinglinks.mqs.event.hook;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备事件 hook 顺序集中表 ── 全部 hook 顺序在此声明,避免散落硬编码.
 * 数字越小越优先;同段位间隔 10,留出插入空间.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Getter
@AllArgsConstructor
public enum DeviceEventHookOrder {

    /**
     * 入口前置:trace 注入 / 入口指标计数
     */
    METRICS_ENTRY(50),
    /**
     * 状态同步:以 broker session 真值更新 device.connect_status
     */
    CONNECT_STATUS_SYNC(100),
    /**
     * 业务联动:告警预触发 / 桥接预处理(Processor 主路径前的横切关注点)
     */
    BUSINESS_LINKAGE(200),
    /**
     * 收尾审计:落审计表 / 出口指标
     */
    AUDIT(300);

    private final int order;
}
