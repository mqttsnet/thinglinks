package com.mqttsnet.thinglinks.video.manager.group.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroupRelation;
import com.mqttsnet.thinglinks.video.manager.group.VideoDeviceGroupRelationManager;
import com.mqttsnet.thinglinks.video.mapper.group.VideoDeviceGroupRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 设备分组关联 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceGroupRelationManagerImpl extends SuperManagerImpl<VideoDeviceGroupRelationMapper, VideoDeviceGroupRelation> implements VideoDeviceGroupRelationManager {

    @Override
    public List<VideoDeviceGroupRelation> listByGroupId(Long groupId) {
        QueryWrap<VideoDeviceGroupRelation> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoDeviceGroupRelation::getGroupId, groupId)
                .orderByAsc(VideoDeviceGroupRelation::getSortOrder);
        return list(queryWrap);
    }

    @Override
    public List<VideoDeviceGroupRelation> listByDeviceIdentification(String deviceIdentification) {
        QueryWrap<VideoDeviceGroupRelation> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoDeviceGroupRelation::getDeviceIdentification, deviceIdentification);
        return list(queryWrap);
    }

    @Override
    public void removeByGroupId(Long groupId) {
        QueryWrap<VideoDeviceGroupRelation> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoDeviceGroupRelation::getGroupId, groupId);
        remove(queryWrap);
    }
}
