package com.mqttsnet.thinglinks.video.service.media.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoStreamProxyTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoStreamProxy;
import com.mqttsnet.thinglinks.video.utils.MediaUrlUtils;
import com.mqttsnet.thinglinks.video.manager.media.VideoStreamProxyManager;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.service.media.VideoStreamProxyService;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerResultVO;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoStreamProxyResultVO;
import com.mqttsnet.thinglinks.video.vo.result.media.zlm.ZlmMediaServerStreamInfoResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoStreamProxySaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoStreamProxyUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * <p>
 * 业务实现类
 * 视频拉流代理信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-05 22:32:06
 * @create [2024-07-05 22:32:06] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoStreamProxyServiceImpl extends SuperServiceImpl<VideoStreamProxyManager, Long, VideoStreamProxy> implements VideoStreamProxyService {


    private final VideoMediaServerService videoMediaServerService;

    private final ZlmMediaServerOpenAnyTenantService zlmMediaServerOpenAnyTenantService;


    @Override
    public VideoStreamProxySaveVO saveStreamProxy(VideoStreamProxySaveVO saveVO) {
        log.info("saveStreamProxy saveVO:{}", saveVO);

        // 校验参数
        checkSaveVO(saveVO);

        // 启用状态需要添加流代理到 ZLM
        VideoStreamProxyResultVO videoStreamProxyResultVO = addStreamProxyToZlm(BeanPlusUtil.toBeanIgnoreError(saveVO, VideoStreamProxyResultVO.class));
        saveVO.setFfmpegCmdKey(videoStreamProxyResultVO.getFfmpegCmdKey());
        saveVO.setStreamKey(videoStreamProxyResultVO.getStreamKey());
        saveVO.setDstUrl(videoStreamProxyResultVO.getDstUrl());


        // 构建实体
        VideoStreamProxy entity = buildSaveVO(saveVO);


        // 保存实体
        superManager.save(entity);

        return BeanPlusUtil.toBeanIgnoreError(entity, VideoStreamProxySaveVO.class);
    }

    @Override
    public VideoStreamProxyUpdateVO updateStreamProxy(VideoStreamProxyUpdateVO updateVO) {
        log.info("updateStreamProxy updateVO:{}", updateVO);

        // 校验参数
        checkUpdateVO(updateVO);

        // 启用状态：先调 ZLM 拿到 streamKey/dstUrl/ffmpegCmdKey 再落库
        // 禁用状态：直接关流，dstUrl/streamKey/ffmpegCmdKey 清空
        VideoStreamProxyResultVO computed = BeanPlusUtil.toBeanIgnoreError(updateVO, VideoStreamProxyResultVO.class);
        if (Boolean.TRUE.equals(updateVO.getStatus())) {
            computed = addStreamProxyToZlm(computed);
        } else {
            removeStreamProxyToZlm(computed);
            computed.setStreamKey(null);
            computed.setDstUrl(null);
            computed.setFfmpegCmdKey(null);
        }

        // 用 ZLM 调用后的最新值（dstUrl/streamKey/ffmpegCmdKey）build entity，前端传的同名字段已忽略
        VideoStreamProxy entity = builderVideoStreamProxyUpdateVO(updateVO, computed)
                .with(VideoStreamProxy::setId, updateVO.getId())
                .build();
        superManager.updateById(entity);
        return updateVO;
    }

    @Override
    public boolean deleteStreamProxy(Long id) {
        VideoStreamProxy videoStreamProxy = superManager.getById(id);
        if (Objects.isNull(videoStreamProxy)) {
            throw BizException.wrap("Stream Proxy ID cannot be null");
        }

        //移除代理流
        try {
            removeStreamProxyToZlm(BeanPlusUtil.toBeanIgnoreError(videoStreamProxy, VideoStreamProxyResultVO.class));
        } catch (BizException e) {
            log.error("deleteStreamProxy removeStreamProxyToZlm error", e);
        }
        return superManager.removeById(id);
    }

    @Override
    public VideoStreamProxyResultVO getStreamProxyDetails(Long id) {
        if (id == null) {
            throw BizException.wrap("Stream Proxy ID cannot be null");
        }

        VideoStreamProxy videoStreamProxy = superManager.getById(id);
        if (videoStreamProxy == null) {
            throw BizException.wrap("Stream Proxy not exist");
        }

        return BeanPlusUtil.toBeanIgnoreError(videoStreamProxy, VideoStreamProxyResultVO.class);
    }

    @Override
    public VideoStreamProxyResultVO getPlayUrl(Long id) {
        VideoStreamProxyResultVO videoStreamProxyResultVO = getStreamProxyDetails(id);

        VideoMediaServerResultDTO videoMediaServerResultDTO = videoMediaServerService.getVideoMediaServerResultDTO(videoStreamProxyResultVO.getMediaIdentification());
        if (videoMediaServerResultDTO == null) {
            log.warn("[getPlayUrl] 未找到流媒体服务器, mediaIdentification={}", videoStreamProxyResultVO.getMediaIdentification());
            videoStreamProxyResultVO.setZlmMediaServerStreamInfoList(Collections.emptyList());
            return videoStreamProxyResultVO;
        }

        try {
            List<ZlmMediaServerStreamInfoResultVO> mediaServerStreamInfoList = zlmMediaServerOpenAnyTenantService.getMediaServerStreamInfoList(videoMediaServerResultDTO, videoStreamProxyResultVO.getAppId(), videoStreamProxyResultVO.getStreamIdentification());
            videoStreamProxyResultVO.setZlmMediaServerStreamInfoList(mediaServerStreamInfoList);
        } catch (Exception e) {
            log.warn("[getPlayUrl] 获取播放地址失败, mediaIdentification={}, error={}", videoStreamProxyResultVO.getMediaIdentification(), e.getMessage());
            videoStreamProxyResultVO.setZlmMediaServerStreamInfoList(Collections.emptyList());
        }

        return videoStreamProxyResultVO;
    }


    /**
     * 用前端 updateVO + 后端计算结果 computed 构造 Entity。
     * <p>关键差异：{@code dstUrl / streamKey / ffmpegCmdKey} 三个字段以 {@code computed}（即 ZLM 调用结果）为准，
     * 前端传的同名值会被忽略——避免用户篡改这些后端语义字段。
     */
    private Builder<VideoStreamProxy> builderVideoStreamProxyUpdateVO(VideoStreamProxyUpdateVO updateVO,
                                                                     VideoStreamProxyResultVO computed) {
        return Builder.of(VideoStreamProxy::new)
                .with(VideoStreamProxy::setAppId, updateVO.getAppId())
                .with(VideoStreamProxy::setProxyType, updateVO.getProxyType())
                .with(VideoStreamProxy::setProxyName, updateVO.getProxyName())
                .with(VideoStreamProxy::setStreamIdentification, updateVO.getStreamIdentification())
                // 用户输入字段：default 模式用 url，ffmpeg 模式用 srcUrl
                .with(VideoStreamProxy::setUrl, updateVO.getUrl())
                .with(VideoStreamProxy::setSrcUrl, updateVO.getSrcUrl())
                // 后端计算字段：以 ZLM 调用结果为准，覆盖前端传值
                .with(VideoStreamProxy::setDstUrl, computed.getDstUrl())
                .with(VideoStreamProxy::setFfmpegCmdKey, computed.getFfmpegCmdKey())
                .with(VideoStreamProxy::setStreamKey, computed.getStreamKey())
                .with(VideoStreamProxy::setTimeoutMs, updateVO.getTimeoutMs())
                .with(VideoStreamProxy::setRtpType, updateVO.getRtpType())
                .with(VideoStreamProxy::setGbIdentification, updateVO.getGbIdentification())
                .with(VideoStreamProxy::setMediaIdentification, updateVO.getMediaIdentification())
                .with(VideoStreamProxy::setEnableAudio, updateVO.getEnableAudio())
                .with(VideoStreamProxy::setEnableMp4, updateVO.getEnableMp4())
                .with(VideoStreamProxy::setStatus, updateVO.getStatus())
                .with(VideoStreamProxy::setEnableRemoveNoneReader, updateVO.getEnableRemoveNoneReader())
                .with(VideoStreamProxy::setEnableDisableNoneReader, updateVO.getEnableDisableNoneReader())
                .with(VideoStreamProxy::setExtendParams, updateVO.getExtendParams())
                .with(VideoStreamProxy::setRemark, updateVO.getRemark())
                .with(VideoStreamProxy::setCreatedOrgId, updateVO.getCreatedOrgId());
    }

    private void checkSaveVO(VideoStreamProxySaveVO saveVO) {
        ArgumentAssert.notBlank(saveVO.getAppId(), "应用ID不能为空");
        ArgumentAssert.notBlank(saveVO.getMediaIdentification(), "媒体唯一标识不能为空");

        if (VideoStreamProxyTypeEnum.fromValue(saveVO.getProxyType()).isEmpty()) {
            throw BizException.wrap("proxyType is not exist");
        }

        VideoMediaServerResultVO videoMediaServerResultVO = videoMediaServerService.getOneByMediaIdentification(saveVO.getMediaIdentification());

        if (Objects.isNull(videoMediaServerResultVO)) {
            throw BizException.wrap("该流媒体服务不存在");
        }


        // 校验相同拉流代理是否存在
        validateUniqueStreamProxy(saveVO.getAppId(), saveVO.getStreamIdentification(), saveVO.getMediaIdentification(), null);


    }

    private void validateUniqueStreamProxy(String appId, String streamIdentification, String mediaIdentification, Long excludeId) {
        LbQueryWrap<VideoStreamProxy> wrap = Wraps.lbQ();
        wrap.eq(VideoStreamProxy::getAppId, appId)
                .eq(VideoStreamProxy::getStreamIdentification, streamIdentification)
                .eq(VideoStreamProxy::getMediaIdentification, mediaIdentification);

        if (excludeId != null) {
            wrap.ne(VideoStreamProxy::getId, excludeId);
        }

        List<VideoStreamProxy> list = list(wrap);

        if (CollUtil.isNotEmpty(list)) {
            throw BizException.wrap("此拉流代理已存在，请勿重复添加");
        }
    }

    private void checkUpdateVO(VideoStreamProxyUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "唯一标识符不能为空");

        VideoStreamProxy videoStreamProxy = superManager.getById(updateVO.getId());

        if (null == videoStreamProxy) {
            throw BizException.wrap("videoStreamProxy is not exist");
        }
        ArgumentAssert.notNull(updateVO.getId(), "唯一标识符不能为空");
        ArgumentAssert.notBlank(updateVO.getAppId(), "应用ID不能为空");

        if (VideoStreamProxyTypeEnum.fromValue(updateVO.getProxyType()).isEmpty()) {
            throw BizException.wrap("proxyType is not exist");
        }

        VideoMediaServerResultVO videoMediaServerResultVO = videoMediaServerService.getOneByMediaIdentification(updateVO.getMediaIdentification());

        if (Objects.isNull(videoMediaServerResultVO)) {
            throw BizException.wrap("该流媒体服务不存在");
        }


        // 校验相同拉流代理是否存在
        validateUniqueStreamProxy(updateVO.getAppId(), updateVO.getStreamIdentification(), updateVO.getMediaIdentification(), updateVO.getId());
    }

    /**
     * 通用saveVO 构建
     *
     * @param saveVO 保存VO
     * @return {@link VideoStreamProxy} 保存DO
     */
    private VideoStreamProxy buildSaveVO(VideoStreamProxySaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, VideoStreamProxy.class);
    }


    /**
     * 添加代理到zlm
     *
     * @param videoStreamProxyResultVO 流代理信息
     * @return {@link VideoStreamProxyResultVO}
     */
    public VideoStreamProxyResultVO addStreamProxyToZlm(VideoStreamProxyResultVO videoStreamProxyResultVO) {
        VideoMediaServerResultDTO videoMediaServerResultDTO = videoMediaServerService
                .getVideoMediaServerResultDTO(videoStreamProxyResultVO.getMediaIdentification());
        if (videoMediaServerResultDTO == null) {
            log.warn("[拉流代理] 未找到流媒体服务器, mediaIdentification={}", videoStreamProxyResultVO.getMediaIdentification());
            return videoStreamProxyResultVO;
        }

        // 校验流是否准备完成
        boolean streamReady = Optional.ofNullable(zlmMediaServerOpenAnyTenantService
                        .isStreamReady(videoMediaServerResultDTO, videoStreamProxyResultVO.getAppId(), videoStreamProxyResultVO.getStreamIdentification()))
                .orElse(false);

        if (streamReady) {
            log.info("[拉流代理] 流已经准备完成，跳过添加操作: appId={}, streamIdentification={}",
                    videoStreamProxyResultVO.getAppId(), videoStreamProxyResultVO.getStreamIdentification());
            return videoStreamProxyResultVO;
        }

        // ============== 字段填充总览 ==============
        // url            ── 用户输入（default 模式）：ZLM addStreamProxy 的源 URL
        // srcUrl         ── 用户输入（ffmpeg 模式）：ffmpeg 输入源
        // dstUrl         ── 后端拼装（**两种模式都计算**）：流在 ZLM 落地后可拉流的 URL
        //                   - default：rtsp://<streamHost>:<rtspPort>/<appId>/<streamIdentification>
        //                   - ffmpeg：按 ffmpeg cmd 解析的 schema 决定（rtsp/rtmp 端口不同）
        // ffmpegCmdKey   ── 后端从 ZLM 配置取（仅 ffmpeg 模式）
        // streamKey      ── ZLM addStreamProxy / addFFmpegSource 的返回值
        // ==========================================
        boolean isFfmpeg = VideoStreamProxyTypeEnum.FFMPEG.getValue()
                .equalsIgnoreCase(videoStreamProxyResultVO.getProxyType());
        String streamPath = videoStreamProxyResultVO.getAppId() + "/" + videoStreamProxyResultVO.getStreamIdentification();

        String dstUrl;
        String streamKey;
        if (isFfmpeg) {
            // === FFmpeg 模式：dstUrl 按 ffmpeg cmd 输出 schema 计算 + 取 ffmpegCmdKey + 调 addFFmpegSource ===
            if (videoStreamProxyResultVO.getTimeoutMs() == null || videoStreamProxyResultVO.getTimeoutMs() == 0) {
                videoStreamProxyResultVO.setTimeoutMs(15);
            }
            String ffmpegCmd = Optional.ofNullable(
                            zlmMediaServerOpenAnyTenantService.getFfmpegCmd(videoMediaServerResultDTO))
                    .filter(StringUtils::isNotBlank)
                    .orElseThrow(() -> BizException.wrap("ffmpeg 拉流代理无法获取 ffmpeg cmd"));
            videoStreamProxyResultVO.setFfmpegCmdKey(ffmpegCmd);

            String schema = Optional.ofNullable(getSchemaFromFFmpegCmd(ffmpegCmd))
                    .filter(StringUtils::isNotBlank)
                    .orElseThrow(() -> BizException.wrap("ffmpeg 拉流代理无法从 ffmpeg cmd 中解析输出格式"));
            int port = "rtsp".equalsIgnoreCase(schema)
                    ? Optional.ofNullable(videoMediaServerResultDTO.getRtspPort()).orElse(0)
                    : Optional.ofNullable(videoMediaServerResultDTO.getRtmpPort()).orElse(0);
            dstUrl = MediaUrlUtils.buildStreamUrl(schema, videoMediaServerResultDTO.getStreamHost(), port, streamPath);

            streamKey = zlmMediaServerOpenAnyTenantService.addFFmpegSource(
                    videoMediaServerResultDTO,
                    videoStreamProxyResultVO.getSrcUrl() != null ? videoStreamProxyResultVO.getSrcUrl().trim() : null,
                    dstUrl,
                    videoStreamProxyResultVO.getTimeoutMs(),
                    videoStreamProxyResultVO.getEnableAudio(),
                    videoStreamProxyResultVO.getEnableMp4(),
                    ffmpegCmd);
        } else {
            // === default 模式：dstUrl 是 ZLM 落地后的 RTSP 拉流地址（流的稳定输出 URL，便于运维使用）===
            videoStreamProxyResultVO.setFfmpegCmdKey(null);
            int rtspPort = Optional.ofNullable(videoMediaServerResultDTO.getRtspPort()).orElse(0);
            dstUrl = MediaUrlUtils.buildStreamUrl("rtsp", videoMediaServerResultDTO.getStreamHost(), rtspPort, streamPath);

            streamKey = zlmMediaServerOpenAnyTenantService.addStreamProxy(
                    videoMediaServerResultDTO,
                    videoStreamProxyResultVO.getAppId(),
                    videoStreamProxyResultVO.getStreamIdentification(),
                    videoStreamProxyResultVO.getUrl() != null ? videoStreamProxyResultVO.getUrl().trim() : null,
                    videoStreamProxyResultVO.getEnableAudio(),
                    videoStreamProxyResultVO.getEnableMp4(),
                    videoStreamProxyResultVO.getRtpType());
        }
        videoStreamProxyResultVO.setDstUrl(dstUrl);

        if (StringUtils.isBlank(streamKey)) {
            throw BizException.wrap("拉流代理添加到 ZLM 失败，请检查源地址与媒体服务器状态");
        }
        videoStreamProxyResultVO.setStreamKey(streamKey);
        log.info("[拉流代理] 添加流成功: proxyType={}, dstUrl={}, streamKey={}",
                videoStreamProxyResultVO.getProxyType(), dstUrl, streamKey);

        return videoStreamProxyResultVO;
    }


    /**
     * 移除代理流
     *
     * @param videoStreamProxyResultVO vo
     * @return {@link Boolean} 是否移除成功
     */
    private Boolean removeStreamProxyToZlm(VideoStreamProxyResultVO videoStreamProxyResultVO) {
        VideoMediaServerResultDTO videoMediaServerResultDTO = videoMediaServerService
                .getVideoMediaServerResultDTO(videoStreamProxyResultVO.getMediaIdentification());
        if (videoMediaServerResultDTO == null) {
            log.warn("[拉流代理] 移除流时未找到流媒体服务器, mediaIdentification={}", videoStreamProxyResultVO.getMediaIdentification());
            return false;
        }

        // 校验流是否准备完成并关闭流
        boolean streamReady = Optional.ofNullable(zlmMediaServerOpenAnyTenantService
                        .isStreamReady(videoMediaServerResultDTO, videoStreamProxyResultVO.getAppId(), videoStreamProxyResultVO.getStreamIdentification()))
                .orElse(false);

        if (streamReady) {
            Boolean closeStreamsFlag = zlmMediaServerOpenAnyTenantService
                    .closeStreams(videoMediaServerResultDTO, videoStreamProxyResultVO.getAppId(), videoStreamProxyResultVO.getStreamIdentification());
            log.info("removeStreamProxyToZlm 正在执行关断单个流，关闭是否成功:{}", closeStreamsFlag);
            if (closeStreamsFlag) {
                return true;
            }
        }

        // 根据代理类型删除流
        return Optional.of(videoStreamProxyResultVO)
                .filter(vo -> VideoStreamProxyTypeEnum.FFMPEG.getValue().equals(vo.getProxyType()))
                .map(vo -> zlmMediaServerOpenAnyTenantService.delFFmpegSource(videoMediaServerResultDTO, videoStreamProxyResultVO.getStreamKey()))
                .orElseGet(() -> zlmMediaServerOpenAnyTenantService.delStreamProxy(videoMediaServerResultDTO, videoStreamProxyResultVO.getStreamKey()));
    }


    private String getSchemaFromFFmpegCmd(String ffmpegCmd) {
        // 去除多余的空格，确保参数之间只有一个空格
        String sanitizedCmd = ffmpegCmd.replaceAll("\\s+", " ");
        String[] paramArray = sanitizedCmd.split(" ");

        // 使用Optional和Stream查找 "-f" 参数的值
        return IntStream.range(0, paramArray.length)
                .filter(i -> "-f".equalsIgnoreCase(paramArray[i]))
                .mapToObj(i -> (i + 1 < paramArray.length) ? paramArray[i + 1] : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(StrPool.EMPTY);
    }


}


