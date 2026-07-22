package com.mqttsnet.thinglinks.video.media.common;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.enumeration.media.MediaServerCapabilityEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;

import java.util.Set;

/**
 * Description:
 * 流媒体节点服务统一抽象接口。
 * 定义所有流媒体服务器必须支持或可选支持的操作，
 * ZLM 和 ABL 各自提供实现类，通过 {@link MediaNodeServiceFactory} 自动选择。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface MediaNodeService {

    // ======================== 能力声明 ========================

    /**
     * 获取当前实现支持的能力集合
     *
     * @return 能力枚举集合
     */
    Set<MediaServerCapabilityEnum> getSupportedCapabilities();

    /**
     * 判断是否支持指定能力
     *
     * @param capability 能力枚举
     * @return true=支持
     */
    default boolean supports(MediaServerCapabilityEnum capability) {
        return getSupportedCapabilities().contains(capability);
    }

    /**
     * 获取流媒体服务器类型标识
     *
     * @return 类型标识（如 "zlm"、"abl"）
     */
    String getServerType();

    // ======================== 服务器管理 ========================

    /**
     * 检查流媒体服务器是否在线
     *
     * @param mediaServer 流媒体服务器信息
     * @return true=在线
     */
    boolean checkServerOnline(VideoMediaServer mediaServer);

    /**
     * 获取服务器配置信息
     *
     * @param mediaServer 流媒体服务器信息
     * @return 配置信息（JSON）
     */
    JSONObject getServerConfig(VideoMediaServer mediaServer);

    /**
     * 实时采集流媒体服务器性能指标（CPU、内存、流数量、网络吞吐）。
     *
     * <p>默认返回空指标，各实现类按自身 API 规范覆盖。
     * 接口不可达或返回异常时静默处理，返回空值指标。</p>
     *
     * @param mediaServer 流媒体服务器信息
     * @return 实时性能指标 VO
     */
    default VideoMediaServerMetricsResultVO getServerMetrics(VideoMediaServer mediaServer) {
        return VideoMediaServerMetricsResultVO.builder().currentStreams(0).networkInSpeed(0L).networkOutSpeed(0L).build();
    }

    /**
     * 重启流媒体服务器
     *
     * @param mediaServer 流媒体服务器信息
     * @return 操作结果
     */
    MediaApiResult restartServer(VideoMediaServer mediaServer);

    // ======================== RTP 服务器管理 ========================

    /**
     * 创建 RTP 接收服务器
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamId    流标识
     * @param ssrc        SSRC 值
     * @param port        指定端口（0=自动分配）
     * @param onlyAuto    是否仅使用自动端口
     * @param reUsePort   是否复用端口
     * @param tcpMode     TCP 模式（0=UDP，1=TCP被动，2=TCP主动）
     * @return 分配的端口号，失败返回 -1
     */
    int createRtpServer(VideoMediaServer mediaServer, String streamId, String ssrc,
                        int port, boolean onlyAuto, boolean reUsePort, int tcpMode);

    /**
     * 关闭 RTP 接收服务器
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamId    流标识
     * @return true=成功
     */
    boolean closeRtpServer(VideoMediaServer mediaServer, String streamId);

    /**
     * 列出所有 RTP 服务器
     *
     * @param mediaServer 流媒体服务器信息
     * @return RTP 服务器列表
     */
    JSONObject listRtpServer(VideoMediaServer mediaServer);

    /**
     * 更新 RTP 服务器的 SSRC
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamId    流标识
     * @param ssrc        新的 SSRC 值
     * @return true=成功
     */
    boolean updateRtpServerSsrc(VideoMediaServer mediaServer, String streamId, String ssrc);

    // ======================== 流操作 ========================

    /**
     * 获取流列表
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称（可为空，查全部）
     * @return 流列表
     */
    JSONObject getMediaList(VideoMediaServer mediaServer, String app, String stream);

    /**
     * 获取流详细信息
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param schema      协议类型
     * @return 流信息
     */
    JSONObject getMediaInfo(VideoMediaServer mediaServer, String app, String stream, String schema);

    /**
     * 关闭指定流
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @return true=成功
     */
    boolean closeStreams(VideoMediaServer mediaServer, String app, String stream);

    /**
     * 判断指定流是否在线
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @return true=在线
     */
    boolean isStreamReady(VideoMediaServer mediaServer, String app, String stream);

    // ======================== RTP 推流 ========================

    /**
     * 主动 RTP 推流（连接远端地址）
     *
     * @param mediaServer 流媒体服务器信息
     * @param params      推流参数
     * @return 操作结果
     */
    MediaApiResult startSendRtp(VideoMediaServer mediaServer, JSONObject params);

    /**
     * 被动 RTP 推流（等待远端连接）
     *
     * @param mediaServer 流媒体服务器信息
     * @param params      推流参数
     * @return 操作结果（含分配的本地端口）
     */
    MediaApiResult startSendRtpPassive(VideoMediaServer mediaServer, JSONObject params);

    /**
     * 停止 RTP 推流
     *
     * @param mediaServer 流媒体服务器信息
     * @param params      停止参数
     * @return true=成功
     */
    boolean stopSendRtp(VideoMediaServer mediaServer, JSONObject params);

    // ======================== 拉流代理 ========================

    /**
     * 添加拉流代理
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param url         源地址
     * @param enableAudio 是否启用音频
     * @param enableMp4   是否启用 MP4 录制
     * @param rtpType     RTP 拉流类型（0=TCP，1=UDP）
     * @return 代理 Key（用于后续停止）
     */
    String addStreamProxy(VideoMediaServer mediaServer, String app, String stream,
                          String url, boolean enableAudio, boolean enableMp4, String rtpType);

    /**
     * 删除拉流代理
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamKey   代理 Key
     * @return true=成功
     */
    boolean delStreamProxy(VideoMediaServer mediaServer, String streamKey);

    // ======================== 截图 ========================

    /**
     * 获取流截图
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param timeoutSec  超时秒数
     * @param targetPath  目标路径
     * @param fileName    文件名
     * @return 截图文件路径
     */
    String getSnapshot(VideoMediaServer mediaServer, String app, String stream,
                       int timeoutSec, String targetPath, String fileName);

    // ======================== 回放控制 ========================

    /**
     * 设置回放速度
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param speed       播放速度（0.25~4.0）
     * @return true=成功
     */
    boolean setPlaybackSpeed(VideoMediaServer mediaServer, String app, String stream, float speed);

    /**
     * 回放拖拽到指定时间点
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param stampSeconds 目标时间戳（秒）
     * @return true=成功
     */
    boolean seekPlayback(VideoMediaServer mediaServer, String app, String stream, long stampSeconds);

    // ======================== RTP 健康检查 ========================

    /**
     * 暂停 RTP 超时检查
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamId    流标识
     * @return true=成功
     */
    boolean pauseRtpCheck(VideoMediaServer mediaServer, String streamId);

    /**
     * 恢复 RTP 超时检查
     *
     * @param mediaServer 流媒体服务器信息
     * @param streamId    流标识
     * @return true=成功
     */
    boolean resumeRtpCheck(VideoMediaServer mediaServer, String streamId);

    // ======================== 录像管理 ========================

    /**
     * 加载 MP4 文件用于点播
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param filePath    文件路径
     * @return true=成功
     */
    boolean loadMp4File(VideoMediaServer mediaServer, String app, String stream, String filePath);

    /**
     * 查询录像文件列表
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 录像文件列表
     */
    JSONObject queryRecordList(VideoMediaServer mediaServer, String app, String stream,
                               String startTime, String endTime);

    // ======================== 构建流地址 ========================

    /**
     * 构建流的多协议播放地址
     *
     * @param mediaServer 流媒体服务器信息
     * @param app         应用名
     * @param stream      流名称
     * @return 包含各协议地址的 JSON 对象
     */
    JSONObject buildStreamUrls(VideoMediaServer mediaServer, String app, String stream);
}
