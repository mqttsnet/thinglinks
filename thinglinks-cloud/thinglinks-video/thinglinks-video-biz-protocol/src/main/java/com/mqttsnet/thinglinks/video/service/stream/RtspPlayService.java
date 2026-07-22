package com.mqttsnet.thinglinks.video.service.stream;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.session.StreamInfoService;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * RTSP 主动拉流业务服务。
 * <p>
 * 平台对 accessProtocol = RTSP 的设备不通过 SIP 信令协商 SDP，而是直接把设备提供的 RTSP URL
 * 通过 ZLMediaKit 的 {@code addStreamProxy} 注册为拉流代理：ZLM 主动从摄像头拉 RTSP，
 * 落地为 RTMP / HTTP-FLV / HLS / WebRTC 等多协议供浏览器播放。
 *
 * <p>与 GB28181 的 {@code PlayService} 区别：
 * <ul>
 *   <li>无 SsrcTransaction 会话：ZLM 内部管理代理流，平台只缓存 {@link StreamInfo}</li>
 *   <li>streamId 稳定哈希：同一设备同通道多次点播复用同一路流，避免重复代理</li>
 *   <li>URL 优先级：{@code protocol_config.streamSource.url} > {@code host/port + streamPath + auth}</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class RtspPlayService {

    /**
     * ZLM 端拉流代理统一归入 "rtp" app，和 GB28181 保持一致。
     * 实际协议不会因此混淆：ZLM 按 app+stream 唯一标识，不关心上游协议。
     */
    private static final String DEFAULT_APP = "rtp";

    /** 默认 RTSP 端口（RFC 2326）。 */
    private static final int DEFAULT_RTSP_PORT = 554;

    private final VideoDeviceService videoDeviceService;
    private final VideoMediaServerService videoMediaServerService;
    private final ZlmMediaServerOpenAnyTenantService zlmMediaServerOpenAnyTenantService;
    private final StreamInfoService streamInfoService;

    /**
     * 发起 RTSP 实时拉流。
     *
     * @param deviceIdentification  设备编号
     * @param channelIdentification 通道编号（RTSP 单通道设备可空，缺省用 deviceIdentification）
     * @return 构建好的流信息（含多协议播放 URL）
     */
    public StreamInfo play(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");

        VideoDeviceResultVO device = Optional.ofNullable(videoDeviceService.getByDeviceIdentification(deviceIdentification))
                .orElseThrow(() -> BizException.wrap("RTSP 设备不存在: " + deviceIdentification));

        VideoMediaServerResultDTO mediaServer = resolveMediaServer(device);
        String rtspUrl = buildRtspUrl(device);
        if (StrUtil.isBlank(rtspUrl)) {
            throw BizException.wrap("RTSP 设备未配置有效拉流地址: " + deviceIdentification);
        }

        String effectiveChannel = StrUtil.blankToDefault(channelIdentification, deviceIdentification);
        String streamId = generateStreamId(deviceIdentification, effectiveChannel);

        boolean alreadyReady = Optional.ofNullable(
                zlmMediaServerOpenAnyTenantService.isStreamReady(mediaServer, DEFAULT_APP, streamId)
        ).orElse(false);

        if (alreadyReady) {
            log.info("[RTSP拉流] 流已存在，复用: deviceIdentification={}, streamId={}",
                    deviceIdentification, streamId);
        } else {
            String rtpType = Optional.ofNullable(device.getProtocolConfig())
                    .map(VideoDeviceProtocolConfig::getStreamSource)
                    .map(VideoDeviceProtocolConfig.StreamSource::getRtpType)
                    .filter(StrUtil::isNotBlank)
                    // ZLM rtp_type: 0=TCP, 1=UDP, 2=组播。公网/跨网默认 TCP 最稳。
                    .orElse("0");

            String streamKey = zlmMediaServerOpenAnyTenantService.addStreamProxy(
                    mediaServer, DEFAULT_APP, streamId,
                    rtspUrl, Boolean.TRUE, Boolean.FALSE, rtpType);

            if (StrUtil.isBlank(streamKey)) {
                throw BizException.wrap("向流媒体服务器注册 RTSP 拉流失败，请检查设备可达性与凭据");
            }
            log.info("[RTSP拉流] addStreamProxy 成功: deviceIdentification={}, streamId={}, streamKey={}, rtpType={}",
                    deviceIdentification, streamId, streamKey, rtpType);
        }

        StreamInfo streamInfo = streamInfoService.buildAndCacheStreamInfo(
                mediaServer, deviceIdentification, effectiveChannel,
                InviteSessionTypeEnum.PLAY.getValue(), DEFAULT_APP, streamId, null);
        log.info("[RTSP拉流] StreamInfo 就绪: deviceIdentification={}, channelIdentification={}, streamId={}",
                deviceIdentification, effectiveChannel, streamId);
        return streamInfo;
    }

    /**
     * 停止 RTSP 拉流。删除 ZLM 端代理并清理本地流信息缓存。
     */
    public void stop(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            log.warn("[RTSP拉流] 停止时设备不存在: {}", deviceIdentification);
            return;
        }

        String effectiveChannel = StrUtil.blankToDefault(channelIdentification, deviceIdentification);
        String streamId = generateStreamId(deviceIdentification, effectiveChannel);
        String mediaIdentification = device.getMediaIdentification();

        if (StrUtil.isNotBlank(mediaIdentification)) {
            VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
            if (mediaServer != null) {
                try {
                    zlmMediaServerOpenAnyTenantService.closeStreams(mediaServer, DEFAULT_APP, streamId);
                } catch (Exception e) {
                    log.warn("[RTSP拉流] closeStreams 失败: streamId={}, error={}", streamId, e.getMessage());
                }
                try {
                    zlmMediaServerOpenAnyTenantService.delStreamProxy(mediaServer, streamId);
                } catch (Exception e) {
                    log.warn("[RTSP拉流] delStreamProxy 失败: streamId={}, error={}", streamId, e.getMessage());
                }
            }
        }
        streamInfoService.removeStreamInfo(deviceIdentification, effectiveChannel, InviteSessionTypeEnum.PLAY.getValue());
        log.info("[RTSP拉流] 已停止: deviceIdentification={}, streamId={}", deviceIdentification, streamId);
    }

    /**
     * 获取当前缓存的流信息（不触发新建流）。
     */
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification) {
        String effectiveChannel = StrUtil.blankToDefault(channelIdentification, deviceIdentification);
        return streamInfoService.getStreamInfo(deviceIdentification, effectiveChannel,
                InviteSessionTypeEnum.PLAY.getValue());
    }

    private VideoMediaServerResultDTO resolveMediaServer(VideoDeviceResultVO device) {
        String mediaIdentification = device.getMediaIdentification();
        if (StrUtil.isBlank(mediaIdentification)) {
            throw BizException.wrap("RTSP 设备未绑定流媒体服务器: " + device.getDeviceIdentification());
        }
        VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        if (mediaServer == null) {
            throw BizException.wrap("未找到对应的流媒体服务器: mediaIdentification=" + mediaIdentification);
        }
        return mediaServer;
    }

    /**
     * 从 protocol_config.streamSource 拼装完整 RTSP URL。
     * <p>优先级：{@code url} 整串 > {@code host/port + streamPath}（配合 {@code username + auth_secret}）。
     */
    private String buildRtspUrl(VideoDeviceResultVO device) {
        VideoDeviceProtocolConfig.StreamSource src = Optional.ofNullable(device.getProtocolConfig())
                .map(VideoDeviceProtocolConfig::getStreamSource).orElse(null);

        // 完整 URL 优先
        if (src != null && StrUtil.isNotBlank(src.getUrl())) {
            return src.getUrl().trim();
        }

        String host = device.getHost();
        if (StrUtil.isBlank(host)) {
            return null;
        }
        int port = Optional.ofNullable(device.getPort()).filter(p -> p > 0).orElse(DEFAULT_RTSP_PORT);
        String path = src != null && StrUtil.isNotBlank(src.getStreamPath()) ? src.getStreamPath() : "/";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String username = src != null ? src.getUsername() : null;
        String password = device.getAuthSecret();

        StringBuilder sb = new StringBuilder("rtsp://");
        if (StrUtil.isNotBlank(username)) {
            sb.append(username);
            if (StrUtil.isNotBlank(password)) {
                sb.append(':').append(password);
            }
            sb.append('@');
        }
        sb.append(host).append(':').append(port).append(path);
        return sb.toString();
    }

    /**
     * 生成稳定 streamId：同设备同通道多次点播使用同一 stream，便于 ZLM 复用与清理。
     * <p>MD5 前 16 位足够避免冲突，整体短小便于日志排查。
     */
    private String generateStreamId(String deviceIdentification, String channelIdentification) {
        String raw = deviceIdentification + "_" + channelIdentification;
        return "rtsp_" + SecureUtil.md5(raw).substring(0, 16);
    }
}
