package com.mqttsnet.thinglinks.video.manager.device.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.manager.device.VideoChannelManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用业务实现类 - 统一通道表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoChannelManagerImpl extends SuperManagerImpl<VideoChannelMapper, VideoChannel> implements VideoChannelManager {

    private final VideoChannelMapper videoChannelMapper;

    @Override
    public List<VideoChannel> listByDeviceIdentification(String deviceIdentification) {
        QueryWrap<VideoChannel> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(deviceIdentification), VideoChannel::getDeviceIdentification, deviceIdentification);
        return videoChannelMapper.selectList(queryWrap);
    }

    @Override
    public VideoChannel getOneByChannelIdentification(String channelIdentification) {
        QueryWrap<VideoChannel> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(channelIdentification), VideoChannel::getChannelIdentification, channelIdentification);
        return videoChannelMapper.selectOne(queryWrap);
    }

    @Override
    public VideoChannel getOneByDeviceAndChannel(String deviceIdentification, String channelIdentification) {
        QueryWrap<VideoChannel> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(CharSequenceUtil.isNotBlank(deviceIdentification), VideoChannel::getDeviceIdentification, deviceIdentification)
                .eq(CharSequenceUtil.isNotBlank(channelIdentification), VideoChannel::getChannelIdentification, channelIdentification);
        return videoChannelMapper.selectOne(queryWrap);
    }
}
