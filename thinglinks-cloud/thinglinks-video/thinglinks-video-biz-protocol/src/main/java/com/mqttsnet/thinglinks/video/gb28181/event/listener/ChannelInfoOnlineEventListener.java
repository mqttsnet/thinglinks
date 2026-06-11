package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoChannelCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.ChannelInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:
 * 通道上线事件监听者
 * <p>
 * 从 DB 拉最新通道信息并缓存到 Redis。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-17
 */
@Slf4j
@Component
public class ChannelInfoOnlineEventListener {

    @Autowired
    private VideoCacheDataHelper videoCacheDataHelper;

    @Autowired
    private VideoChannelService videoChannelService;

    @EventListener
    public void handleOnlineEvent(ChannelInfoOnlineEvent event) {
        Optional.ofNullable(event.getSource()).ifPresent(source -> {
            log.info("[通道上线事件监听] 设备: {}, 通道: {}",
                    source.getDeviceIdentification(), source.getChannelIdentification());

            var latest = Optional.ofNullable(videoChannelService.getByChannelIdentification(source.getChannelIdentification()))
                    .orElse(source);
            var cacheVO = BeanPlusUtil.toBeanIgnoreError(latest, VideoChannelCacheVO.class);
            videoCacheDataHelper.setChannelInfo(cacheVO);
        });
    }
}
