package com.mqttsnet.thinglinks.video.manager.record;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;

import java.util.List;

/**
 * 录像计划 Manager 接口。
 *
 * <p>封装录像计划的数据访问逻辑，为
 * {@link com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService}
 * 提供底层数据操作支持。继承 {@link SuperManager} 获得通用持久化能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoRecordPlan
 * @see SuperManager
 * @see com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService
 */
public interface VideoRecordPlanManager extends SuperManager<VideoRecordPlan> {

    /**
     * 查询所有处于启用状态的录像计划。
     *
     * <p>底层数据访问方法，按 {@link VideoRecordPlan} 的 status 字段
     * 过滤出启用状态的记录。</p>
     *
     * @return 启用状态的 {@link VideoRecordPlan} 列表，若无匹配记录则返回空列表
     */
    List<VideoRecordPlan> listActivePlans();
}
