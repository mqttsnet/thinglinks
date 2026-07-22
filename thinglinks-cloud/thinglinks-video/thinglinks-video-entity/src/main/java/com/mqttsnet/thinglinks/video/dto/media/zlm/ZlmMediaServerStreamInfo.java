package com.mqttsnet.thinglinks.video.dto.media.zlm;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * -----------------------------------------------------------------------------
 * File Name: ZlmMediaServerStreamInfo
 * -----------------------------------------------------------------------------
 * Description:
 * ZLM流信息实体
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/7/8       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/7/8 00:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(description = "ZLM流媒体信息表")
public class ZlmMediaServerStreamInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名")
    private String appId;
    @Schema(description = "流标识")
    private String streamIdentification;
    @Schema(description = "设备编号")
    private String deviceIdentification;
    @Schema(description = "通道编号")
    private String channelIdentification;

    @Schema(description = "流IP/域名")
    private String streamIp;


    @Schema(description = "HTTP-FLV流地址")
    private StreamUrl flv;

    @Schema(description = "HTTPS-FLV流地址")
    private StreamUrl httpsFlv;
    @Schema(description = "Websocket-FLV流地址")
    private StreamUrl wsFlv;
    @Schema(description = "Websockets-FLV流地址")
    private StreamUrl wssFlv;
    @Schema(description = "HTTP-FMP4流地址")
    private StreamUrl fmp4;
    @Schema(description = "HTTPS-FMP4流地址")
    private StreamUrl httpsFmp4;
    @Schema(description = "Websocket-FMP4流地址")
    private StreamUrl wsFmp4;
    @Schema(description = "Websockets-FMP4流地址")
    private StreamUrl wssFmp4;
    @Schema(description = "HLS流地址")
    private StreamUrl hls;
    @Schema(description = "HTTPS-HLS流地址")
    private StreamUrl httpsHls;
    @Schema(description = "Websocket-HLS流地址")
    private StreamUrl wsHls;
    @Schema(description = "Websockets-HLS流地址")
    private StreamUrl wssHls;
    @Schema(description = "HTTP-TS流地址")
    private StreamUrl ts;
    @Schema(description = "HTTPS-TS流地址")
    private StreamUrl httpsTs;
    @Schema(description = "Websocket-TS流地址")
    private StreamUrl wsTs;
    @Schema(description = "Websockets-TS流地址")
    private StreamUrl wssTs;
    @Schema(description = "RTMP流地址")
    private StreamUrl rtmp;
    @Schema(description = "RTMPS流地址")
    private StreamUrl rtmps;
    @Schema(description = "RTSP流地址")
    private StreamUrl rtsp;
    @Schema(description = "RTSPS流地址")
    private StreamUrl rtsps;
    @Schema(description = "RTC流地址")
    private StreamUrl rtc;

    @Schema(description = "RTCS流地址")
    private StreamUrl rtcs;

    @Schema(description = "鉴权参数")
    private String callId;

    @Schema(description = "流媒体ID")
    private String mediaIdentification;

    @Schema(description = "ZLM视频信息")
    private ZlmMediaInfo zlmMediaInfo;

    @Schema(description = "流媒体服务器信息")
    private VideoMediaServerResultDTO mediaServer;


    @Schema(description = "文件下载地址（录像下载使用）")
    private ZlmMediaDownloadFileInfo zlmMediaDownloadFileInfo;


    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "进度（录像下载使用）")
    private Double progress;

    @Schema(description = "是否暂停（录像回放使用）")
    private Boolean pause;

    @Schema(description = "产生源类型，包括 unknown = 0,rtmp_push=1,rtsp_push=2,rtp_push=3,pull=4,ffmpeg_pull=5,mp4_vod=6,device_chn=7")
    private Integer originType;

    @Schema(description = "转码后的视频流")
    private ZlmMediaServerStreamInfo transcodeStream;


    public void setRtmp(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam) {
        String file = String.format("%s/%s%s", app, stream, callIdParam);
        if (port > 0) {
            this.rtmp = new StreamUrl("rtmp", host, port, file);
        }
        if (sslPort > 0) {
            this.rtmps = new StreamUrl("rtmps", host, sslPort, file);
        }
    }

    public void setRtsp(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam) {
        String file = String.format("%s/%s%s", app, stream, callIdParam);
        if (port > 0) {
            this.rtsp = new StreamUrl("rtsp", host, port, file);
        }
        if (sslPort > 0) {
            this.rtsps = new StreamUrl("rtsps", host, sslPort, file);
        }
    }

    public void setFlv(String host, Integer port, Integer sslPort, String file) {
        if (port > 0) {
            this.flv = new StreamUrl("http", host, port, file);
        }
        this.wsFlv = new StreamUrl("ws", host, port, file);
        if (sslPort > 0) {
            this.httpsFlv = new StreamUrl("https", host, sslPort, file);
            this.wssFlv = new StreamUrl("wss", host, sslPort, file);
        }
    }

    public void setWsFlv(String host, Integer port, Integer sslPort, String file) {
        if (port > 0) {
            this.wsFlv = new StreamUrl("ws", host, port, file);
        }
        if (sslPort > 0) {
            this.wssFlv = new StreamUrl("wss", host, sslPort, file);
        }
    }

    public void setFmp4(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam) {
        String file = String.format("%s/%s.live.mp4%s", app, stream, callIdParam);
        if (port > 0) {
            this.fmp4 = new StreamUrl("http", host, port, file);
            this.wsFmp4 = new StreamUrl("ws", host, port, file);
        }
        if (sslPort > 0) {
            this.httpsFmp4 = new StreamUrl("https", host, sslPort, file);
            this.wssFmp4 = new StreamUrl("wss", host, sslPort, file);
        }
    }

    public void setHls(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam) {
        String file = String.format("%s/%s/hls.m3u8%s", app, stream, callIdParam);
        if (port > 0) {
            this.hls = new StreamUrl("http", host, port, file);
            this.wsHls = new StreamUrl("ws", host, port, file);
        }
        if (sslPort > 0) {
            this.httpsHls = new StreamUrl("https", host, sslPort, file);
            this.wssHls = new StreamUrl("wss", host, sslPort, file);
        }
    }

    public void setTs(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam) {
        String file = String.format("%s/%s.live.ts%s", app, stream, callIdParam);
        if (port > 0) {
            this.ts = new StreamUrl("http", host, port, file);
            this.wsTs = new StreamUrl("ws", host, port, file);
        }
        if (sslPort > 0) {
            this.httpsTs = new StreamUrl("https", host, sslPort, file);
            this.wssTs = new StreamUrl("wss", host, sslPort, file);
        }
    }

    public void setRtc(String host, Integer port, Integer sslPort, String app, String stream, String callIdParam, boolean isPlay) {
        if (callIdParam != null) {
            callIdParam = callIdParam.replace("?", "&");
        }
        String file = String.format("index/api/webrtc?app=%s&stream=%s&type=%s%s", app, stream, isPlay ? "play" : "push", callIdParam);
        if (port > 0) {
            this.rtc = new StreamUrl("http", host, port, file);
        }
        if (sslPort > 0) {
            this.rtcs = new StreamUrl("https", host, sslPort, file);
        }
    }
}
