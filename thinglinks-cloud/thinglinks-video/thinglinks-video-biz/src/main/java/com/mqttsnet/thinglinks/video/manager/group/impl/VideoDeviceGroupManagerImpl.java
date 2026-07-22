package com.mqttsnet.thinglinks.video.manager.group.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroup;
import com.mqttsnet.thinglinks.video.manager.group.VideoDeviceGroupManager;
import com.mqttsnet.thinglinks.video.mapper.group.VideoDeviceGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 设备分组 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceGroupManagerImpl extends SuperManagerImpl<VideoDeviceGroupMapper, VideoDeviceGroup> implements VideoDeviceGroupManager {

    @Override
    public List<VideoDeviceGroup> listByParentId(Long parentId) {
        QueryWrap<VideoDeviceGroup> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoDeviceGroup::getParentId, parentId)
                .orderByAsc(VideoDeviceGroup::getSortOrder);
        return list(queryWrap);
    }

    @Override
    public List<VideoDeviceGroup> listAllEnabled() {
        QueryWrap<VideoDeviceGroup> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoDeviceGroup::getEnable, true)
                .orderByAsc(VideoDeviceGroup::getGroupLevel)
                .orderByAsc(VideoDeviceGroup::getSortOrder);
        return list(queryWrap);
    }
}
