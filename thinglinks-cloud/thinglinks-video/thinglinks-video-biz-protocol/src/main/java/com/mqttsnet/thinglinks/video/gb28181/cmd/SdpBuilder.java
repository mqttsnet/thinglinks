package com.mqttsnet.thinglinks.video.gb28181.cmd;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.enumeration.stream.InviteSessionTypeEnum;
import com.mqttsnet.thinglinks.video.enumeration.stream.StreamModeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Description:
 * SDP（Session Description Protocol）消息构建器。
 * 根据 GB/T 28181-2016/2022 标准构建 SDP 内容，
 * 支持实时点播、录像回放、文件下载、语音广播/对讲等场景，
 * 支持 UDP/TCP 主动/被动传输模式。
 * <p>
 * SDP 结构遵循 RFC 4566 及 GB/T 28181 扩展（y= SSRC、f= 媒体描述）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class SdpBuilder {

    /**
     * PS 封装格式的 RTP 负载类型
     */
    private static final int PAYLOAD_TYPE_PS = 96;

    /**
     * H.264 裸流的 RTP 负载类型
     */
    private static final int PAYLOAD_TYPE_H264 = 97;

    /**
     * H.265 裸流的 RTP 负载类型
     */
    private static final int PAYLOAD_TYPE_H265 = 98;

    /**
     * MPEG-4 的 RTP 负载类型
     */
    private static final int PAYLOAD_TYPE_MPEG4 = 99;

    /**
     * 构建实时点播 SDP
     *
     * @param sessionName 会话名称（通常为 "Play"）
     * @param mediaIp     流媒体服务器接收 RTP 的 IP
     * @param rtpPort     流媒体服务器接收 RTP 的端口
     * @param ssrc        SSRC 值（10 位十进制字符串）
     * @param streamMode  传输模式（UDP/TCP_ACTIVE/TCP_PASSIVE）
     * @param deviceIdentification    设备国标编号（用于 o= 行的 username）
     * @param channelIdentification   通道国标编号（用于 o= 行备用）
     * @return SDP 字符串
     */
    public String buildPlaySdp(String sessionName, String mediaIp, int rtpPort,
                               String ssrc, StreamModeEnum streamMode,
                               String deviceIdentification, String channelIdentification) {
        return buildSdp(sessionName, mediaIp, rtpPort, ssrc, streamMode,
                deviceIdentification, channelIdentification, null, null, false);
    }

    /**
     * 构建录像回放 SDP
     *
     * @param mediaIp   流媒体服务器接收 RTP 的 IP
     * @param rtpPort   流媒体服务器接收 RTP 的端口
     * @param ssrc      SSRC 值
     * @param streamMode 传输模式
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param startTime 回放开始时间
     * @param endTime   回放结束时间
     * @return SDP 字符串
     */
    public String buildPlaybackSdp(String mediaIp, int rtpPort, String ssrc,
                                   StreamModeEnum streamMode,
                                   String deviceIdentification, String channelIdentification,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, "回放开始时间不能为空");
        Objects.requireNonNull(endTime, "回放结束时间不能为空");
        return buildSdp("Playback", mediaIp, rtpPort, ssrc, streamMode,
                deviceIdentification, channelIdentification, startTime, endTime, false);
    }

    /**
     * 构建录像下载 SDP
     *
     * @param mediaIp    流媒体服务器接收 RTP 的 IP
     * @param rtpPort    流媒体服务器接收 RTP 的端口
     * @param ssrc       SSRC 值
     * @param streamMode 传输模式
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param startTime  下载开始时间
     * @param endTime    下载结束时间
     * @param speed      下载倍速（1/2/4 等）
     * @return SDP 字符串
     */
    public String buildDownloadSdp(String mediaIp, int rtpPort, String ssrc,
                                   StreamModeEnum streamMode,
                                   String deviceIdentification, String channelIdentification,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   int speed) {
        Objects.requireNonNull(startTime, "下载开始时间不能为空");
        Objects.requireNonNull(endTime, "下载结束时间不能为空");
        String sdp = buildSdp("Download", mediaIp, rtpPort, ssrc, streamMode,
                deviceIdentification, channelIdentification, startTime, endTime, false);
        // GB28181 下载 SDP 追加 a=downloadspeed 属性
        return sdp + "a=downloadspeed:" + speed + "\r\n";
    }

    /**
     * 构建语音广播/对讲 SDP
     *
     * @param mediaIp    流媒体服务器 IP
     * @param rtpPort    RTP 端口
     * @param ssrc       SSRC 值
     * @param streamMode 传输模式
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @return SDP 字符串
     */
    public String buildBroadcastSdp(String mediaIp, int rtpPort, String ssrc,
                                    StreamModeEnum streamMode,
                                    String deviceIdentification, String channelIdentification) {
        return buildSdp("Broadcast", mediaIp, rtpPort, ssrc, streamMode,
                deviceIdentification, channelIdentification, null, null, true);
    }

    /**
     * 根据会话类型构建 SDP
     *
     * @param sessionType 会话类型枚举
     * @param mediaIp     流媒体服务器 IP
     * @param rtpPort     RTP 端口
     * @param ssrc        SSRC
     * @param streamMode  传输模式
     * @param deviceIdentification    设备国标编号
     * @param channelIdentification   通道国标编号
     * @param startTime   开始时间（回放/下载时必填）
     * @param endTime     结束时间（回放/下载时必填）
     * @return SDP 字符串
     */
    public String buildBySessionType(InviteSessionTypeEnum sessionType,
                                     String mediaIp, int rtpPort, String ssrc,
                                     StreamModeEnum streamMode,
                                     String deviceIdentification, String channelIdentification,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        return switch (sessionType) {
            case PLAY -> buildPlaySdp("Play", mediaIp, rtpPort, ssrc, streamMode, deviceIdentification, channelIdentification);
            case PLAYBACK -> buildPlaybackSdp(mediaIp, rtpPort, ssrc, streamMode, deviceIdentification, channelIdentification, startTime, endTime);
            case DOWNLOAD -> buildDownloadSdp(mediaIp, rtpPort, ssrc, streamMode, deviceIdentification, channelIdentification, startTime, endTime, 4);
            case BROADCAST, TALK -> buildBroadcastSdp(mediaIp, rtpPort, ssrc, streamMode, deviceIdentification, channelIdentification);
        };
    }

    /**
     * 构建 SDP 内容（核心方法）
     *
     * @param sessionName  会话名称（Play/Playback/Download/Broadcast）
     * @param mediaIp      流媒体服务器 IP
     * @param rtpPort      RTP 端口
     * @param ssrc         SSRC 值
     * @param streamMode   传输模式
     * @param deviceIdentification     设备国标编号
     * @param channelIdentification    通道国标编号
     * @param startTime    开始时间（null 表示实时）
     * @param endTime      结束时间（null 表示实时）
     * @param isAudio      是否为音频流（广播/对讲场景）
     * @return SDP 字符串
     */
    private String buildSdp(String sessionName, String mediaIp, int rtpPort,
                            String ssrc, StreamModeEnum streamMode,
                            String deviceIdentification, String channelIdentification,
                            LocalDateTime startTime, LocalDateTime endTime,
                            boolean isAudio) {
        var sb = new StringBuilder(512);

        // v= 协议版本
        sb.append("v=0\r\n");

        // o= 会话发起者（username sessionId version IN IP4 address）
        String owner = StrUtil.isNotBlank(channelIdentification) ? channelIdentification : deviceIdentification;
        sb.append("o=").append(owner).append(" 0 0 IN IP4 ").append(mediaIp).append("\r\n");

        // s= 会话名称
        sb.append("s=").append(sessionName).append("\r\n");

        // c= 连接信息
        sb.append("c=IN IP4 ").append(mediaIp).append("\r\n");

        // t= 时间描述
        if (startTime != null && endTime != null) {
            long start = startTime.toEpochSecond(ZoneOffset.ofHours(8));
            long end = endTime.toEpochSecond(ZoneOffset.ofHours(8));
            sb.append("t=").append(start).append(" ").append(end).append("\r\n");
        } else {
            sb.append("t=0 0\r\n");
        }

        // m= 媒体描述
        if (isAudio) {
            sb.append(buildAudioMediaLine(rtpPort, streamMode));
        } else {
            sb.append(buildVideoMediaLine(rtpPort, streamMode));
        }

        // a=recvonly / a=sendrecv
        if (isAudio) {
            sb.append("a=sendrecv\r\n");
        } else {
            sb.append("a=recvonly\r\n");
        }

        // a=rtpmap 属性
        if (isAudio) {
            sb.append("a=rtpmap:8 PCMA/8000\r\n");
        } else {
            sb.append("a=rtpmap:").append(PAYLOAD_TYPE_PS).append(" PS/90000\r\n");
            sb.append("a=rtpmap:").append(PAYLOAD_TYPE_H264).append(" H264/90000\r\n");
            sb.append("a=rtpmap:").append(PAYLOAD_TYPE_H265).append(" H265/90000\r\n");
        }

        // TCP 模式的 setup 和 connection 属性
        if (StreamModeEnum.TCP_PASSIVE.equals(streamMode)) {
            sb.append("a=setup:passive\r\n");
            sb.append("a=connection:new\r\n");
        } else if (StreamModeEnum.TCP_ACTIVE.equals(streamMode)) {
            sb.append("a=setup:active\r\n");
            sb.append("a=connection:new\r\n");
        }

        // y= SSRC（GB28181 扩展字段）
        if (StrUtil.isNotBlank(ssrc)) {
            sb.append("y=").append(ssrc).append("\r\n");
        }

        // f= 媒体描述（GB28181 扩展字段，可选）
        // 默认不设置，由设备自适应

        return sb.toString();
    }

    /**
     * 构建视频媒体行（m=video ...）
     *
     * @param rtpPort    RTP 端口
     * @param streamMode 传输模式
     * @return m= 行字符串
     */
    private String buildVideoMediaLine(int rtpPort, StreamModeEnum streamMode) {
        String protocol = getTransportProtocol(streamMode);
        return "m=video " + rtpPort + " " + protocol + " "
                + PAYLOAD_TYPE_PS + " " + PAYLOAD_TYPE_H264 + " " + PAYLOAD_TYPE_H265 + "\r\n";
    }

    /**
     * 构建音频媒体行（m=audio ...）
     *
     * @param rtpPort    RTP 端口
     * @param streamMode 传输模式
     * @return m= 行字符串
     */
    private String buildAudioMediaLine(int rtpPort, StreamModeEnum streamMode) {
        String protocol = getTransportProtocol(streamMode);
        // G.711 A-law (PCMA) payload type = 8
        return "m=audio " + rtpPort + " " + protocol + " 8\r\n";
    }

    /**
     * 根据传输模式获取 SDP 中的 RTP 传输协议描述
     *
     * @param streamMode 传输模式
     * @return RTP 协议描述（RTP/AVP 或 TCP/RTP/AVP）
     */
    private String getTransportProtocol(StreamModeEnum streamMode) {
        if (StreamModeEnum.TCP_PASSIVE.equals(streamMode) || StreamModeEnum.TCP_ACTIVE.equals(streamMode)) {
            return "TCP/RTP/AVP";
        }
        return "RTP/AVP";
    }
}
