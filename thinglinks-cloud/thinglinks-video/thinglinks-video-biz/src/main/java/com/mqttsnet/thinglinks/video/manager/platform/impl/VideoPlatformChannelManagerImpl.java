package com.mqttsnet.thinglinks.video.manager.platform.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformChannel;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformChannelManager;
import com.mqttsnet.thinglinks.video.mapper.platform.VideoPlatformChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 级联平台通道 Manager 实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformChannelManagerImpl extends SuperManagerImpl<VideoPlatformChannelMapper, VideoPlatformChannel> implements VideoPlatformChannelManager {

    @Override
    public List<VideoPlatformChannel> listByPlatformId(Long platformId) {
        QueryWrap<VideoPlatformChannel> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoPlatformChannel::getPlatformId, platformId);
        return list(queryWrap);
    }
}
