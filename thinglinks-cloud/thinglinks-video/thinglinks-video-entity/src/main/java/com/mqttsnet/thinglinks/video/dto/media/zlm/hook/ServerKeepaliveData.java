package com.mqttsnet.thinglinks.video.dto.media.zlm.hook;

import lombok.Data;

/**
 * ZLMediaKit on_server_keepalive 心跳回调数据。
 *
 * <p>ZLM 服务器定期通过 on_server_keepalive Hook 上报当前内部对象统计信息，
 * 用于监控流媒体服务器的运行状态。</p>
 *
 * <p>心跳回调 JSON 结构示例：</p>
 * <pre>{@code
 * {
 *   "mediaServerId": "zlm_001@1",
 *   "data": {
 *     "Buffer": 131,
 *     "BufferLikeString": 2,
 *     "BufferList": 0,
 *     "BufferRaw": 129,
 *     "Frame": 0,
 *     "FrameImp": 0,
 *     "MediaSource": 0,
 *     "MultiMediaSourceMuxer": 0,
 *     "RtmpPacket": 0,
 *     "RtpPacket": 0,
 *     "Socket": 54,
 *     "TcpClient": 1,
 *     "TcpServer": 39,
 *     "TcpSession": 1,
 *     "UdpServer": 16,
 *     "UdpSession": 0
 *   }
 * }
 * }</pre>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-02
 * @see OnServerKeepaliveHookParam
 */
@Data
public class ServerKeepaliveData {

    /**
     * Buffer 对象数量（所有类型缓冲区总数）
     */
    private Integer Buffer;

    /**
     * BufferLikeString 对象数量（类字符串缓冲区）
     */
    private Integer BufferLikeString;

    /**
     * BufferList 对象数量（缓冲区列表）
     */
    private Integer BufferList;

    /**
     * BufferRaw 对象数量（原始缓冲区）
     */
    private Integer BufferRaw;

    /**
     * Frame 对象数量（音视频帧基类）
     */
    private Integer Frame;

    /**
     * FrameImp 对象数量（音视频帧实现）
     */
    private Integer FrameImp;

    /**
     * MediaSource 对象数量（媒体源，即当前活跃的流）
     */
    private Integer MediaSource;

    /**
     * MultiMediaSourceMuxer 对象数量（多协议复用器）
     */
    private Integer MultiMediaSourceMuxer;

    /**
     * RtmpPacket 对象数量（RTMP 数据包）
     */
    private Integer RtmpPacket;

    /**
     * RtpPacket 对象数量（RTP 数据包）
     */
    private Integer RtpPacket;

    /**
     * Socket 对象数量（所有套接字总数）
     */
    private Integer Socket;

    /**
     * TcpClient 对象数量（TCP 客户端连接）
     */
    private Integer TcpClient;

    /**
     * TcpServer 对象数量（TCP 服务监听器）
     */
    private Integer TcpServer;

    /**
     * TcpSession 对象数量（TCP 活跃会话）
     */
    private Integer TcpSession;

    /**
     * UdpServer 对象数量（UDP 服务监听器）
     */
    private Integer UdpServer;

    /**
     * UdpSession 对象数量（UDP 活跃会话）
     */
    private Integer UdpSession;
}
