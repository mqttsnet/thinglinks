package com.mqttsnet.thinglinks.video.manager.device.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceManager;
import com.mqttsnet.thinglinks.video.mapper.device.VideoDeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用业务实现类 - 统一设备表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceManagerImpl extends SuperManagerImpl<VideoDeviceMapper, VideoDevice> implements VideoDeviceManager {

    private final VideoDeviceMapper videoDeviceMapper;

    @Override
    public VideoDevice getOneByDeviceIdentification(String deviceIdentification) {
        QueryWrap<VideoDevice> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(deviceIdentification), VideoDevice::getDeviceIdentification, deviceIdentification);
        return videoDeviceMapper.selectOne(queryWrap);
    }

    @Override
    public List<VideoDevice> listByAccessProtocol(String accessProtocol) {
        QueryWrap<VideoDevice> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(accessProtocol), VideoDevice::getAccessProtocol, accessProtocol);
        return videoDeviceMapper.selectList(queryWrap);
    }
}
