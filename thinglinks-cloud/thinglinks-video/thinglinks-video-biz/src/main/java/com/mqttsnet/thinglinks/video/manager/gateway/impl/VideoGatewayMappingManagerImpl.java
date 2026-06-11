package com.mqttsnet.thinglinks.video.manager.gateway.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;
import com.mqttsnet.thinglinks.video.manager.gateway.VideoGatewayMappingManager;
import com.mqttsnet.thinglinks.video.mapper.gateway.VideoGatewayMappingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用业务实现类 - 网关协议映射表
 *
 * @author mqttsnet
 * @date 2026-03-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoGatewayMappingManagerImpl extends SuperManagerImpl<VideoGatewayMappingMapper, VideoGatewayMapping> implements VideoGatewayMappingManager {

    private final VideoGatewayMappingMapper videoGatewayMappingMapper;

    @Override
    public List<VideoGatewayMapping> listBySrcDeviceIdentification(String srcDeviceIdentification) {
        QueryWrap<VideoGatewayMapping> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(srcDeviceIdentification), VideoGatewayMapping::getSrcDeviceIdentification, srcDeviceIdentification);
        return videoGatewayMappingMapper.selectList(queryWrap);
    }

    @Override
    public VideoGatewayMapping getOneByGbDeviceId(String gbDeviceId) {
        QueryWrap<VideoGatewayMapping> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(gbDeviceId), VideoGatewayMapping::getGbDeviceId, gbDeviceId);
        return videoGatewayMappingMapper.selectOne(queryWrap);
    }

    @Override
    public VideoGatewayMapping getOneByGbChannelId(String gbChannelId) {
        QueryWrap<VideoGatewayMapping> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(CharSequenceUtil.isNotBlank(gbChannelId), VideoGatewayMapping::getGbChannelId, gbChannelId);
        return videoGatewayMappingMapper.selectOne(queryWrap);
    }
}
