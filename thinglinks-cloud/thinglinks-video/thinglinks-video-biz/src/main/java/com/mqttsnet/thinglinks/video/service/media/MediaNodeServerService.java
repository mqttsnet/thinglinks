package com.mqttsnet.thinglinks.video.service.media;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaServerStreamInfo;

import java.util.List;

/**
 * -----------------------------------------------------------------------------
 * File Name: IMediaNodeServerService
 * -----------------------------------------------------------------------------
 * Description:
 * 多媒体节点服务接口
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
 * @date 2024/7/6 16:31
 */
public interface MediaNodeServerService {


    /**
     * 验证多媒体服务配置是否正常
     *
     * @param ip     IP
     * @param port   端口
     * @param secret 秘钥
     * @return {@link VideoMediaServerResultDTO} 配置参数
     */
    VideoMediaServerResultDTO checkMediaServerConfig(String ip, Integer port, String secret);


    /**
     * 获取流节点列表信息
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @param callId
     * @return {@link List<ZlmMediaServerStreamInfo>}
     */
    List<ZlmMediaServerStreamInfo> getMediaList(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String callId);


    /**
     * 获取单流节点信息
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @return {@link ZlmMediaServerStreamInfo}
     */
    ZlmMediaServerStreamInfo getMediaInfo(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);

    /**
     * 关闭流
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @return {@link Boolean}
     */
    Boolean closeStreams(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);

    /**
     * 获取 ffmpeg Cmd
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @return
     */
    String getFfmpegCmd(VideoMediaServerResultDTO videoMediaServerResultDTO);

    /**
     * 删除 FFmpeg 源
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param streamKey
     * @return
     */
    Boolean delFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey);


    /**
     * 删除 流代理
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param streamKey
     * @return
     */
    Boolean delStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey);

    /**
     * 添加 FFmpeg 流代理源
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param srcUrl
     * @param dstUrl
     * @param timeoutMs
     * @param enableAudio
     * @param enableMp4
     * @param ffmpegCmdKey
     * @return streamKey
     */
    String addFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String srcUrl, String dstUrl, Integer timeoutMs, Boolean enableAudio, Boolean enableMp4, String ffmpegCmdKey);

    /**
     * 添加流代理
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId
     * @param streamIdentification
     * @param url
     * @param enableAudio
     * @param enableMp4
     * @param rtpType
     * @return streamKey
     */
    String addStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String url, Boolean enableAudio, Boolean enableMp4, String rtpType);

    /**
     * 开始录制
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @param fileFormat                录制格式（mp4/flv/hls等）
     * @param maxSecond                 录制最大时长（秒），0为无限
     * @return 是否成功
     */
    Boolean startRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String fileFormat, int maxSecond);

    /**
     * 停止录制
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @return 是否成功
     */
    Boolean stopRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);

    /**
     * 查询是否正在录制
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @return true=正在录制
     */
    Boolean isRecording(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);
}
