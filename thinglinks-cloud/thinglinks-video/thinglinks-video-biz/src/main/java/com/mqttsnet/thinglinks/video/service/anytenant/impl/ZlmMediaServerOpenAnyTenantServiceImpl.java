package com.mqttsnet.thinglinks.video.service.anytenant.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.ZlmMediaServerStreamInfo;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.media.MediaNodeServerService;
import com.mqttsnet.thinglinks.video.vo.result.media.zlm.ZlmMediaServerStreamInfoResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * -----------------------------------------------------------------------------
 * File Name: ZlmMediaServerOpenAnyTenantServiceImpl
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
 * @date 2024/7/8 00:47
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DS(DsConstant.BASE_TENANT)
public class ZlmMediaServerOpenAnyTenantServiceImpl implements ZlmMediaServerOpenAnyTenantService {

    private final MediaNodeServerService mediaNodeServerService;


    @Override
    public VideoMediaServerResultDTO checkMediaServerConfig(String ip, Integer port, String secret) {
        return mediaNodeServerService.checkMediaServerConfig(ip, port, secret);
    }

    @Override
    public List<ZlmMediaServerStreamInfoResultVO> getMediaServerStreamInfoList(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        // TODO redis中获取
        String callId = "";

        List<ZlmMediaServerStreamInfo> mediaList = mediaNodeServerService.getMediaList(videoMediaServerResultDTO, appId, streamIdentification, callId);

        return mediaList.stream().map(mediaInfo -> {
            ZlmMediaServerStreamInfoResultVO resultVO = new ZlmMediaServerStreamInfoResultVO();
            resultVO.setMediaServer(mediaInfo.getMediaServer());

            Optional.ofNullable(mediaInfo.getFlv()).ifPresent(url -> resultVO.setFlv(url.getUrl()));
            Optional.ofNullable(mediaInfo.getHttpsFlv()).ifPresent(url -> resultVO.setHttpsFlv(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWsFlv()).ifPresent(url -> resultVO.setWsFlv(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWssFlv()).ifPresent(url -> resultVO.setWssFlv(url.getUrl()));
            Optional.ofNullable(mediaInfo.getFmp4()).ifPresent(url -> resultVO.setFmp4(url.getUrl()));
            Optional.ofNullable(mediaInfo.getHttpsFmp4()).ifPresent(url -> resultVO.setHttpsFmp4(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWsFmp4()).ifPresent(url -> resultVO.setWsFmp4(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWssFmp4()).ifPresent(url -> resultVO.setWssFmp4(url.getUrl()));
            Optional.ofNullable(mediaInfo.getHls()).ifPresent(url -> resultVO.setHls(url.getUrl()));
            Optional.ofNullable(mediaInfo.getHttpsHls()).ifPresent(url -> resultVO.setHttpsHls(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWsHls()).ifPresent(url -> resultVO.setWsHls(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWssHls()).ifPresent(url -> resultVO.setWssHls(url.getUrl()));
            Optional.ofNullable(mediaInfo.getTs()).ifPresent(url -> resultVO.setTs(url.getUrl()));
            Optional.ofNullable(mediaInfo.getHttpsTs()).ifPresent(url -> resultVO.setHttpsTs(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWsTs()).ifPresent(url -> resultVO.setWsTs(url.getUrl()));
            Optional.ofNullable(mediaInfo.getWssTs()).ifPresent(url -> resultVO.setWssTs(url.getUrl()));
            Optional.ofNullable(mediaInfo.getRtmp()).ifPresent(url -> resultVO.setRtmp(url.getUrl()));
            Optional.ofNullable(mediaInfo.getRtmps()).ifPresent(url -> resultVO.setRtmps(url.getUrl()));
            Optional.ofNullable(mediaInfo.getRtsp()).ifPresent(url -> resultVO.setRtsp(url.getUrl()));
            Optional.ofNullable(mediaInfo.getRtc()).ifPresent(url -> resultVO.setRtc(url.getUrl()));
            Optional.ofNullable(mediaInfo.getRtcs()).ifPresent(url -> resultVO.setRtcs(url.getUrl()));

            return resultVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean isStreamReady(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        try {
            ZlmMediaServerStreamInfo mediaInfo = mediaNodeServerService.getMediaInfo(videoMediaServerResultDTO, appId, streamIdentification);
            return Objects.nonNull(mediaInfo);
        } catch (Exception e) {
            log.error("An error occurred while checking if the stream is ready", e);
            return false;
        }
    }

    @Override
    public Boolean closeStreams(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        try {
            return mediaNodeServerService.closeStreams(videoMediaServerResultDTO, appId, streamIdentification);
        } catch (Exception e) {
            log.error("An error occurred while closing streams", e);
            return false;
        }
    }

    @Override
    public String getFfmpegCmd(VideoMediaServerResultDTO videoMediaServerResultDTO) {
        try {
            return mediaNodeServerService.getFfmpegCmd(videoMediaServerResultDTO);
        } catch (Exception e) {
            log.error("An error occurred while getting FFmpeg command", e);
            return null;
        }
    }

    @Override
    public Boolean delFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey) {
        try {
            return mediaNodeServerService.delFFmpegSource(videoMediaServerResultDTO, streamKey);
        } catch (Exception e) {
            log.error("An error occurred while deleting FFmpeg source", e);
            return false;
        }
    }

    @Override
    public Boolean delStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String streamKey) {
        try {
            return mediaNodeServerService.delStreamProxy(videoMediaServerResultDTO, streamKey);
        } catch (Exception e) {
            log.error("An error occurred while deleting stream proxy", e);
            return false;
        }
    }

    @Override
    public String addFFmpegSource(VideoMediaServerResultDTO videoMediaServerResultDTO, String srcUrl, String dstUrl, Integer timeoutMs, Boolean enableAudio, Boolean enableMp4, String ffmpegCmdKey) {
        return mediaNodeServerService.addFFmpegSource(videoMediaServerResultDTO, srcUrl, dstUrl, timeoutMs, enableAudio, enableMp4, ffmpegCmdKey);
    }

    @Override
    public String addStreamProxy(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String url, Boolean enableAudio, Boolean enableMp4, String rtpType) {
        return mediaNodeServerService.addStreamProxy(videoMediaServerResultDTO, appId, streamIdentification, url, enableAudio, enableMp4, rtpType);
    }

    @Override
    public Boolean startRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification, String fileFormat, int maxSecond) {
        try {
            return mediaNodeServerService.startRecord(videoMediaServerResultDTO, appId, streamIdentification, fileFormat, maxSecond);
        } catch (Exception e) {
            log.error("开始录制异常: appId={}, stream={}", appId, streamIdentification, e);
            return false;
        }
    }

    @Override
    public Boolean stopRecord(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        try {
            return mediaNodeServerService.stopRecord(videoMediaServerResultDTO, appId, streamIdentification);
        } catch (Exception e) {
            log.error("停止录制异常: appId={}, stream={}", appId, streamIdentification, e);
            return false;
        }
    }

    @Override
    public Boolean isRecording(VideoMediaServerResultDTO videoMediaServerResultDTO, String appId, String streamIdentification) {
        try {
            return mediaNodeServerService.isRecording(videoMediaServerResultDTO, appId, streamIdentification);
        } catch (Exception e) {
            log.error("查询录制状态异常: appId={}, stream={}", appId, streamIdentification, e);
            return false;
        }
    }
}
