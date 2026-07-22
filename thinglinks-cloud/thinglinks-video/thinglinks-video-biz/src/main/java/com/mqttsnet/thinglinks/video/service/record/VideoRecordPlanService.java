package com.mqttsnet.thinglinks.video.service.record;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;

import java.util.List;

/**
 * 录像计划业务接口。
 *
 * <p>提供录像计划的查询、激活与停用等业务操作，
 * 继承 {@link SuperService} 获得通用 CRUD 能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoRecordPlan
 * @see SuperService
 */
public interface VideoRecordPlanService extends SuperService<Long, VideoRecordPlan> {

    /**
     * 查询所有处于启用状态的录像计划。
     *
     * <p>返回 {@link VideoRecordPlan} 中 status 为启用的记录列表，
     * 可用于定时任务调度或前端展示。</p>
     *
     * @return 启用状态的 {@link VideoRecordPlan} 列表，若无匹配记录则返回空列表
     */
    List<VideoRecordPlan> listActivePlans();

    /**
     * 激活指定的录像计划。
     *
     * <p>将 {@link VideoRecordPlan} 的状态更新为启用，
     * 激活后该计划将参与录像调度。</p>
     *
     * @param planId 要激活的录像计划主键 ID，不能为 {@code null}
     * @throws IllegalArgumentException 当指定 planId 对应的录像计划不存在时
     */
    void activatePlan(Long planId);

    /**
     * 停用指定的录像计划。
     *
     * <p>将 {@link VideoRecordPlan} 的状态更新为禁用，
     * 停用后该计划不再参与录像调度。</p>
     *
     * @param planId 要停用的录像计划主键 ID，不能为 {@code null}
     * @throws IllegalArgumentException 当指定 planId 对应的录像计划不存在时
     */
    void deactivatePlan(Long planId);
}
