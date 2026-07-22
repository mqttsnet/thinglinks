package com.mqttsnet.thinglinks.video.service.record.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;
import com.mqttsnet.thinglinks.video.manager.record.VideoRecordPlanManager;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 录像计划业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoRecordPlanServiceImpl extends SuperServiceImpl<VideoRecordPlanManager, Long, VideoRecordPlan> implements VideoRecordPlanService {

    @Override
    public List<VideoRecordPlan> listActivePlans() {
        return superManager.listActivePlans();
    }

    @Override
    public void activatePlan(Long planId) {
        VideoRecordPlan plan = superManager.getById(planId);
        if (plan != null) {
            plan.setPlanStatus(1);
            superManager.updateById(plan);
            log.info("激活录像计划: planId={}, planName={}", planId, plan.getPlanName());
        }
    }

    @Override
    public void deactivatePlan(Long planId) {
        VideoRecordPlan plan = superManager.getById(planId);
        if (plan != null) {
            plan.setPlanStatus(0);
            superManager.updateById(plan);
            log.info("停用录像计划: planId={}, planName={}", planId, plan.getPlanName());
        }
    }
}
