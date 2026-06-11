package com.mqttsnet.thinglinks.video.dto.device.event;

import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import lombok.Getter;

/**
 * Description:
 * 通道上线事件对象
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-17
 */
@Getter
public class ChannelInfoOnlineEvent extends ChannelInfoBaseEventAbstract<VideoChannelResultVO> {

    public ChannelInfoOnlineEvent(VideoChannelResultVO source) {
        super(source);
    }
}
