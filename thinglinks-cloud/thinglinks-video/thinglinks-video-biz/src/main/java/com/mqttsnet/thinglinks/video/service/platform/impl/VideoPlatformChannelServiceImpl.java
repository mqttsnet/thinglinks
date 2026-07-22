package com.mqttsnet.thinglinks.video.service.platform.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import cn.hutool.core.bean.BeanUtil;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformChannel;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformChannelManager;
import com.mqttsnet.thinglinks.video.service.platform.VideoPlatformChannelService;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformChannelResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 * 级联平台通道业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformChannelServiceImpl extends SuperServiceImpl<VideoPlatformChannelManager, Long, VideoPlatformChannel> implements VideoPlatformChannelService {

    @Override
    public List<VideoPlatformChannel> listByPlatformId(Long platformId) {
        return superManager.listByPlatformId(platformId);
    }

    @Override
    public void bindChannel(Long platformId, Long deviceChannelId, Long catalogId, String deviceIdentification, String channelIdentification) {
        VideoPlatformChannel channel = VideoPlatformChannel.builder()
                .platformId(platformId)
                .deviceChannelId(deviceChannelId)
                .catalogId(catalogId)
                .deviceIdentification(deviceIdentification)
                .channelIdentification(channelIdentification)
                .build();
        superManager.save(channel);
        log.info("绑定平台通道: platformId={}, deviceIdentification={}, channelIdentification={}", platformId, deviceIdentification, channelIdentification);
    }

    @Override
    public void unbindChannel(Long platformId, Long deviceChannelId) {
        QueryWrap<VideoPlatformChannel> queryWrap = new QueryWrap<>();
        queryWrap.lambda()
                .eq(VideoPlatformChannel::getPlatformId, platformId)
                .eq(VideoPlatformChannel::getDeviceChannelId, deviceChannelId);
        superManager.remove(queryWrap);
        log.info("解绑平台通道: platformId={}, deviceChannelId={}", platformId, deviceChannelId);
    }

    @Override
    public List<VideoPlatformChannelResultVO> listResultVOByPlatformId(Long platformId) {
        return BeanUtil.copyToList(listByPlatformId(platformId), VideoPlatformChannelResultVO.class);
    }
}
