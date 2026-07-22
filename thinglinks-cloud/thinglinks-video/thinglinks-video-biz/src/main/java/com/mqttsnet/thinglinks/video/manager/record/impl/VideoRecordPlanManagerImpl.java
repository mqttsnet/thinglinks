package com.mqttsnet.thinglinks.video.manager.record.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;
import com.mqttsnet.thinglinks.video.manager.record.VideoRecordPlanManager;
import com.mqttsnet.thinglinks.video.mapper.record.VideoRecordPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 录像计划 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoRecordPlanManagerImpl extends SuperManagerImpl<VideoRecordPlanMapper, VideoRecordPlan> implements VideoRecordPlanManager {

    @Override
    public List<VideoRecordPlan> listActivePlans() {
        QueryWrap<VideoRecordPlan> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoRecordPlan::getPlanStatus, 1);
        return list(queryWrap);
    }
}
