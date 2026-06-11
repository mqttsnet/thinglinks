package com.mqttsnet.thinglinks.video.media.abl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.manager.media.VideoMediaServerManager;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.media.server.event.publisher.MediaEventPublisher;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理 ABL 流媒体节点的状态
 * <p>
 * 参照 {@link com.mqttsnet.thinglinks.video.media.zlm.ZLMMediaServerStatusManager} 实现，
 * 由 iot-executor 通过 XXL-Job 调度触发心跳检测。
 * 该类是 executor → FacadeImpl 调用链中第一个业务入口 Bean，
 * 必须标注 {@code @DS} 以确保多租户场景下数据源正确切换，
 * 参照 link 模块 {@code DeviceCacheService} 的处理方式。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-05
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class ABLMediaServerStatusManager extends CacheSuperAbstract {

    private final String mediaServerType = VideoMediaServerTypeEnum.ABL.getValue();
    private final VideoMediaServerService videoMediaServerService;
    private final VideoMediaServerManager videoMediaServerManager;
    private final MediaEventPublisher eventPublisher;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;

    /**
     * 供 VideoJobHandlerFacadeImpl 调用的入口方法。
     *
     * @param tenantId 租户ID
     */
    public void executeForTenant(Long tenantId) {
        try {
            ContextUtil.setTenantId(tenantId);
            executeMediaServerCacheRefresh(String.valueOf(tenantId));
        } catch (Exception e) {
            log.warn("[ABL-心跳检测] 租户ID: {} ,执行缓存刷新异常", tenantId, e);
        }
    }

    private void executeMediaServerCacheRefresh(String tenantId) {
        VideoMediaServerPageQuery query = new VideoMediaServerPageQuery()
                .setType(mediaServerType);
        List<VideoMediaServerResultDTO> serverList = videoMediaServerService.getVideoMediaServerResultDTOList(query);

        if (CollUtil.isEmpty(serverList)) {
            log.debug("[ABL-心跳检测] tenantId: {} ,未找到ABL媒体服务器", tenantId);
            return;
        }

        // 每次心跳都对每个节点做真实探测——cache 不再决定走不走业务，仅作为最近心跳快照（业务侧可读）
        serverList.forEach(this::heartbeatOne);
    }

    /**
     * 单节点心跳：探测 → 落库（幂等）→ 状态翻转才发事件 → 顺手刷新 cache。
     */
    private void heartbeatOne(VideoMediaServerResultDTO server) {
        String mediaId = server.getMediaIdentification();
        boolean wasOnline = Boolean.TRUE.equals(server.getOnlineStatus());
        VideoMediaServerMetricsResultVO metrics = probeMetrics(server);
        boolean reachable = metrics != null;

        if (reachable) {
            log.info("[ABL-心跳成功] ID={}, 地址={}:{}", mediaId, server.getHost(), server.getHttpPort());
            server.setOnlineStatus(true);
            // 直接落库（幂等）
            videoMediaServerService.serverOnline(server);
            // 顺手把指标也写一次
            try {
                videoMediaServerService.updateServerMetrics(mediaId,
                        metrics.getCpuUsage(), metrics.getMemoryUsage(),
                        metrics.getCurrentStreams(), metrics.getNetworkInSpeed(), metrics.getNetworkOutSpeed());
            } catch (Exception e) {
                log.debug("[ABL-指标更新] 失败（忽略）: mediaId={}, error={}", mediaId, e.getMessage());
            }
            if (!wasOnline) {
                log.info("[ABL-状态翻转] OFFLINE → ONLINE: ID={}", mediaId);
                eventPublisher.mediaServerOnlineEventPublish(server);
            }
            videoMediaServerManager.putOnlineHookCache(mediaServerType, mediaId, server);
        } else {
            log.warn("[ABL-心跳失败] ID={}, 地址={}:{}", mediaId, server.getHost(), server.getHttpPort());
            server.setOnlineStatus(false);
            videoMediaServerService.serverOffline(server);
            if (wasOnline) {
                log.warn("[ABL-状态翻转] ONLINE → OFFLINE: ID={}", mediaId);
                eventPublisher.mediaServerOfflineEventPublish(server);
            }
            videoMediaServerManager.removeOnlineHookCache(mediaServerType, mediaId);
        }
    }

    /**
     * 探测 ABL 服务器指标；不可达或返回异常都返回 null（由调用方判定离线）。
     */
    private VideoMediaServerMetricsResultVO probeMetrics(VideoMediaServerResultDTO server) {
        try {
            VideoMediaServer entity = toEntity(server);
            return mediaNodeServiceFactory.getService(entity).getServerMetrics(entity);
        } catch (Exception e) {
            log.warn("[ABL-探测异常] ID={}, error={}", server.getMediaIdentification(), e.getMessage());
            return null;
        }
    }

    private void collectAndUpdateMetrics(VideoMediaServerResultDTO server) {
        try {
            VideoMediaServer entity = toEntity(server);
            VideoMediaServerMetricsResultVO metrics = mediaNodeServiceFactory
                    .getService(entity)
                    .getServerMetrics(entity);

            videoMediaServerService.updateServerMetrics(
                    server.getMediaIdentification(),
                    metrics.getCpuUsage(),
                    metrics.getMemoryUsage(),
                    metrics.getCurrentStreams(),
                    metrics.getNetworkInSpeed(),
                    metrics.getNetworkOutSpeed());
        } catch (Exception e) {
            log.debug("[ABL-指标采集] 采集失败: mediaId={}, error={}",
                    server.getMediaIdentification(), e.getMessage());
        }
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
