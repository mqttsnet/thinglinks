package com.mqttsnet.thinglinks.video.service.anytenant;

import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.vo.result.media.zlm.ZlmMediaServerStreamInfoResultVO;

import java.util.List;

/**
 * -----------------------------------------------------------------------------
 * File Name: ZlmMediaServerOpenAnyTenantService
 * -----------------------------------------------------------------------------
 * Description:
 * ZLM流媒体相关API接口
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
 * @date 2024/7/8 00:45
 */
public interface ZlmMediaServerOpenAnyTenantService {

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
     * @return {@link List<ZlmMediaServerStreamInfoResultVO>}
     */
    List<ZlmMediaServerStreamInfoResultVO> getMediaServerStreamInfoList(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);

    /**
     * 流状态是否准备完成
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @param appId                     应用ID
     * @param streamIdentification      流唯一标识
     * @return {@link Boolean} true: 准备完成、false：未准备完成
     */
    Boolean isStreamReady(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification);

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
     * 获取 Ffmpeg 命令
     *
     * @param videoMediaServerResultDTO 流媒体服务信息
     * @return ffmpeg Cmd
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
     * @param url                       拉流地址
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
     * @param fileFormat                录制格式
     * @param maxSecond                 录制最大时长（秒）
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
