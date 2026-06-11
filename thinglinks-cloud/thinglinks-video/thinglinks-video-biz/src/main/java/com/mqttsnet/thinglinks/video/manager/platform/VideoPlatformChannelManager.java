package com.mqttsnet.thinglinks.video.manager.platform;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatformChannel;

import java.util.List;

/**
 * Description:
 * 级联平台通道 Manager 接口。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface VideoPlatformChannelManager extends SuperManager<VideoPlatformChannel> {

    List<VideoPlatformChannel> listByPlatformId(Long platformId);
}
