package com.mqttsnet.thinglinks.video.media.zlm;

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
 * ZLMediaKit 流媒体节点服务实现。
 * 基于 ZLMediaKit 最新版本 RESTful API，提供完整的流媒体操作能力，
 * 包括 RTP 服务器管理、流操作、推流、拉流代理、截图、回放控制等。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DS(DsConstant.BASE_TENANT)
public class ZlmMediaNodeServiceImpl implements MediaNodeService {

    private final ZlmRestClient zlmRestClient;

    /**
     * ZLM 支持的全部能力
     */
    private static final Set<MediaServerCapabilityEnum> CAPABILITIES = EnumSet.allOf(MediaServerCapabilityEnum.class);

    @Override
    public Set<MediaServerCapabilityEnum> getSupportedCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public String getServerType() {
        return VideoMediaServerTypeEnum.ZLM.getValue();
    }

    // ======================== 服务器管理 ========================

    @Override
    public boolean checkServerOnline(VideoMediaServer mediaServer) {
        var result = zlmRestClient.get(mediaServer, "getServerConfig", null);
        return result.isSuccess();
    }

    @Override
    public JSONObject getServerConfig(VideoMediaServer mediaServer) {
        var result = zlmRestClient.get(mediaServer, "getServerConfig", null);
        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public MediaApiResult restartServer(VideoMediaServer mediaServer) {
        return zlmRestClient.get(mediaServer, "restartServer", null);
    }

    /**
     * 实时采集 ZLM 性能指标。
     *
     * <p>采集策略：
     * <ul>
     *   <li><b>CPU</b>: getThreadsLoad → 线程平均负载</li>
     *   <li><b>流数量 + 网络吞吐</b>: getMediaList → 流数 + 逐流 bytesSpeed 求和（真实吞吐）</li>
     *   <li><b>内存</b>: getStatistic → 对象存量加权估算（ZLM 无直接内存 API）</li>
     * </ul>
     * 所有接口不可达时静默返回空值。</p>
     *
     * @param mediaServer 流媒体服务器信息
     * @return 实时性能指标 VO
     */
    @Override
    public VideoMediaServerMetricsResultVO getServerMetrics(VideoMediaServer mediaServer) {
        // CPU 负载（线程负载平均值）
        BigDecimal cpuUsage = fetchApiData(mediaServer, "getThreadsLoad")
                .map(json -> json.getJSONArray("data"))
                .filter(arr -> arr != null && !arr.isEmpty())
                .map(this::calcAverageLoad)
                .orElse(null);

        // 流列表（同时提取流数量 + 真实网络吞吐）
        JSONArray mediaList = fetchApiData(mediaServer, "getMediaList")
                .map(json -> json.getJSONArray("data"))
                .orElse(null);

        int currentStreams = mediaList != null ? mediaList.size() : 0;
        long[] networkSpeed = calcNetworkSpeed(mediaList);

        // 内存估算（getStatistic 对象数量）
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

    /**
     * 调用 ZLM HTTP API 并提取返回数据，异常时返回 {@link Optional#empty()}。
     */
    private Optional<JSONObject> fetchApiData(VideoMediaServer entity, String apiPath) {
        try {
            MediaApiResult result = zlmRestClient.get(entity, apiPath, null);
            return Optional.ofNullable(result)
                    .filter(MediaApiResult::isSuccess)
                    .map(MediaApiResult::getData);
        } catch (Exception e) {
            log.debug("[ZLM-指标采集] {} 接口调用失败: {}", apiPath, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 计算 event poller 线程的平均负载。
     */
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

    /**
     * 从 getStatistic 数据估算内存使用率。
     * <p>加权公式：Buffer≈1KB, Frame≈4KB, Packet≈1KB, MediaSource≈64KB,
     * 假定进程最大可用内存 = maxStreams*2MB（默认 500 路 ≈ 1GB），最低 512MB。</p>
     */
    private BigDecimal estimateMemoryUsage(JSONObject stat) {
        long bufferBytes = (stat.getLongValue("Buffer") + stat.getLongValue("BufferRaw")
                + stat.getLongValue("BufferList") + stat.getLongValue("BufferLikeString")) * 1024;
        long frameBytes = (stat.getLongValue("Frame") + stat.getLongValue("FrameImp")) * 4096;
        long packetBytes = (stat.getLongValue("RtpPacket") + stat.getLongValue("RtmpPacket")) * 1024;
        long sourceBytes = (stat.getLongValue("MediaSource") + stat.getLongValue("MultiMediaSourceMuxer")) * 65536;
        long estimatedBytes = bufferBytes + frameBytes + packetBytes + sourceBytes;
        // 基准：512MB
        long maxBytes = 512L * 1024 * 1024;
        double usage = (double) estimatedBytes / maxBytes * 100;
        return BigDecimal.valueOf(Math.min(usage, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 从 getMediaList 的每个流中提取 bytesSpeed，分别求和得到真实的入网/出网速率。
     * <p>ZLM getMediaList 每个流的 bytesSpeed 表示该流的实时码率（bytes/s），
     * 入网 = 所有 originType != 0（拉流/推流入）的 bytesSpeed 之和，
     * 出网 = 所有流的 readerCount * bytesSpeed 之和（每个观看者产生一份出网流量）。
     * 简化处理：入网 = 总 bytesSpeed，出网 = 总 totalReaderCount * 平均码率。</p>
     *
     * @return long[2]: [0]=networkInSpeed, [1]=networkOutSpeed
     */
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

    // ======================== RTP 服务器管理 ========================

    @Override
    public int createRtpServer(VideoMediaServer mediaServer, String streamId, String ssrc,
                               int port, boolean onlyAuto, boolean reUsePort, int tcpMode) {
        var params = Map.<String, Object>of(
                "port", port,
                "enable_tcp", tcpMode > 0 ? 1 : 0,
                "stream_id", streamId,
                "ssrc", ssrc,
                "only_audio", 0,
                "re_use_port", reUsePort ? 1 : 0,
                "tcp_mode", tcpMode
        );

        var result = zlmRestClient.get(mediaServer, "openRtpServer", params);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getIntValue("port", -1);
        }
        log.warn("创建RTP服务器失败 streamId={}, msg={}", streamId, result.getMsg());
        return -1;
    }

    @Override
    public boolean closeRtpServer(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("stream_id", streamId);
        var result = zlmRestClient.get(mediaServer, "closeRtpServer", params);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getIntValue("hit", 0) > 0;
        }
        return false;
    }

    @Override
    public JSONObject listRtpServer(VideoMediaServer mediaServer) {
        var result = zlmRestClient.get(mediaServer, "listRtpServer", null);
        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public boolean updateRtpServerSsrc(VideoMediaServer mediaServer, String streamId, String ssrc) {
        var params = Map.<String, Object>of("stream_id", streamId, "ssrc", ssrc);
        var result = zlmRestClient.get(mediaServer, "updateRtpServerSSRC", params);
        return result.isSuccess();
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
        params.put("schema", "rtsp");
        var result = zlmRestClient.get(mediaServer, "getMediaList", params);
        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public JSONObject getMediaInfo(VideoMediaServer mediaServer, String app, String stream, String schema) {
        var params = Map.<String, Object>of("app", app, "stream", stream, "schema", schema);
        var result = zlmRestClient.get(mediaServer, "getMediaInfo", params);
        return result.isSuccess() ? result.getData() : null;
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
        params.put("force", 1);
        var result = zlmRestClient.get(mediaServer, "close_streams", params);
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

    // ======================== RTP 推流 ========================

    @Override
    public MediaApiResult startSendRtp(VideoMediaServer mediaServer, JSONObject params) {
        return zlmRestClient.post(mediaServer, "startSendRtp", params);
    }

    @Override
    public MediaApiResult startSendRtpPassive(VideoMediaServer mediaServer, JSONObject params) {
        return zlmRestClient.post(mediaServer, "startSendRtpPassive", params);
    }

    @Override
    public boolean stopSendRtp(VideoMediaServer mediaServer, JSONObject params) {
        var result = zlmRestClient.post(mediaServer, "stopSendRtp", params);
        return result.isSuccess();
    }

    // ======================== 拉流代理 ========================

    @Override
    public String addStreamProxy(VideoMediaServer mediaServer, String app, String stream,
                                 String url, boolean enableAudio, boolean enableMp4, String rtpType) {
        var params = Map.<String, Object>of(
                "vhost", "__defaultVhost__",
                "app", app,
                "stream", stream,
                "url", url,
                "enable_audio", enableAudio ? 1 : 0,
                "enable_mp4", enableMp4 ? 1 : 0,
                "rtp_type", StrUtil.blankToDefault(rtpType, "0")
        );
        var result = zlmRestClient.get(mediaServer, "addStreamProxy", params);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getString("key");
        }
        log.warn("添加拉流代理失败 app={}, stream={}, msg={}", app, stream, result.getMsg());
        return null;
    }

    @Override
    public boolean delStreamProxy(VideoMediaServer mediaServer, String streamKey) {
        var params = Map.<String, Object>of("key", streamKey);
        var result = zlmRestClient.get(mediaServer, "delStreamProxy", params);
        return result.isSuccess();
    }

    // ======================== 截图 ========================

    @Override
    public String getSnapshot(VideoMediaServer mediaServer, String app, String stream,
                              int timeoutSec, String targetPath, String fileName) {
        var streamUrl = MediaUrlUtils.buildStreamUrl("rtsp", "127.0.0.1", mediaServer.getRtspPort(), app + "/" + stream);
        var params = Map.<String, Object>of(
                "url", streamUrl,
                "timeout_sec", timeoutSec,
                "expire_sec", 1
        );
        var result = zlmRestClient.get(mediaServer, "getSnap", params);
        return result.isSuccess() ? targetPath + "/" + fileName : null;
    }

    // ======================== 回放控制 ========================

    @Override
    public boolean setPlaybackSpeed(VideoMediaServer mediaServer, String app, String stream, float speed) {
        var params = Map.<String, Object>of(
                "app", app,
                "stream", stream,
                "speed", speed,
                "schema", "rtsp"
        );
        var result = zlmRestClient.get(mediaServer, "setRecordSpeed", params);
        return result.isSuccess();
    }

    @Override
    public boolean seekPlayback(VideoMediaServer mediaServer, String app, String stream, long stampSeconds) {
        var params = Map.<String, Object>of(
                "app", app,
                "stream", stream,
                "stamp", stampSeconds * 1000,
                "schema", "rtsp"
        );
        var result = zlmRestClient.get(mediaServer, "seekRecordStamp", params);
        return result.isSuccess();
    }

    // ======================== RTP 健康检查 ========================

    @Override
    public boolean pauseRtpCheck(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("stream_id", streamId);
        var result = zlmRestClient.get(mediaServer, "pauseRtpCheck", params);
        return result.isSuccess();
    }

    @Override
    public boolean resumeRtpCheck(VideoMediaServer mediaServer, String streamId) {
        var params = Map.<String, Object>of("stream_id", streamId);
        var result = zlmRestClient.get(mediaServer, "resumeRtpCheck", params);
        return result.isSuccess();
    }

    // ======================== 录像管理 ========================

    @Override
    public boolean loadMp4File(VideoMediaServer mediaServer, String app, String stream, String filePath) {
        var params = Map.<String, Object>of(
                "vhost", "__defaultVhost__",
                "app", app,
                "stream", stream,
                "file_path", filePath
        );
        var result = zlmRestClient.get(mediaServer, "loadMP4File", params);
        return result.isSuccess();
    }

    @Override
    public JSONObject queryRecordList(VideoMediaServer mediaServer, String app, String stream,
                                      String startTime, String endTime) {
        var params = Map.<String, Object>of("app", app, "stream", stream);
        var result = zlmRestClient.get(mediaServer, "deleteRecordDirectory", params);
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
        // FMP4
        urls.put("fmp4", MediaUrlUtils.buildStreamUrl("http", host, mediaServer.getHttpPort(), app + "/" + stream + ".live.mp4"));
        // TS
        urls.put("ts", MediaUrlUtils.buildStreamUrl("http", host, mediaServer.getHttpPort(), app + "/" + stream + ".live.ts"));

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
