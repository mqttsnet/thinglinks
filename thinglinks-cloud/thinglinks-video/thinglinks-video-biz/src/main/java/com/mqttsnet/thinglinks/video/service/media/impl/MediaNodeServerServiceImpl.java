package com.mqttsnet.thinglinks.video.service.media.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.constants.ZlmMediaConstants;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaInfo;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaListInfo;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaServerConfig;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaServerStreamInfo;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.media.common.MediaApiResult;
import com.mqttsnet.thinglinks.video.media.zlm.ZlmRestClient;
import com.mqttsnet.thinglinks.video.service.media.MediaNodeServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * -----------------------------------------------------------------------------
 * File Name: MediaNodeServerServiceImpl
 * -----------------------------------------------------------------------------
 * Description:
 * 多媒体节点服务接口实现类
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/6       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/6 16:32
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DS(DsConstant.BASE_TENANT)
public class MediaNodeServerServiceImpl implements MediaNodeServerService {

    private final ZlmRestClient zlmRestClient;

    @Override
    public VideoMediaServerResultDTO checkMediaServerConfig(String ip, Integer port, String secret) {
        VideoMediaServerResultDTO videoMediaServerResultDTO = new VideoMediaServerResultDTO();
        videoMediaServerResultDTO.setHost(ip);
        videoMediaServerResultDTO.setHttpPort(port);
        videoMediaServerResultDTO.setFlvPort(port);
        videoMediaServerResultDTO.setWsFlvPort(port);
        videoMediaServerResultDTO.setSecret(secret);

        MediaApiResult result = zlmRestClient.get(toEntity(videoMediaServerResultDTO), "getServerConfig", null);
        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        JSONArray data = Optional.ofNullable(result.getData())
                .map(d -> d.getJSONArray("data"))
                .filter(d -> !d.isEmpty())
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        ZlmMediaServerConfig zlmServerConfig = Optional.ofNullable(JSON.parseObject(JSON.toJSONString(data.get(0)), ZlmMediaServerConfig.class))
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        videoMediaServerResultDTO.setMediaIdentification(zlmServerConfig.getGeneralMediaServerId());

        Optional.ofNullable(zlmServerConfig.getHttpPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setHttpSslPort);

        Optional.ofNullable(zlmServerConfig.getHttpPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setFlvSslPort);

        Optional.ofNullable(zlmServerConfig.getHttpPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setWsFlvSslPort);

        Optional.ofNullable(zlmServerConfig.getRtmpPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setRtmpPort);

        Optional.ofNullable(zlmServerConfig.getRtmpSslport())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setRtmpSslPort);

        Optional.ofNullable(zlmServerConfig.getRtspPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setRtspPort);

