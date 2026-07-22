package com.mqttsnet.thinglinks.video.media.zlm.event.listener;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.media.zlm.event.HookZlmServerStartEvent;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 监听ZLM服务器启动事件，处理ZLM服务器的启动。
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookZlmServerStartEventListener {

    private final String mediaServerType = VideoMediaServerTypeEnum.ZLM.getValue();

    private final VideoMediaServerService videoMediaServerService;


    @EventListener
    public void onApplicationEvent(HookZlmServerStartEvent event) {
        if (event.getMediaServerItem() == null
            || !mediaServerType.equals(event.getMediaServerItem().getType())
            || event.getMediaServerItem().getOnlineStatus()) {
            return;
        }
        VideoMediaServerResultDTO serverItem = videoMediaServerService.getVideoMediaServerResultDTO(event.getMediaServerItem().getMediaIdentification());
        if (serverItem == null) {
            return;
        }
        log.info("[ZLM-HOOK事件-服务启动] ID：" + event.getMediaServerItem().getMediaIdentification());
        // TODO 处理ZLM服务启动后的逻辑
//        onlineCheck(serverItem, null);
    }

}
