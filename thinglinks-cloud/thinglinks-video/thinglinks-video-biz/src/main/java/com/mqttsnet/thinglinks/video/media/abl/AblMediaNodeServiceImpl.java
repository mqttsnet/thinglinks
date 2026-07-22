package com.mqttsnet.thinglinks.video.media.abl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.enumeration.media.MediaServerCapabilityEnum;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.media.common.MediaApiResult;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeService;
import com.mqttsnet.thinglinks.video.utils.MediaUrlUtils;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Description:
 * ABLMediaServer 流媒体节点服务实现。
 * 基于 ABLMediaServer 最新版本 RESTful API，提供流媒体操作能力。
 * 部分 ZLM 独有功能（如 SSRC 更新、主动推流、会话管理等）在 ABL 中不支持，
 * 调用时会抛出 {@link UnsupportedOperationException}。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DS(DsConstant.BASE_TENANT)
public class AblMediaNodeServiceImpl implements MediaNodeService {

    private final AblRestClient ablRestClient;

    /**
     * ABL 支持的能力子集（不支持 SSRC_UPDATE、RTP_SEND_ACTIVE、RTP_SEND_PASSIVE、RTP_SEND_TALK、
     * MEDIA_INFO、LOAD_MP4、SESSION_MANAGE、SERVER_RESTART、RECORD_DELETE、CONNECT_RTP_SERVER、LIST_RTP_SERVER）
     */
    private static final Set<MediaServerCapabilityEnum> CAPABILITIES = EnumSet.of(
        MediaServerCapabilityEnum.RTP_SERVER,
        MediaServerCapabilityEnum.CLOSE_RTP_SERVER,
        MediaServerCapabilityEnum.STOP_SEND_RTP,
        MediaServerCapabilityEnum.STREAM_PROXY,
        MediaServerCapabilityEnum.FFMPEG_PROXY,
        MediaServerCapabilityEnum.MEDIA_LIST,
        MediaServerCapabilityEnum.SNAPSHOT,
        MediaServerCapabilityEnum.PLAYBACK_SPEED,
        MediaServerCapabilityEnum.PLAYBACK_SEEK,
        MediaServerCapabilityEnum.RTP_CHECK_CONTROL,
        MediaServerCapabilityEnum.SERVER_CONFIG,
        MediaServerCapabilityEnum.RECORD_QUERY
    );

    @Override
    public Set<MediaServerCapabilityEnum> getSupportedCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public String getServerType() {
        return VideoMediaServerTypeEnum.ABL.getValue();
    }

    // ======================== 指标采集 ========================

    /**
     * 实时采集 ABL 性能指标。
     *
     * <p>采集策略（尽量兼容 ZLM API）：
     * <ul>
     *   <li><b>CPU</b>: 尝试 getThreadsLoad，不支持则返回 null</li>
     *   <li><b>流数量 + 网络吞吐</b>: getMediaList → 流数 + bytesSpeed 求和</li>
     *   <li><b>内存</b>: 尝试 getStatistic，不支持则返回 null</li>
     * </ul>
     * 所有接口不可达时静默处理。</p>
     *
     * @param mediaServer 流媒体服务器信息
     * @return 实时性能指标 VO
     */
    @Override
    public VideoMediaServerMetricsResultVO getServerMetrics(VideoMediaServer mediaServer) {
        // CPU（ABL 部分版本兼容 getThreadsLoad）
        BigDecimal cpuUsage = fetchApiData(mediaServer, "getThreadsLoad")
                .map(json -> json.getJSONArray("data"))
                .filter(arr -> arr != null && !arr.isEmpty())
                .map(this::calcAverageLoad)
                .orElse(null);

        // 流列表（流数量 + 真实网络吞吐）
        JSONArray mediaList = fetchApiData(mediaServer, "getMediaList")
                .map(json -> json.getJSONArray("data"))
                .orElse(null);

        int currentStreams = mediaList != null ? mediaList.size() : 0;
        long[] networkSpeed = calcNetworkSpeed(mediaList);

        // 内存（ABL 部分版本兼容 getStatistic）
        BigDecimal memoryUsage = fetchApiData(mediaServer, "getStatistic")
                .map(json -> json.getJSONObject("data"))
                .map(this::estimateMemoryUsage)
                .orElse(null);

        return VideoMediaServerMetricsResultVO.builder()
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .currentStreams(currentStreams)
                .networkInSpeed(networkSpeed[0])
                .networkOutSpeed(networkSpeed[1])
                .build();
    }

