package com.mqttsnet.thinglinks.video.media.server.event.listener;

import com.mqttsnet.thinglinks.video.dto.media.event.MediaServerOfflineEvent;
import com.mqttsnet.thinglinks.video.dto.media.event.MediaServerOnlineEvent;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


/**
 * @description: 在线事件监听器，监听到离线后，修改设备离在线状态。 设备在线有两个来源：
 * 1、设备主动注销，发送注销指令
 * 2、设备未知原因离线，心跳超时
 * @author: mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaServerStatusEventListener {

    private final VideoMediaServerService videoMediaServerService;

    @EventListener
    public void onApplicationEvent(MediaServerOnlineEvent event) {
        log.info("[媒体节点] 上线 ID：" + event.getMediaServer().getMediaIdentification());
        videoMediaServerService.serverOnline(event.getMediaServer());
    }

    @EventListener
    public void onApplicationEvent(MediaServerOfflineEvent event) {
        log.info("[媒体节点] 离线，ID：" + event.getMediaServer().getMediaIdentification());
        videoMediaServerService.serverOffline(event.getMediaServer());
    }
}
