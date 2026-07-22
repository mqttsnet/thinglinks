package com.mqttsnet.thinglinks.video.gb28181.session;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.manager.stream.StreamInfoManager;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Description:
 * 流信息业务服务。
 * 负责构建多协议流地址、流信息缓存管理。
 * <p>
 * 从流媒体服务器配置信息中提取端口，构建完整的多协议 URL：
 * RTMP/RTSP/HTTP-FLV/WebSocket-FLV/HLS/HTTP-TS/HTTP-FMP4/WebRTC
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class StreamInfoService {

    private final StreamInfoManager streamInfoManager;
    private final VideoMediaServerService videoMediaServerService;

    /**
     * 按 mediaIdentification 重新构建并刷新流信息缓存。
     * <p>统一入口：业务方只要给设备/通道/流类型 + 当前会话的 app/stream/callId，
     * 内部按最新 mediaServer 行（host / 端口）重算所有协议 URL，写回缓存并返回。
     * 用户在 UI 改了 ZLM host / 端口后，下一次播放就能立刻反映最新配置。
     *
     * @param mediaIdentification   流媒体服务器唯一标识（来自 device.mediaIdentification）
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param streamType            流类型（play/playback/download）
     * @param app                   应用名
     * @param stream                流 ID
     * @param callId                SIP Call-ID
     * @return 用最新 mediaServer 配置构建出的 StreamInfo（已写入缓存）
     */
    public StreamInfo buildAndCacheStreamInfoByMediaId(String mediaIdentification,
                                                       String deviceIdentification,
                                                       String channelIdentification,
                                                       String streamType,
                                                       String app,
                                                       String stream,
                                                       String callId) {
        VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        if (mediaServer == null) {
            throw BizException.wrap("流媒体服务器不存在: " + mediaIdentification);
        }
        return buildAndCacheStreamInfo(mediaServer, deviceIdentification, channelIdentification, streamType, app, stream, callId);
    }

    /**
     * 构建流信息并缓存
     *
     * @param mediaServer 流媒体服务器信息
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param streamType  流类型（play/playback/download）
     * @param app         应用名
     * @param stream      流 ID
     * @param callId      SIP Call-ID
     * @return 构建好的流信息
     */
    public StreamInfo buildAndCacheStreamInfo(VideoMediaServerResultDTO mediaServer,
                                              String deviceIdentification, String channelIdentification,
                                              String streamType, String app, String stream,
                                              String callId) {
        StreamInfo streamInfo = buildStreamInfo(mediaServer, deviceIdentification, channelIdentification, app, stream, callId);
        streamInfoManager.put(deviceIdentification, channelIdentification, streamType, streamInfo);
        log.info("流信息构建并缓存: deviceIdentification={}, channelIdentification={}, streamType={}, app={}, stream={}",
                deviceIdentification, channelIdentification, streamType, app, stream);
        return streamInfo;
    }

    /**
     * 构建多协议流信息
     *
     * @param mediaServer 流媒体服务器信息
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param app         应用名
     * @param stream      流 ID
     * @param callId      SIP Call-ID
     * @return 流信息
     */
    public StreamInfo buildStreamInfo(VideoMediaServerResultDTO mediaServer,
                                      String deviceIdentification, String channelIdentification,
                                      String app, String stream, String callId) {
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setApp(app);
        streamInfo.setStream(stream);
        streamInfo.setDeviceIdentification(deviceIdentification);
        streamInfo.setCallId(callId);
        streamInfo.setServerId(mediaServer.getMediaIdentification());

        String streamHost = StrUtil.isNotBlank(mediaServer.getStreamHost())
                ? mediaServer.getStreamHost() : mediaServer.getHost();
        streamInfo.setHost(streamHost);

        String callIdParam = StrUtil.isNotBlank(callId) ? "?callId=" + callId : "";

        int httpPort = Optional.ofNullable(mediaServer.getHttpPort()).orElse(0);
        int httpSslPort = Optional.ofNullable(mediaServer.getHttpSslPort()).orElse(0);
        int rtmpPort = Optional.ofNullable(mediaServer.getRtmpPort()).orElse(0);
        int rtmpSslPort = Optional.ofNullable(mediaServer.getRtmpSslPort()).orElse(0);
        int rtspPort = Optional.ofNullable(mediaServer.getRtspPort()).orElse(0);
        int rtspSslPort = Optional.ofNullable(mediaServer.getRtspSslPort()).orElse(0);
        int flvPort = Optional.ofNullable(mediaServer.getFlvPort()).orElse(httpPort);
        int flvSslPort = Optional.ofNullable(mediaServer.getFlvSslPort()).orElse(httpSslPort);
        int wsFlvPort = Optional.ofNullable(mediaServer.getWsFlvPort()).orElse(httpPort);
        int wsFlvSslPort = Optional.ofNullable(mediaServer.getWsFlvSslPort()).orElse(httpSslPort);

        // RTMP
        streamInfo.setRtmp(streamHost, rtmpPort, rtmpSslPort, app, stream, callIdParam);

        // RTSP
        streamInfo.setRtsp(streamHost, rtspPort, rtspSslPort, app, stream, callIdParam);

        // HTTP-FLV
        String flvFile = String.format("%s/%s.live.flv%s", app, stream, callIdParam);
        streamInfo.setFlv(streamHost, flvPort, flvSslPort, flvFile);

        // WebSocket-FLV
        streamInfo.setWsFlv(streamHost, wsFlvPort, wsFlvSslPort, flvFile);

        // HLS
        streamInfo.setHls(streamHost, httpPort, httpSslPort, app, stream, callIdParam);

        // HTTP-TS
        streamInfo.setTs(streamHost, httpPort, httpSslPort, app, stream, callIdParam);

        // HTTP-FMP4
        streamInfo.setFmp4(streamHost, httpPort, httpSslPort, app, stream, callIdParam);

        // WebRTC
        streamInfo.setRtc(streamHost, httpPort, httpSslPort, app, stream, callIdParam, true);

        return streamInfo;
    }

    /**
     * 获取流信息
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param streamType 流类型
     * @return 流信息
     */
    public Optional<StreamInfo> getStreamInfo(String deviceIdentification, String channelIdentification, String streamType) {
        return streamInfoManager.get(deviceIdentification, channelIdentification, streamType);
    }

    /**
     * 删除流信息
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param streamType 流类型
     */
    public void removeStreamInfo(String deviceIdentification, String channelIdentification, String streamType) {
        streamInfoManager.remove(deviceIdentification, channelIdentification, streamType);
    }

    /**
     * 获取设备的所有流信息
     *
     * @param deviceIdentification 设备国标编号
     * @return 流信息列表
     */
    public List<StreamInfo> getAllStreamInfos(String deviceIdentification) {
        return streamInfoManager.getAll(deviceIdentification);
    }
}
