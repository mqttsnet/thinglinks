package com.mqttsnet.thinglinks.video.media.zlm.event.listener;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.media.zlm.event.HookZlmServerKeepaliveEvent;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * ZLMediaKit 心跳事件监听器。
 * <p>
 * 使用 ContextAwareExecutor + videoDefaultExecutor 异步执行，自动传递租户上下文。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HookZlmServerKeepaliveEventListener {

    private final VideoMediaServerService videoMediaServerService;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;
    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;

    @EventListener
    public void onApplicationEvent(HookZlmServerKeepaliveEvent event) {
        VideoMediaServerResultDTO serverItem = event.getMediaServerItem();
        if (serverItem == null) {
            return;
        }

        contextAwareExecutor.executeWithContext(() -> {
            String mediaId = serverItem.getMediaIdentification();
            // 从 mediaIdentification 提取租户 ID 并补充到上下文
            String tenantId = TenantUtil.extractTenantIdWithDefault(mediaId);
            ContextUtil.setTenantId(tenantId);

            log.debug("[ZLM-HOOK事件-心跳] ID：{}, tenantId：{}", mediaId, tenantId);

            try {
                VideoMediaServer entity = toEntity(serverItem);
                VideoMediaServerMetricsResultVO metrics = mediaNodeServiceFactory
                        .getService(entity)
                        .getServerMetrics(entity);

                videoMediaServerService.updateServerMetrics(
                        mediaId,
                        metrics.getCpuUsage(),
                        metrics.getMemoryUsage(),
                        metrics.getCurrentStreams(),
                        metrics.getNetworkInSpeed(),
                        metrics.getNetworkOutSpeed());
            } catch (Exception e) {
                log.debug("[ZLM-HOOK事件-心跳] 指标采集失败: mediaId={}, error={}", mediaId, e.getMessage());
            }
            return null;
        }, videoDefaultExecutor);
    }

    private VideoMediaServer toEntity(VideoMediaServerResultDTO dto) {
        VideoMediaServer entity = new VideoMediaServer();
        entity.setHost(dto.getHost());
        entity.setHttpPort(dto.getHttpPort());
        entity.setSecret(dto.getSecret());
        entity.setType(dto.getType());
        return entity;
    }
}
