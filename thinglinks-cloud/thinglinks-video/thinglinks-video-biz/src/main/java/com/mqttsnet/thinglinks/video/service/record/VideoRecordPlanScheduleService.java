package com.mqttsnet.thinglinks.video.service.record;

/**
 * 录像计划调度服务。
 * <p>
 * 由 XXL-Job 定时触发，负责：
 * <ol>
 *   <li>查询所有启用状态的录像计划</li>
 *   <li>根据 scheduleRule 判断当前是否在录制窗口内</li>
 *   <li>调用 ZLM startRecord / stopRecord 控制云端录像</li>
 * </ol>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-06
 */
public interface VideoRecordPlanScheduleService {

    /**
     * 执行录像计划调度。
     * <p>
     * 遍历当前租户下所有启用的录像计划，判断是否应开始/停止录像，
     * 并调用流媒体服务器 API 执行对应操作。
     *
     * @param tenantId 租户ID
     */
    void executeSchedule(Long tenantId);
}
