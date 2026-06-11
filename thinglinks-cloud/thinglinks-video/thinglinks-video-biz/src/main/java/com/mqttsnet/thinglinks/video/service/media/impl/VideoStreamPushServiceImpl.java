package com.mqttsnet.thinglinks.video.service.media.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.media.VideoStreamPush;
import com.mqttsnet.thinglinks.video.manager.media.VideoStreamPushManager;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.service.media.VideoStreamPushService;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoStreamPushResultVO;
import com.mqttsnet.thinglinks.video.vo.result.media.zlm.ZlmMediaServerStreamInfoResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoStreamPushSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoStreamPushUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 业务实现类
 * 视频推流信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-07 19:19:57
 * @create [2024-07-07 19:19:57] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoStreamPushServiceImpl extends SuperServiceImpl<VideoStreamPushManager, Long, VideoStreamPush> implements VideoStreamPushService {


    private final VideoMediaServerService videoMediaServerService;

    private final ZlmMediaServerOpenAnyTenantService zlmMediaServerOpenAnyTenantService;

    @Override
    public VideoStreamPushSaveVO saveStreamPush(VideoStreamPushSaveVO saveVO) {
        log.info("saveStreamPush saveVO:{}", saveVO);

        // 校验参数
        checkSaveVO(saveVO);

        // 构建实体
        VideoStreamPush entity = buildSaveVO(saveVO);

        // 保存实体
        superManager.save(entity);

        return BeanPlusUtil.toBeanIgnoreError(entity, VideoStreamPushSaveVO.class);
    }

    @Override
    public VideoStreamPushUpdateVO updateStreamPush(VideoStreamPushUpdateVO updateVO) {
        log.info("updateStreamPush updateVO:{}", updateVO);

        // 校验参数
        checkUpdateVO(updateVO);

        // 构建实体
        VideoStreamPush entity = buildUpdateVO(updateVO);

        // 更新
        superManager.updateById(entity);

        return updateVO;
    }

    @Override
    public boolean deleteStreamPush(Long id) {
        return superManager.removeById(id);
    }

    @Override
    public VideoStreamPushResultVO getStreamPushDetails(Long id) {
        if (id == null) {
            throw BizException.wrap("Stream Push ID cannot be null");
        }

        VideoStreamPush videoStreamPush = superManager.getById(id);
        if (videoStreamPush == null) {
            throw BizException.wrap("Stream Push not exist");
        }

        VideoStreamPushResultVO resultVO =
                BeanPlusUtil.toBeanIgnoreError(videoStreamPush, VideoStreamPushResultVO.class);

        // 拼装推流入口地址，前端用它指引用户配置 OBS / FFmpeg
        fillPushEntryUrls(resultVO);
        return resultVO;
    }

    @Override
    public VideoStreamPushResultVO getPlayUrl(Long id) {
        // getStreamPushDetails 已经拼好 pushUrl/pushUrlRtsp，这里只补播放 URL 列表
        VideoStreamPushResultVO videoStreamPushResultVO = getStreamPushDetails(id);

        VideoMediaServerResultDTO videoMediaServerResultDTO = videoMediaServerService.getVideoMediaServerResultDTO(videoStreamPushResultVO.getMediaIdentification());
        if (videoMediaServerResultDTO == null) {
            log.warn("[getPlayUrl] 未找到流媒体服务器, mediaIdentification={}", videoStreamPushResultVO.getMediaIdentification());
            videoStreamPushResultVO.setZlmMediaServerStreamInfoList(Collections.emptyList());
            return videoStreamPushResultVO;
        }

        try {
            List<ZlmMediaServerStreamInfoResultVO> mediaServerStreamInfoList = zlmMediaServerOpenAnyTenantService.getMediaServerStreamInfoList(videoMediaServerResultDTO, videoStreamPushResultVO.getAppId(), videoStreamPushResultVO.getStreamIdentification());
            videoStreamPushResultVO.setZlmMediaServerStreamInfoList(mediaServerStreamInfoList);
        } catch (Exception e) {
            log.warn("[getPlayUrl] 获取播放地址失败, mediaIdentification={}, error={}", videoStreamPushResultVO.getMediaIdentification(), e.getMessage());
            videoStreamPushResultVO.setZlmMediaServerStreamInfoList(Collections.emptyList());
        }

        return videoStreamPushResultVO;
    }

    /**
     * 拼装推流入口地址。前端拿到后展示给用户："请用此 URL 推流到平台"。
     * <p>RTMP: {@code rtmp://<streamHost>:<rtmpPort>/<appId>/<streamIdentification>}
     * <p>RTSP: {@code rtsp://<streamHost>:<rtspPort>/<appId>/<streamIdentification>}
     * <p>容错：未找到媒体服务器或端口缺省时跳过，留 URL 为 null，前端显示"流媒体服务器未就绪"。
     */
    private void fillPushEntryUrls(VideoStreamPushResultVO resultVO) {
        if (resultVO == null) {
            return;
        }
        String mediaIdentification = resultVO.getMediaIdentification();
        if (mediaIdentification == null || mediaIdentification.isBlank()) {
            return;
        }
        VideoMediaServerResultDTO mediaServer = videoMediaServerService.getVideoMediaServerResultDTO(mediaIdentification);
        if (mediaServer == null) {
            log.warn("[推流入口] 未找到流媒体服务器, mediaIdentification={}", mediaIdentification);
            return;
        }
        String streamHost = mediaServer.getStreamHost();
        if (streamHost == null || streamHost.isBlank()) {
            streamHost = mediaServer.getHost();
        }
        if (streamHost == null || streamHost.isBlank()) {
            return;
        }
        String stream = resultVO.getAppId() + "/" + resultVO.getStreamIdentification();
        if (mediaServer.getRtmpPort() != null && mediaServer.getRtmpPort() > 0) {
            resultVO.setPushUrl("rtmp://" + streamHost + ":" + mediaServer.getRtmpPort() + "/" + stream);
        }
        if (mediaServer.getRtspPort() != null && mediaServer.getRtspPort() > 0) {
            resultVO.setPushUrlRtsp("rtsp://" + streamHost + ":" + mediaServer.getRtspPort() + "/" + stream);
        }
    }

    private void checkSaveVO(VideoStreamPushSaveVO saveVO) {
        ArgumentAssert.notBlank(saveVO.getAppId(), "应用ID不能为空");
        ArgumentAssert.notBlank(saveVO.getMediaIdentification(), "媒体唯一标识不能为空");
    }

    private void checkUpdateVO(VideoStreamPushUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "唯一标识符不能为空");
        ArgumentAssert.notBlank(updateVO.getAppId(), "应用ID不能为空");
        ArgumentAssert.notBlank(updateVO.getMediaIdentification(), "媒体唯一标识不能为空");
    }

    private VideoStreamPush buildSaveVO(VideoStreamPushSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, VideoStreamPush.class);
    }

    private VideoStreamPush buildUpdateVO(VideoStreamPushUpdateVO updateVO) {
        return BeanPlusUtil.toBeanIgnoreError(updateVO, VideoStreamPush.class);
    }

}


