package com.mqttsnet.thinglinks.video.enumeration.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * 流媒体服务器能力枚举，标记每种流媒体服务器支持的 API 能力，
 * 用于运行时判断当前服务器是否支持某项操作。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MediaServerCapabilityEnum", description = "流媒体服务器能力枚举")
public enum MediaServerCapabilityEnum {

    /**
     * 创建 RTP 接收服务器
     */
    RTP_SERVER("rtp_server", "RTP接收服务器"),

    /**
     * 关闭 RTP 服务器
     */
    CLOSE_RTP_SERVER("close_rtp_server", "关闭RTP服务器"),

    /**
     * 更新 SSRC
     */
    SSRC_UPDATE("ssrc_update", "SSRC更新"),

    /**
     * 主动 RTP 推流（TCP 主动连接远端）
     */
    RTP_SEND_ACTIVE("rtp_send_active", "主动RTP推流"),

    /**
     * 被动 RTP 推流（TCP 被动等待连接）
     */
    RTP_SEND_PASSIVE("rtp_send_passive", "被动RTP推流"),

    /**
     * 语音对讲推流
     */
    RTP_SEND_TALK("rtp_send_talk", "语音对讲推流"),

    /**
     * 停止推流
     */
    STOP_SEND_RTP("stop_send_rtp", "停止推流"),

    /**
     * 拉流代理
     */
    STREAM_PROXY("stream_proxy", "拉流代理"),

    /**
     * FFmpeg 代理
     */
    FFMPEG_PROXY("ffmpeg_proxy", "FFmpeg代理"),

    /**
     * 流列表查询
     */
    MEDIA_LIST("media_list", "流列表查询"),

    /**
     * 流详细信息查询
     */
    MEDIA_INFO("media_info", "流详细信息查询"),

    /**
     * 截图
     */
    SNAPSHOT("snapshot", "截图"),

    /**
     * 回放速度控制
     */
    PLAYBACK_SPEED("playback_speed", "回放速度控制"),

    /**
     * 回放拖拽（Seek）
     */
    PLAYBACK_SEEK("playback_seek", "回放拖拽"),

    /**
     * 加载 MP4 文件
     */
    LOAD_MP4("load_mp4", "加载MP4文件"),

    /**
     * RTP 健康检查暂停/恢复
     */
    RTP_CHECK_CONTROL("rtp_check_control", "RTP健康检查控制"),

    /**
     * 服务器配置管理
     */
    SERVER_CONFIG("server_config", "服务器配置管理"),

    /**
     * 会话管理（查询/踢出）
     */
    SESSION_MANAGE("session_manage", "会话管理"),

    /**
     * 重启服务器
     */
    SERVER_RESTART("server_restart", "重启服务器"),

    /**
     * 录像文件查询
     */
    RECORD_QUERY("record_query", "录像文件查询"),

    /**
     * 删除录像目录
     */
    RECORD_DELETE("record_delete", "删除录像目录"),

    /**
     * TCP 主动连接 RTP 服务器
     */
    CONNECT_RTP_SERVER("connect_rtp_server", "TCP主动连接RTP服务器"),

    /**
     * 列出所有 RTP 服务器
     */
    LIST_RTP_SERVER("list_rtp_server", "列出RTP服务器"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<MediaServerCapabilityEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(cap -> cap.getValue().equals(value))
                .findFirst();
    }
}
