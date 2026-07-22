package com.mqttsnet.thinglinks.video.media.zlm;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.cache.CacheSuperAbstract;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZLMServerConfig;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.manager.media.VideoMediaServerManager;
import com.mqttsnet.thinglinks.video.media.common.MediaApiResult;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.media.server.event.publisher.MediaEventPublisher;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 管理 ZLM 流媒体节点的状态
 * <p>
 * 由 iot-executor 通过 XXL-Job 调度触发心跳检测。
 * 该类是 executor → FacadeImpl 调用链中第一个业务入口 Bean，
 * 必须标注 {@code @DS} 以确保多租户场景下数据源正确切换，
 * 参照 link 模块 {@code DeviceCacheService} 的处理方式。
 * </p>
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class ZLMMediaServerStatusManager extends CacheSuperAbstract {

    private final String mediaServerType = VideoMediaServerTypeEnum.ZLM.getValue();
    private final ZlmRestClient zlmRestClient;
    private final VideoMediaServerService videoMediaServerService;
    private final VideoMediaServerManager videoMediaServerManager;
    private final MediaEventPublisher eventPublisher;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;
    @Value("${media.hook-domain-prefix:http://127.0.0.1:18760/video/zlmHook/index/hook}")
    private String mediaHookDomainPrefix;

    /**
     * 供 VideoJobHandlerFacadeImpl 调用的入口方法（已设置租户上下文）。
     * <p>
     * 原 {@code @Scheduled(fixedRate = 60 * 1000)} 已移除，
     * 改由 iot-executor 通过 XXL-Job 调度触发。
     * </p>
     *
     * @param tenantId 租户ID
     */
    public void executeForTenant(Long tenantId) {
        try {
            ContextUtil.setTenantId(tenantId);
            executeMediaServerCacheRefresh(String.valueOf(tenantId));
        } catch (Exception e) {
            log.warn("[ZLM-心跳检测] 租户ID: {} ,执行缓存刷新异常", tenantId, e);
        }
    }

    private void executeMediaServerCacheRefresh(String tenantId) {
        //获取所有媒体服务器
        VideoMediaServerPageQuery mediaServerPageQuery = new VideoMediaServerPageQuery()
                .setType(mediaServerType);
        List<VideoMediaServerResultDTO> mediaServerResultDTOList = videoMediaServerService.getVideoMediaServerResultDTOList(mediaServerPageQuery);

        if (CollUtil.isEmpty(mediaServerResultDTOList)) {
            log.info("[获取媒体服务器列表] tenantId: {} ,未找到媒体服务器", tenantId);
            return;
        }

        // 每次心跳都对每个节点做真实探测——cache 不再决定走不走业务，仅作为最近心跳的快照（业务侧可读）
        mediaServerResultDTOList.forEach(this::heartbeatOne);
    }

    /**
     * 单节点心跳：探测 → 落库（幂等）→ 状态翻转才发事件 → 顺手刷新 cache。
     * <p>事件机制只用于"状态翻转通知"（避免狂发上线/离线事件给订阅者）。
     * DB 一致性由 {@code service.serverOnline/serverOffline} 直接写保证，不依赖事件链是否能跑到。
     */
    private void heartbeatOne(VideoMediaServerResultDTO server) {
        String mediaId = server.getMediaIdentification();
        boolean wasOnline = Boolean.TRUE.equals(server.getOnlineStatus());
        ZLMServerConfig config = probeServerConfig(server);
        boolean reachable = config != null;

        if (reachable) {
            log.info("[ZLM-心跳成功] ID={}, 地址={}:{}", mediaId, server.getHost(), server.getHttpPort());
            initZLMMediaServerPort(server, config);
            server.setOnlineStatus(true);
            // 直接落库（幂等）
            videoMediaServerService.serverOnline(server);
            if (!wasOnline) {
                log.info("[ZLM-状态翻转] OFFLINE → ONLINE: ID={}", mediaId);
                eventPublisher.mediaServerOnlineEventPublish(server);
                // 首次上线时同步 hook 配置
                if (Boolean.TRUE.equals(server.getAutoConfig())) {
                    setZLMConfig(server, "0".equals(config.getHookEnable())
                            || !Objects.equals(server.getHookAliveInterval(), config.getHookAliveInterval()));
                }
            }
            collectAndUpdateMetrics(server);
            // cache 仅作为最近心跳快照——给业务侧（video-server）快速读，不影响下一轮 Job 走向
            videoMediaServerManager.putOnlineHookCache(mediaServerType, mediaId, server);
        } else {
            log.warn("[ZLM-心跳失败] ID={}, 地址={}:{}", mediaId, server.getHost(), server.getHttpPort());
            server.setOnlineStatus(false);
            videoMediaServerService.serverOffline(server);
            if (wasOnline) {
                log.warn("[ZLM-状态翻转] ONLINE → OFFLINE: ID={}", mediaId);
                eventPublisher.mediaServerOfflineEventPublish(server);
            }
            // 离线时清掉心跳快照——业务侧读 cache 可作"最近不可达"判定
            videoMediaServerManager.removeOnlineHookCache(mediaServerType, mediaId);
        }
    }

    /**
     * 探测 ZLM 服务器配置；不可达或返回异常都返回 null（由调用方判定离线）。
     */
    private ZLMServerConfig probeServerConfig(VideoMediaServerResultDTO server) {
        try {
            MediaApiResult result = zlmRestClient.get(toEntity(server), "getServerConfig", null);
            if (!result.isSuccess() || result.getData() == null) {
                return null;
            }
            JSONArray data = result.getData().getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return null;
            }
            return JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);
        } catch (Exception e) {
            log.warn("[ZLM-探测异常] ID={}, error={}", server.getMediaIdentification(), e.getMessage());
            return null;
        }
    }


    /**
     * 在线处理
     *
     * @param mediaServerItem 媒体服务器
     * @param config          配置
     */
    private void handleOnline(VideoMediaServerResultDTO mediaServerItem, ZLMServerConfig config) {
        //如果当前状态为离线、则进行连接
        if (mediaServerItem.getOnlineStatus()) {
            log.info("[ZLM-心跳检测成功] ID：{}, 地址： {}:{}", mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
            return;
        }
        log.info("[ZLM-重新连接成功] ID：{}, 地址： {}:{}", mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
        mediaServerItem.setOnlineStatus(true);
        mediaServerItem.setHookAliveInterval(60);
        // 发送上线通知
        eventPublisher.mediaServerOnlineEventPublish(mediaServerItem);
        // 自动配置
        if (mediaServerItem.getAutoConfig()) {
            if (null == config) {
                MediaApiResult configResult = zlmRestClient.get(toEntity(mediaServerItem), "getServerConfig", null);
                if (!configResult.isSuccess()) {
                    log.info("[ZLM-尝试连接]失败, ID：{}, 地址： {}:{}", mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
                    return;
                }
                JSONArray data = configResult.getData().getJSONArray("data");
                if (data != null && !data.isEmpty()) {
                    config = JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);
                }
            }
            if (null != config) {
                initZLMMediaServerPort(mediaServerItem, config);
                setZLMConfig(mediaServerItem, "0".equals(config.getHookEnable()) || !Objects.equals(mediaServerItem.getHookAliveInterval(), config.getHookAliveInterval()));
            }
        }
    }


    /**
     * 离线处理
     *
     * @param mediaServerItem 媒体服务器
     */
    private void handleOffline(VideoMediaServerResultDTO mediaServerItem) {
        log.warn("[ZLM-心跳超时] 更新zlm状态为离线 ID：{}", mediaServerItem.getMediaIdentification());
        mediaServerItem.setOnlineStatus(false);
        // 发送离线通知
        eventPublisher.mediaServerOfflineEventPublish(mediaServerItem);
    }

    private void initZLMMediaServerPort(VideoMediaServerResultDTO mediaServerItem, ZLMServerConfig zlmServerConfig) {
        // 端口只会从配置中读取一次，一旦自己配置或者读取过了将不在配置
        if (mediaServerItem.getHttpSslPort() == 0) {
            mediaServerItem.setHttpSslPort(zlmServerConfig.getHttpSSLport());
        }
        if (mediaServerItem.getRtmpPort() == 0) {
            mediaServerItem.setRtmpPort(zlmServerConfig.getRtmpPort());
        }
        if (mediaServerItem.getRtmpSslPort() == 0) {
            mediaServerItem.setRtmpSslPort(zlmServerConfig.getRtmpSslPort());
        }
        if (mediaServerItem.getRtspPort() == 0) {
            mediaServerItem.setRtspPort(zlmServerConfig.getRtspPort());
        }
        if (mediaServerItem.getRtspSslPort() == 0) {
            mediaServerItem.setRtspSslPort(zlmServerConfig.getRtspSSlport());
        }
        if (mediaServerItem.getRtpProxyPort() == 0) {
            mediaServerItem.setRtpProxyPort(zlmServerConfig.getRtpProxyPort());
        }
        if (mediaServerItem.getFlvSslPort() == 0) {
            mediaServerItem.setFlvSslPort(zlmServerConfig.getHttpSSLport());
        }
        if (mediaServerItem.getWsFlvSslPort() == 0) {
            mediaServerItem.setWsFlvSslPort(zlmServerConfig.getHttpSSLport());
        }
        if (Objects.isNull(zlmServerConfig.getTranscodeSuffix())) {
            mediaServerItem.setTranscodeSuffix(null);
        } else {
            mediaServerItem.setTranscodeSuffix(zlmServerConfig.getTranscodeSuffix());
        }
        mediaServerItem.setHookAliveInterval(10);
    }

    public void setZLMConfig(VideoMediaServerResultDTO mediaServerItem, boolean restart) {
        log.info("[媒体服务节点] 正在设置 ：{} -> {}:{}",
                mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());

        // Hook 回调 URL 前缀：优先用 DB 里 per-ZLM 的 hookHost（必须是合法 http(s) URL），
        // 否则用 Nacos 全局 media.hook-domain-prefix。集群场景下若某个 ZLM 需要回调到不同的平台 Gateway，
        // 可以在"流媒体管理"页面给该 ZLM 单独填 hookHost，不填走全局。
        String hookPrefix;
        String hookHostFromDb = mediaServerItem.getHookHost();
        if (cn.hutool.core.util.StrUtil.isNotBlank(hookHostFromDb)
                && (hookHostFromDb.startsWith("http://") || hookHostFromDb.startsWith("https://"))) {
            hookPrefix = hookHostFromDb;
            log.info("[媒体服务节点] Hook 前缀使用 per-ZLM 配置: mediaIdentification={}, hookHost={}",
                    mediaServerItem.getMediaIdentification(), hookPrefix);
        } else {
            hookPrefix = mediaHookDomainPrefix;
            if (cn.hutool.core.util.StrUtil.isNotBlank(hookHostFromDb)) {
                log.warn("[媒体服务节点] DB hookHost={} 不是合法 URL（必须 http:// 或 https:// 开头），" +
                                "fallback 到 Nacos 全局 media.hook-domain-prefix={}",
                        hookHostFromDb, hookPrefix);
            }
        }

        Map<String, Object> param = new HashMap<>();
        // -profile:v Baseline
        param.put("api.secret", mediaServerItem.getSecret());
        if (mediaServerItem.getRtspPort() != 0) {
            param.put("ffmpeg.snap", "%s -rtsp_transport tcp -i %s -y -f mjpeg -frames:v 1 %s");
        }
        param.put("hook.enable", "1");
        param.put("hook.on_flow_report", "");
        param.put("hook.on_play", String.format("%s/on_play", hookPrefix));
        param.put("hook.on_http_access", "");
        param.put("hook.on_publish", String.format("%s/on_publish", hookPrefix));
        param.put("hook.on_record_ts", "");
        param.put("hook.on_rtsp_auth", "");
        param.put("hook.on_rtsp_realm", "");
        param.put("hook.on_server_started", String.format("%s/on_server_started", hookPrefix));
        param.put("hook.on_shell_login", "");
        param.put("hook.on_stream_changed", String.format("%s/on_stream_changed", hookPrefix));
        param.put("hook.on_stream_none_reader", String.format("%s/on_stream_none_reader", hookPrefix));
        param.put("hook.on_stream_not_found", String.format("%s/on_stream_not_found", hookPrefix));
        param.put("hook.on_server_keepalive", String.format("%s/on_server_keepalive", hookPrefix));
        param.put("hook.on_send_rtp_stopped", String.format("%s/on_send_rtp_stopped", hookPrefix));
        param.put("hook.on_rtp_server_timeout", String.format("%s/on_rtp_server_timeout", hookPrefix));
        param.put("hook.on_record_mp4", String.format("%s/on_record_mp4", hookPrefix));
        param.put("hook.timeoutSec", "30");
        param.put("hook.alive_interval", mediaServerItem.getHookAliveInterval());
        // 推流断开后可以在超时时间内重新连接上继续推流，这样播放器会接着播放。
        // 置0关闭此特性(推流断开会导致立即断开播放器)
        // 此参数不应大于播放器超时时间
        // 优化此消息以更快的收到流注销事件
        param.put("protocol.continue_push_ms", "3000");
        // 最多等待未初始化的Track时间，单位毫秒，超时之后会忽略未初始化的Track, 设置此选项优化那些音频错误的不规范流，
        // 等zlm支持给每个rtpServer设置关闭音频的时候可以不设置此选项
        if (mediaServerItem.getRtpEnable() && !ObjectUtils.isEmpty(mediaServerItem.getRtpPortRange())) {
            param.put("rtp_proxy.port_range", mediaServerItem.getRtpPortRange().replace(",", "-"));
        } else {
            param.put("rtp_proxy.port", mediaServerItem.getRtpProxyPort());
        }

        if (!ObjectUtils.isEmpty(mediaServerItem.getRecordPath())) {
            File recordPathFile = new File(mediaServerItem.getRecordPath());
            param.put("protocol.mp4_save_path", recordPathFile.getParentFile().getPath());
            param.put("protocol.downloadRoot", recordPathFile.getParentFile().getPath());
            param.put("record.appName", recordPathFile.getName());
        }

        MediaApiResult setConfigResult = zlmRestClient.postForm(toEntity(mediaServerItem), "setServerConfig", param);

        if (setConfigResult.isSuccess()) {
            if (restart) {
                log.info("[媒体服务节点] 设置成功,开始重启以保证配置生效 {} -> {}:{}",
                        mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
                zlmRestClient.get(toEntity(mediaServerItem), "restartServer", null);
            } else {
                log.info("[媒体服务节点] 设置成功 {} -> {}:{}",
                        mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
            }
        } else {
            log.info("[媒体服务节点] 设置媒体服务节点失败 {} -> {}:{}",
                    mediaServerItem.getMediaIdentification(), mediaServerItem.getHost(), mediaServerItem.getHttpPort());
        }
    }

    /**
     * 采集流媒体性能指标并更新数据库。
     *
     * <p>通过 {@link MediaNodeServiceFactory} 自动路由到 ZLM/ABL 对应的实现。
     *
     * <p><b>失败时清缓存，让下一轮心跳 Job 走完整探测路径决定上下线</b>——避免节点已死但 cache TTL
     * 还没过期、平台一直显示"在线"的悬挂窗口。两步降级（删 cache → 下轮完整探测 → 失败才 handleOffline）
     * 也避免单次抖动误判离线，平衡敏感度和稳定性。
     *
     * @param mediaServerItem 流媒体服务器信息
     */
    private void collectAndUpdateMetrics(VideoMediaServerResultDTO mediaServerItem) {
        try {
            VideoMediaServer entity = toEntity(mediaServerItem);
            VideoMediaServerMetricsResultVO metrics = mediaNodeServiceFactory
                    .getService(entity)
                    .getServerMetrics(entity);

            videoMediaServerService.updateServerMetrics(
                    mediaServerItem.getMediaIdentification(),
                    metrics.getCpuUsage(),
                    metrics.getMemoryUsage(),
                    metrics.getCurrentStreams(),
                    metrics.getNetworkInSpeed(),
                    metrics.getNetworkOutSpeed());
        } catch (Exception e) {
            log.warn("[ZLM-指标采集] 采集失败，清除心跳缓存以触发下一轮完整探测: mediaId={}, error={}",
                    mediaServerItem.getMediaIdentification(), e.getMessage());
            videoMediaServerManager.removeOnlineHookCache(mediaServerType, mediaServerItem.getMediaIdentification());
        }
    }

    /**
     * 将 DTO 转换为 VideoMediaServer（仅设置 REST 客户端所需字段）
     */
    private VideoMediaServer toEntity(VideoMediaServerResultDTO dto) {
        VideoMediaServer entity = new VideoMediaServer();
        entity.setHost(dto.getHost());
        entity.setHttpPort(dto.getHttpPort());
        entity.setSecret(dto.getSecret());
        entity.setType(dto.getType());
        return entity;
    }

}