    private BigDecimal calcAverageLoad(JSONArray threads) {
        double totalLoad = 0;
        int count = 0;
        for (int i = 0; i < threads.size(); i++) {
            JSONObject t = threads.getJSONObject(i);
            if (t != null && t.containsKey("load")) {
                totalLoad += t.getIntValue("load");
                count++;
            }
        }
        return count > 0
                ? BigDecimal.valueOf(totalLoad / count).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private BigDecimal estimateMemoryUsage(JSONObject stat) {
        long bufferBytes = (stat.getLongValue("Buffer") + stat.getLongValue("BufferRaw")
                + stat.getLongValue("BufferList") + stat.getLongValue("BufferLikeString")) * 1024;
        long frameBytes = (stat.getLongValue("Frame") + stat.getLongValue("FrameImp")) * 4096;
        long packetBytes = (stat.getLongValue("RtpPacket") + stat.getLongValue("RtmpPacket")) * 1024;
        long sourceBytes = (stat.getLongValue("MediaSource") + stat.getLongValue("MultiMediaSourceMuxer")) * 65536;
        long estimatedBytes = bufferBytes + frameBytes + packetBytes + sourceBytes;
        long maxBytes = 512L * 1024 * 1024;
        double usage = (double) estimatedBytes / maxBytes * 100;
        return BigDecimal.valueOf(Math.min(usage, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    private long[] calcNetworkSpeed(JSONArray mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return new long[]{0L, 0L};
        }
        long totalBytesSpeed = 0;
        long totalReaderBytes = 0;
        for (int i = 0; i < mediaList.size(); i++) {
            JSONObject stream = mediaList.getJSONObject(i);
            if (stream == null) continue;
            long speed = stream.getLongValue("bytesSpeed");
            int readers = stream.getIntValue("totalReaderCount");
            totalBytesSpeed += speed;
            totalReaderBytes += speed * Math.max(readers, 1);
        }
        return new long[]{totalBytesSpeed, totalReaderBytes};
    }

    /**
     * 调用 ABL HTTP API 并提取返回数据，异常时返回 {@link Optional#empty()}。
     */
    private Optional<JSONObject> fetchApiData(VideoMediaServer entity, String apiPath) {
        try {
            MediaApiResult result = ablRestClient.get(entity, apiPath, null);
            return Optional.ofNullable(result)
                    .filter(MediaApiResult::isSuccess)
                    .map(MediaApiResult::getData);
        } catch (Exception e) {
            log.debug("[ABL-指标采集] {} 接口调用失败: {}", apiPath, e.getMessage());
            return Optional.empty();
        }
    }

    // ======================== 服务器管理 ========================

    @Override
    public boolean checkServerOnline(VideoMediaServer mediaServer) {
        var result = ablRestClient.get(mediaServer, "getServerConfig", null);
        return result.isSuccess();
    }

    @Override
    public JSONObject getServerConfig(VideoMediaServer mediaServer) {
        var result = ablRestClient.get(mediaServer, "getServerConfig", null);
        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public MediaApiResult restartServer(VideoMediaServer mediaServer) {
        throw new UnsupportedOperationException("ABLMediaServer不支持远程重启");
    }

    // ======================== RTP 服务器管理 ========================

    @Override
    public int createRtpServer(VideoMediaServer mediaServer, String streamId, String ssrc,
                               int port, boolean onlyAuto, boolean reUsePort, int tcpMode) {
        var params = Map.<String, Object>of(
            "port", port,
            "enable_tcp", tcpMode > 0 ? 1 : 0,
            "stream_id", streamId,
            "payload", 96,
            "enable_audio", 1
        );

        var result = ablRestClient.get(mediaServer, "openRtpServer", params);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getIntValue("port", -1);
        }
        log.warn("ABL创建RTP服务器失败 streamId={}, msg={}", streamId, result.getMsg());
        return -1;
    }

    @Override
    public boolean closeRtpServer(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("app", "rtp", "stream", streamId);
        var result = ablRestClient.get(mediaServer, "closeStreams", params);
        return result.isSuccess();
    }

    @Override
    public JSONObject listRtpServer(VideoMediaServer mediaServer) {
        throw new UnsupportedOperationException("ABLMediaServer不支持列出RTP服务器");
    }

    @Override
    public boolean updateRtpServerSsrc(VideoMediaServer mediaServer, String streamId, String ssrc) {
        log.warn("ABLMediaServer不支持SSRC更新，streamId={}, ssrc={}", streamId, ssrc);
        return false;
    }

    // ======================== 流操作 ========================

    @Override
    public JSONObject getMediaList(VideoMediaServer mediaServer, String app, String stream) {
        var params = new HashMap<String, Object>();
        if (StrUtil.isNotBlank(app)) {
            params.put("app", app);
        }
        if (StrUtil.isNotBlank(stream)) {
            params.put("stream", stream);
        }
        var result = ablRestClient.get(mediaServer, "getMediaList", params);
        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public JSONObject getMediaInfo(VideoMediaServer mediaServer, String app, String stream, String schema) {
        throw new UnsupportedOperationException("ABLMediaServer不支持流详细信息查询");
    }

    @Override
    public boolean closeStreams(VideoMediaServer mediaServer, String app, String stream) {
        var params = new HashMap<String, Object>();
        if (StrUtil.isNotBlank(app)) {
            params.put("app", app);
        }
        if (StrUtil.isNotBlank(stream)) {
            params.put("stream", stream);
        }
        var result = ablRestClient.get(mediaServer, "closeStreams", params);
        return result.isSuccess();
    }

    @Override
    public boolean isStreamReady(VideoMediaServer mediaServer, String app, String stream) {
        var result = getMediaList(mediaServer, app, stream);
        if (result == null) {
            return false;
        }
        var data = result.getJSONArray("data");
        return data != null && !data.isEmpty();
    }

    // ======================== RTP 推流（ABL 不支持） ========================

    @Override
    public MediaApiResult startSendRtp(VideoMediaServer mediaServer, JSONObject params) {
        throw new UnsupportedOperationException("ABLMediaServer不支持主动RTP推流");
    }

    @Override
    public MediaApiResult startSendRtpPassive(VideoMediaServer mediaServer, JSONObject params) {
        throw new UnsupportedOperationException("ABLMediaServer不支持被动RTP推流");
    }

    @Override
    public boolean stopSendRtp(VideoMediaServer mediaServer, JSONObject params) {
        var result = ablRestClient.post(mediaServer, "stopSendRtp", params);
        return result.isSuccess();
    }

    // ======================== 拉流代理 ========================

    @Override
    public String addStreamProxy(VideoMediaServer mediaServer, String app, String stream,
                                 String url, boolean enableAudio, boolean enableMp4, String rtpType) {
        var params = Map.<String, Object>of(
            "app", app,
            "stream", stream,
            "url", url,
            "disableAudio", enableAudio ? 0 : 1,
            "enableMp4", enableMp4 ? 1 : 0,
            "rtpType", StrUtil.blankToDefault(rtpType, "0")
        );
        var result = ablRestClient.get(mediaServer, "addStreamProxy", params);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getString("key");
        }
        log.warn("ABL添加拉流代理失败 app={}, stream={}, msg={}", app, stream, result.getMsg());
        return null;
    }

    @Override
    public boolean delStreamProxy(VideoMediaServer mediaServer, String streamKey) {
        var params = Map.<String, Object>of("key", streamKey);
        var result = ablRestClient.get(mediaServer, "delStreamProxy", params);
        return result.isSuccess();
    }

    // ======================== 截图 ========================

    @Override
    public String getSnapshot(VideoMediaServer mediaServer, String app, String stream,
                              int timeoutSec, String targetPath, String fileName) {
        var params = Map.<String, Object>of(
            "app", app,
            "stream", stream,
            "timeout_sec", timeoutSec,
            "path", targetPath,
            "fileName", fileName
        );
        var result = ablRestClient.get(mediaServer, "getSnap", params);
        return result.isSuccess() ? targetPath + "/" + fileName : null;
    }

    // ======================== 回放控制 ========================

    @Override
    public boolean setPlaybackSpeed(VideoMediaServer mediaServer, String app, String stream, float speed) {
        var params = Map.<String, Object>of(
            "app", app,
            "stream", stream,
            "command", "scale",
            "value", String.valueOf(speed)
        );
        var result = ablRestClient.get(mediaServer, "controlRecordPlay", params);
        return result.isSuccess();
    }

    @Override
    public boolean seekPlayback(VideoMediaServer mediaServer, String app, String stream, long stampSeconds) {
        var params = Map.<String, Object>of(
            "app", app,
            "stream", stream,
            "command", "seek",
            "value", String.valueOf(stampSeconds)
        );
        var result = ablRestClient.get(mediaServer, "controlRecordPlay", params);
        return result.isSuccess();
    }

    // ======================== RTP 健康检查 ========================

    @Override
    public boolean pauseRtpCheck(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("key", streamId);
        var result = ablRestClient.get(mediaServer, "pauseRtpServer", params);
        return result.isSuccess();
    }

    @Override
    public boolean resumeRtpCheck(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("key", streamId);
        var result = ablRestClient.get(mediaServer, "resumeRtpServer", params);
        return result.isSuccess();
    }

    // ======================== 录像管理 ========================

    @Override
    public boolean loadMp4File(VideoMediaServer mediaServer, String app, String stream, String filePath) {
        throw new UnsupportedOperationException("ABLMediaServer不支持加载MP4文件");
    }

    @Override
    public JSONObject queryRecordList(VideoMediaServer mediaServer, String app, String stream,
                                      String startTime, String endTime) {
        var params = Map.<String, Object>of(
            "app", app,
            "stream", stream,
            "startTime", startTime,
            "endTime", endTime
        );
        var result = ablRestClient.get(mediaServer, "queryRecordList", params);
        return result.isSuccess() ? result.getData() : null;
    }

    // ======================== 构建流地址 ========================

    @Override
    public JSONObject buildStreamUrls(VideoMediaServer mediaServer, String app, String stream) {
        var urls = new JSONObject();
        var host = StrUtil.blankToDefault(mediaServer.getStreamHost(), mediaServer.getHost());
        String flvPath = app + "/" + stream + ".live.flv";
        String hlsPath = app + "/" + stream + "/hls.m3u8";
        String streamPath = app + "/" + stream;

        // RTSP
        urls.put("rtsp", MediaUrlUtils.buildStreamUrl("rtsp", host, mediaServer.getRtspPort(), streamPath));
        // RTMP
        urls.put("rtmp", MediaUrlUtils.buildStreamUrl("rtmp", host, mediaServer.getRtmpPort(), streamPath));
        // HTTP-FLV
        urls.put("flv", MediaUrlUtils.buildStreamUrl("http", host, mediaServer.getHttpPort(), flvPath));
        // WS-FLV
        urls.put("ws_flv", MediaUrlUtils.buildStreamUrl("ws", host, mediaServer.getHttpPort(), flvPath));
        // HLS
        urls.put("hls", MediaUrlUtils.buildStreamUrl("http", host, mediaServer.getHttpPort(), hlsPath));

        // SSL 变体
        if (mediaServer.getHttpSslPort() != null && mediaServer.getHttpSslPort() > 0) {
            urls.put("flv_https", MediaUrlUtils.buildStreamUrl("https", host, mediaServer.getHttpSslPort(), flvPath));
            urls.put("hls_https", MediaUrlUtils.buildStreamUrl("https", host, mediaServer.getHttpSslPort(), hlsPath));
            urls.put("wss_flv", MediaUrlUtils.buildStreamUrl("wss", host, mediaServer.getHttpSslPort(), flvPath));
        }
        if (mediaServer.getRtspSslPort() != null && mediaServer.getRtspSslPort() > 0) {
            urls.put("rtsps", MediaUrlUtils.buildStreamUrl("rtsps", host, mediaServer.getRtspSslPort(), streamPath));
        }

        return urls;
    }
}