        Optional.ofNullable(zlmServerConfig.getRtspSslport())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setRtspSslPort);

        Optional.ofNullable(zlmServerConfig.getRtpProxyPort())
                .map(this::safeParseInt)
                .ifPresent(videoMediaServerResultDTO::setRtpProxyPort);

        videoMediaServerResultDTO.setStreamHost(ip);
        videoMediaServerResultDTO.setHookHost("127.0.0.1");
        videoMediaServerResultDTO.setSdpHost(ip);
        videoMediaServerResultDTO.setType(VideoMediaServerTypeEnum.ZLM.getValue());

        return videoMediaServerResultDTO;
    }

    @Override
    public List<ZlmMediaServerStreamInfo> getMediaList(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String callId) {
        var params = new HashMap<String, Object>();
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("vhost", "__defaultVhost__");
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "getMediaList", params);

        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        JSONArray data = Optional.ofNullable(result.getData())
                .map(d -> d.getJSONArray("data"))
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        if (data.isEmpty()) {
            throw BizException.wrap("流媒体服务器读取配置失败");
        }

        List<ZlmMediaListInfo> zlmMediaListInfoList = Optional.ofNullable(JSON.parseArray(JSON.toJSONString(data), ZlmMediaListInfo.class))
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        return zlmMediaListInfoList.stream()
                .map(zlmMediaListInfo -> buildZlmMediaServerStreamInfo(videoMediaServerResultDTO, zlmMediaListInfo, callId))
                .toList();
    }

    @Override
    public ZlmMediaServerStreamInfo getMediaInfo(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        var params = Map.<String, Object>of("app", appId, "schema", ZlmMediaConstants.RTSP, "stream", streamIdentification, "vhost", "__defaultVhost__");
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "getMediaInfo", params);

        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        ZlmMediaListInfo zlmMediaListInfo = Optional.ofNullable(JSON.parseObject(JSON.toJSONString(result.getData()), ZlmMediaListInfo.class))
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        return buildZlmMediaServerStreamInfo(videoMediaServerResultDTO, zlmMediaListInfo, StringUtils.EMPTY);
    }

    @Override
    public Boolean closeStreams(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        log.info("Attempting to close stream: appId={}, streamIdentification={}, serverId={}", appId, streamIdentification, videoMediaServerResultDTO.getMediaIdentification());

        var params = new HashMap<String, Object>();
        params.put("vhost", "__defaultVhost__");
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("force", 1);
        params.put("schema", ZlmMediaConstants.RTSP);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "close_streams", params);

        if (result == null) {
            log.error("Failed to close stream: appId={}, streamIdentification={}, serverId={}, reason=Connection to server failed", appId, streamIdentification, videoMediaServerResultDTO.getMediaIdentification());
            throw BizException.wrap("连接失败");
        }

        if (!result.isSuccess()) {
            log.error("Failed to close stream: appId={}, streamIdentification={}, serverId={}, reason={}", appId, streamIdentification, videoMediaServerResultDTO.getMediaIdentification(), JSON.toJSONString(result));
            return false;
        }

        log.info("Successfully closed stream: appId={}, streamIdentification={}, serverId={}", appId, streamIdentification, videoMediaServerResultDTO.getMediaIdentification());

        return true;
    }

    @Override
    public String getFfmpegCmd(VideoMediaServerResultDTO videoMediaServerResultDTO) {
        MediaApiResult result = zlmRestClient.get(toEntity(videoMediaServerResultDTO), "getServerConfig", null);
        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        JSONArray data = Optional.ofNullable(result.getData())
                .map(d -> d.getJSONArray("data"))
                .filter(d -> !d.isEmpty())
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        ZlmMediaServerConfig zlmServerConfig = Optional.ofNullable(JSON.parseObject(JSON.toJSONString(data.get(0)), ZlmMediaServerConfig.class))
                .orElseThrow(() -> BizException.wrap("流媒体服务器读取配置失败"));

        return Optional.ofNullable(zlmServerConfig.getFfmpegCmd())
                .filter(cmd -> !cmd.isEmpty())
                .orElse(ZlmMediaConstants.FFMPEG_CMD);
    }

    @Override
    public Boolean delFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey) {
        var params = Map.<String, Object>of("key", streamKey);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "delFFmpegSource", params);
        return result.isSuccess();
    }

    @Override
    public Boolean delStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey) {
        var params = Map.<String, Object>of("key", streamKey);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "delStreamProxy", params);
        return result.isSuccess();
    }

    @Override
    public String addFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String srcUrl, String dstUrl, Integer timeoutMs, Boolean enableAudio, Boolean enableMp4, String ffmpegCmdKey) {
        var params = new HashMap<String, Object>();
        params.put("src_url", srcUrl);
        params.put("dst_url", dstUrl);
        params.put("timeout_ms", timeoutMs);
        params.put("enable_mp4", enableMp4);
        params.put("ffmpeg_cmd_key", ffmpegCmdKey);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "addFFmpegSource", params);
        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        JSONObject jsonObject = Optional.ofNullable(result.getData())
                .orElseThrow(() -> BizException.wrap("流媒体服务器返回数据为空"));

        return Optional.ofNullable(jsonObject.getString("key"))
                .orElseThrow(() -> BizException.wrap("代理结果中缺少key"));

    }


    @Override
    public String addStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String url, Boolean enableAudio, Boolean enableMp4, String rtpType) {
        var params = new HashMap<String, Object>();
        params.put("vhost", "__defaultVhost__");
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("url", url);
        params.put("enable_mp4", enableMp4 ? 1 : 0);
        params.put("enable_audio", enableAudio ? 1 : 0);
        params.put("rtp_type", rtpType);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "addStreamProxy", params);
        if (result == null || !result.isSuccess()) {
            throw BizException.wrap("流媒体服务器连接失败: {}", Objects.nonNull(result) ? result.getMsg() : "未知错误");
        }

        JSONObject jsonObject = Optional.ofNullable(result.getData())
                .orElseThrow(() -> BizException.wrap("流媒体服务器返回数据为空"));

        return Optional.ofNullable(jsonObject.getString("key"))
                .orElseThrow(() -> BizException.wrap("代理结果中缺少key"));
    }

    /**
     * 将 DTO 转换为实体对象（ZlmRestClient 接收 VideoMediaServer 实体）
     */
    /**
     * 将 DTO 转换为 VideoMediaServer（仅设置 REST 客户端所需字段）
     */
    private VideoMediaServer toEntity(VideoMediaServerResultDTO dto) {
        VideoMediaServer entity = new VideoMediaServer();
        entity.setHost(dto.getHost());
        entity.setHttpPort(dto.getHttpPort());
        entity.setSecret(dto.getSecret());
        return entity;
    }

    private ZlmMediaServerStreamInfo buildZlmMediaServerStreamInfo(VideoMediaServerResultDTO videoMediaServerResultDTO, ZlmMediaListInfo zlmMediaListInfo, String callId) {
        ZlmMediaServerStreamInfo streamInfo = new ZlmMediaServerStreamInfo();
        streamInfo.setAppId(zlmMediaListInfo.getApp());
        streamInfo.setStreamIdentification(zlmMediaListInfo.getStream());
        streamInfo.setStreamIp(videoMediaServerResultDTO.getStreamHost());
        streamInfo.setMediaIdentification(videoMediaServerResultDTO.getMediaIdentification());
        streamInfo.setMediaServer(videoMediaServerResultDTO);
        streamInfo.setCallId(callId);
        streamInfo.setOriginType(zlmMediaListInfo.getOriginType());

        // 构建 ZlmMediaInfo 对象
        ZlmMediaInfo zlmMediaInfo = new ZlmMediaInfo();
        zlmMediaInfo.setApp(zlmMediaListInfo.getApp());
        zlmMediaInfo.setStream(zlmMediaListInfo.getStream());
        zlmMediaInfo.setMediaServer(videoMediaServerResultDTO);
        zlmMediaInfo.setSchema(zlmMediaListInfo.getSchema());
        zlmMediaInfo.setReaderCount(zlmMediaListInfo.getReaderCount());
        zlmMediaInfo.setVideoCodec(zlmMediaListInfo.getTracks().stream()
                .filter(track -> track.getCodecType() != null && track.getCodecType() == 0)
                .findFirst()
                .map(track -> ZlmMediaInfo.getVideoCodec(track.getCodecId()))
                .orElse(null));
        zlmMediaInfo.setWidth(zlmMediaListInfo.getTracks().stream()
                .map(ZlmMediaListInfo.Track::getWidth)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        zlmMediaInfo.setHeight(zlmMediaListInfo.getTracks().stream()
                .map(ZlmMediaListInfo.Track::getHeight)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        zlmMediaInfo.setAudioCodec(zlmMediaListInfo.getTracks().stream()
                .filter(track -> track.getCodecType() != null && track.getCodecType() == 1)
                .findFirst()
                .map(track -> ZlmMediaInfo.getAudioCodec(track.getCodecId()))
                .orElse(null));
        zlmMediaInfo.setAudioChannels(zlmMediaListInfo.getTracks().stream()
                .map(ZlmMediaListInfo.Track::getChannels)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        zlmMediaInfo.setAudioSampleRate(zlmMediaListInfo.getTracks().stream()
                .map(ZlmMediaListInfo.Track::getSampleRate)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        zlmMediaInfo.setDuration(zlmMediaListInfo.getTracks().stream()
                .map(ZlmMediaListInfo.Track::getDuration)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        zlmMediaInfo.setOnline(zlmMediaListInfo.getOnline());
        zlmMediaInfo.setOriginType(zlmMediaListInfo.getOriginType());
        zlmMediaInfo.setAliveSecond(zlmMediaListInfo.getAliveSecond());
        zlmMediaInfo.setBytesSpeed(zlmMediaListInfo.getBytesSpeed());
        zlmMediaInfo.setCallId(callId);
        // 设置流媒体地址
        setStreamUrls(streamInfo, zlmMediaInfo, videoMediaServerResultDTO, callId);

        return streamInfo;
    }

    private void setStreamUrls(ZlmMediaServerStreamInfo streamInfo, ZlmMediaInfo zlmMediaInfo, VideoMediaServerResultDTO videoMediaServerResultDTO, String callId) {
        String addr = videoMediaServerResultDTO.getStreamHost();
        String callIdParam = StrUtil.isBlank(callId) ? "" : "?callId=" + callId;
        String app = zlmMediaInfo.getApp();
        String stream = zlmMediaInfo.getStream();
        streamInfo.setRtmp(addr, videoMediaServerResultDTO.getRtmpPort(), videoMediaServerResultDTO.getRtmpSslPort(), app, stream, callIdParam);
        streamInfo.setRtsp(addr, videoMediaServerResultDTO.getRtspPort(), videoMediaServerResultDTO.getRtspSslPort(), app, stream, callIdParam);

        String flvFile = String.format("%s/%s.live.flv%s", app, stream, callIdParam);
        streamInfo.setFlv(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), flvFile);
        streamInfo.setWsFlv(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), flvFile);

        streamInfo.setFmp4(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), app, stream, callIdParam);
        streamInfo.setHls(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), app, stream, callIdParam);
        streamInfo.setTs(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), app, stream, callIdParam);
        streamInfo.setRtc(addr, videoMediaServerResultDTO.getHttpPort(), videoMediaServerResultDTO.getHttpSslPort(), app, stream, callIdParam, true);
    }


    @Override
    public Boolean startRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String fileFormat, int maxSecond) {
        log.info("开始录制: appId={}, stream={}, format={}, maxSecond={}", appId, streamIdentification, fileFormat, maxSecond);
        var params = new HashMap<String, Object>();
        params.put("vhost", "__defaultVhost__");
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("type", 1); // 1=mp4录制
        params.put("max_second", maxSecond);
        if (StrUtil.isNotBlank(fileFormat)) {
            params.put("customized_path", "");
        }
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "startRecord", params);
        if (result == null || !result.isSuccess()) {
            log.error("开始录制失败: appId={}, stream={}, msg={}", appId, streamIdentification,
                    result != null ? result.getMsg() : "连接失败");
            return false;
        }
        return true;
    }

    @Override
    public Boolean stopRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        log.info("停止录制: appId={}, stream={}", appId, streamIdentification);
        var params = new HashMap<String, Object>();
        params.put("vhost", "__defaultVhost__");
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("type", 1);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "stopRecord", params);
        if (result == null || !result.isSuccess()) {
            log.error("停止录制失败: appId={}, stream={}, msg={}", appId, streamIdentification,
                    result != null ? result.getMsg() : "连接失败");
            return false;
        }
        return true;
    }

    @Override
    public Boolean isRecording(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        var params = new HashMap<String, Object>();
        params.put("vhost", "__defaultVhost__");
        params.put("app", appId);
        params.put("stream", streamIdentification);
        params.put("type", 1);
        MediaApiResult result = zlmRestClient.postForm(toEntity(videoMediaServerResultDTO), "isRecording", params);
        if (result == null || !result.isSuccess()) {
            return false;
        }
        JSONObject data = result.getData();
        return data != null && data.getBooleanValue("status", false);
    }

    private Integer safeParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
