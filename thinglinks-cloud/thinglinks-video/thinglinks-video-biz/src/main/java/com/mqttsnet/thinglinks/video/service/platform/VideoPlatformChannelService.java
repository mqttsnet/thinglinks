package com.mqttsnet.thinglinks.video.service.platform;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformChannel;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformChannelResultVO;

import java.util.List;

/**
 * Description:
 * 级联平台通道业务接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoPlatformChannelService extends SuperService<Long, VideoPlatformChannel> {

    List<VideoPlatformChannel> listByPlatformId(Long platformId);

    void bindChannel(Long platformId, Long deviceChannelId, Long catalogId, String deviceIdentification, String channelIdentification);

    void unbindChannel(Long platformId, Long deviceChannelId);

    /**
     * 查询指定平台已绑定的通道列表（返回ResultVO）。
     */
    List<VideoPlatformChannelResultVO> listResultVOByPlatformId(Long platformId);
}
