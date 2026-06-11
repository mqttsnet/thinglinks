package com.mqttsnet.thinglinks.video.dto.device.event;

import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import lombok.Getter;

/**
 * Description:
 * 通道下线事件对象
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-17
 */
@Getter
public class ChannelInfoOfflineEvent extends ChannelInfoBaseEventAbstract<VideoChannelResultVO> {

    public ChannelInfoOfflineEvent(VideoChannelResultVO source) {
        super(source);
    }
}
