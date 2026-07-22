package com.mqttsnet.thinglinks.video.media.zlm.event.listener;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.event.MediaServerChangeEvent;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 监听媒体服务器变更事件，处理ZLM服务器的变更。
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/13
 */
@Slf4j
@Component
public class MediaServerChangeEventListener {

    private final String mediaServerType = VideoMediaServerTypeEnum.ZLM.getValue();


    @EventListener
    public void onApplicationEvent(MediaServerChangeEvent event) {
        if (event.getMediaServerItemList() == null || event.getMediaServerItemList().isEmpty()) {
            return;
        }
        for (VideoMediaServerResultDTO mediaServerItem : event.getMediaServerItemList()) {
            if (!mediaServerType.equals(mediaServerItem.getType())) {
                continue;
            }
            log.info("[ZLM-添加待上线节点] ID：" + mediaServerItem.getMediaIdentification());
        }
    }

}
