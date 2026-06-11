package com.mqttsnet.thinglinks.video.service.media.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.enumeration.media.VideoMediaServerTypeEnum;
import com.mqttsnet.thinglinks.video.entity.media.VideoMediaServer;
import com.mqttsnet.thinglinks.video.manager.media.VideoMediaServerManager;
import com.mqttsnet.thinglinks.video.media.common.MediaNodeServiceFactory;
import com.mqttsnet.thinglinks.video.service.anytenant.ZlmMediaServerOpenAnyTenantService;
import com.mqttsnet.thinglinks.video.service.media.VideoMediaServerService;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerMetricsResultVO;
import com.mqttsnet.thinglinks.video.vo.query.media.VideoMediaServerPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.media.VideoMediaServerResultVO;
import com.mqttsnet.thinglinks.video.vo.save.media.VideoMediaServerSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.media.VideoMediaServerUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * 流媒体服务器信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-07-03 17:56:38
 * @create [2024-07-03 17:56:38] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoMediaServerServiceImpl extends SuperServiceImpl<VideoMediaServerManager, Long, VideoMediaServer> implements VideoMediaServerService {


    private final ZlmMediaServerOpenAnyTenantService zlmMediaServerOpenAnyTenantService;
    private final MediaNodeServiceFactory mediaNodeServiceFactory;

    /**
     * 保存流媒体服务器信息
     *
     * @param saveVO 保存参数
     * @return {@link VideoMediaServerSaveVO} 实体
     */
    @Override
    public VideoMediaServerSaveVO saveMediaServer(VideoMediaServerSaveVO saveVO) {
        log.info("saveMediaServer saveVO:{}", saveVO);

        // 校验参数准确性
        checkSaveVO(saveVO);

        // 校验连接是否有效并更新 saveVO
        VideoMediaServerResultDTO checkMediaServerConfig = validateAndUpdateMediaServerConfig(saveVO.getHost(), saveVO.getHttpPort(), saveVO.getSecret());
        // 合并 saveVO 和 checkMediaServerConfig
        saveVO = mergeSaveVOAndConfig(saveVO, checkMediaServerConfig);

        // 构建实体
        VideoMediaServer entity = buildSaveVO(saveVO);

        // 保存实体
        superManager.save(entity);

        return BeanPlusUtil.toBeanIgnoreError(entity, VideoMediaServerSaveVO.class);
    }

    /**
     * 更新流媒体服务器信息
     *
     * @param updateVO 更新参数
     * @return {@link VideoMediaServerUpdateVO} 实体
     */
    @Override
    public VideoMediaServerUpdateVO updateMediaServer(VideoMediaServerUpdateVO updateVO) {
        log.info("updateMediaServer updateVO:{}", updateVO);

        // 校验参数
        checkUpdateVO(updateVO);

        // 校验连接是否有效并更新 updateVO
        VideoMediaServerResultDTO checkMediaServerConfig = validateAndUpdateMediaServerConfig(updateVO.getHost(), updateVO.getHttpPort(), updateVO.getSecret());
        // 合并 updateVO 和 checkMediaServerConfig
        updateVO = mergeUpdateVOAndConfig(updateVO, checkMediaServerConfig);

        // 更新
        superManager.updateById(builderVideoMediaServerUpdateVO(updateVO));
        return updateVO;
    }

    /**
     * 校验并更新流媒体服务器配置
     *
     * @param ip       服务器IP
     * @param httpPort HTTP端口
     * @param secret   密钥
     * @return {@link VideoMediaServerResultDTO}
     */
    private VideoMediaServerResultDTO validateAndUpdateMediaServerConfig(String ip, Integer httpPort, String secret) {
        VideoMediaServerResultDTO checkMediaServerConfig = zlmMediaServerOpenAnyTenantService.checkMediaServerConfig(ip, httpPort, secret);

        if (Objects.isNull(checkMediaServerConfig)) {
            throw BizException.wrap("多媒体服务器认证失败，请检查通信是否正常!");
        }
        // 校验流媒体服务标识合法性
        validateMediaIdentifier(checkMediaServerConfig.getMediaIdentification());

        return checkMediaServerConfig;
    }

    @Override
    public Boolean deleteMediaServer(Long id) {
        return superManager.removeById(id);
    }

    @Override
    public VideoMediaServerResultVO getMediaServerDetails(Long id) {
        ArgumentAssert.notNull(id, "Media Server ID cannot be null");
        VideoMediaServer videoMediaServer = superManager.getById(id);
        ArgumentAssert.notNull(videoMediaServer, "Media Server cannot be null");
        return BeanPlusUtil.toBeanIgnoreError(videoMediaServer, VideoMediaServerResultVO.class);
    }

    @Override
    public VideoMediaServerResultVO getOneByMediaIdentification(String mediaIdentification) {
        return BeanPlusUtil.toBeanIgnoreError(superManager.getOneByMediaIdentification(mediaIdentification), VideoMediaServerResultVO.class);
    }

    @Override
    public VideoMediaServerResultDTO getVideoMediaServerResultDTO(String mediaIdentification) {
        return BeanPlusUtil.toBeanIgnoreError(superManager.getOneByMediaIdentification(mediaIdentification), VideoMediaServerResultDTO.class);
    }

    @Override
    public List<VideoMediaServerResultDTO> getVideoMediaServerResultDTOList(VideoMediaServerPageQuery query) {
        List<VideoMediaServer> mediaServerList = superManager.getVideoMediaServerList(query);
        return BeanPlusUtil.toBeanList(mediaServerList, VideoMediaServerResultDTO.class);
    }

    @Override
    public void serverOnline(VideoMediaServerResultDTO mediaServer) {
        validateMediaServer(mediaServer);
        updateServerStatus(mediaServer.getMediaIdentification(),
                mediaServer.getOnlineStatus(),
                mediaServer.getHookAliveInterval());

    }

    @Override
    public void serverOffline(VideoMediaServerResultDTO mediaServer) {
        validateMediaServer(mediaServer);
        updateServerStatus(mediaServer.getMediaIdentification(),
                mediaServer.getOnlineStatus(),
                null);
    }

    private void validateMediaServer(VideoMediaServerResultDTO mediaServer) {
        ArgumentAssert.notNull(mediaServer, "媒体服务器参数不能为空");
        ArgumentAssert.notBlank(mediaServer.getMediaIdentification(), "媒体标识不能为空");
    }


    /**
     * 更新媒体服务器状态
     *
     * @param mediaIdentification 媒体标识
     * @param onlineStatus        在线状态
     * @param hookInterval        心跳间隔
     */
    private void updateServerStatus(String mediaIdentification, Boolean onlineStatus, Integer hookInterval) {
        VideoMediaServer existServer = superManager.getOneByMediaIdentification(mediaIdentification);
        ArgumentAssert.notNull(existServer, "媒体服务器不存在");

        if (onlineStatus.equals(existServer.getOnlineStatus())) {
            log.info("{}:媒体服务器状态未发生变化，无需更新", mediaIdentification);
            return;
        }

        UpdateWrapper<VideoMediaServer> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(VideoMediaServer::getMediaIdentification, mediaIdentification)
                .set(VideoMediaServer::getOnlineStatus, onlineStatus)
                .set(hookInterval != null, VideoMediaServer::getHookAliveInterval, hookInterval);

        if (!superManager.update(updateWrapper)) {
            throw BizException.wrap("媒体服务器状态更新失败");
        }
    }


    @Override
    public void updateServerMetrics(String mediaIdentification,
                                    BigDecimal cpuUsage,
                                    BigDecimal memoryUsage,
                                    Integer currentStreams,
                                    Long networkInSpeed,
                                    Long networkOutSpeed) {
        VideoMediaServer existServer = superManager.getOneByMediaIdentification(mediaIdentification);
        if (existServer == null) {
            log.warn("[更新指标] 媒体服务器不存在: {}", mediaIdentification);
            return;
        }

        UpdateWrapper<VideoMediaServer> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(VideoMediaServer::getMediaIdentification, mediaIdentification)
                .set(VideoMediaServer::getCpuUsage, cpuUsage)
                .set(VideoMediaServer::getMemoryUsage, memoryUsage)
                .set(VideoMediaServer::getCurrentStreams, currentStreams)
                .set(VideoMediaServer::getNetworkInSpeed, networkInSpeed)
                .set(VideoMediaServer::getNetworkOutSpeed, networkOutSpeed)
                .set(VideoMediaServer::getLastAliveTime, LocalDateTime.now());
        superManager.update(updateWrapper);
        log.debug("[更新指标] mediaId={}, cpu={}%, mem={}%, streams={}, in={}, out={}",
                mediaIdentification, cpuUsage, memoryUsage, currentStreams, networkInSpeed, networkOutSpeed);
    }

    @Override
    public VideoMediaServerMetricsResultVO getRealTimeMetrics(Long id) {
        VideoMediaServer server = superManager.getById(id);
        if (server == null) {
            return VideoMediaServerMetricsResultVO.builder()
                    .currentStreams(0).networkInSpeed(0L).networkOutSpeed(0L).build();
        }
        return mediaNodeServiceFactory.getService(server).getServerMetrics(server);
    }

    @Override
    public boolean testConnection(String host, Integer httpPort, String secret) {
        try {
            zlmMediaServerOpenAnyTenantService.checkMediaServerConfig(host, httpPort, secret);
            return true;
        } catch (Exception e) {
            log.warn("[测试连接] 连接失败: host={}, httpPort={}, error={}", host, httpPort, e.getMessage());
            return false;
        }
    }

    /**
     * 构建更新参数
     *
     * @param updateVO 更新参数
     * @return {@link VideoMediaServer} 实体
     */
    private VideoMediaServer builderVideoMediaServerUpdateVO(VideoMediaServerUpdateVO updateVO) {
        return BeanPlusUtil.toBeanIgnoreError(updateVO, VideoMediaServer.class);
    }


    /**
     * 合并 saveVO 和 checkMediaServerConfig
     *
     * @param saveVO                 原始保存参数
     * @param checkMediaServerConfig 校验返回的配置
     * @return 合并后的保存参数
     */
    private VideoMediaServerSaveVO mergeSaveVOAndConfig(VideoMediaServerSaveVO saveVO, VideoMediaServerResultDTO checkMediaServerConfig) {
        VideoMediaServerSaveVO mergedVO = BeanPlusUtil.toBeanIgnoreError(checkMediaServerConfig, VideoMediaServerSaveVO.class);

        Optional.ofNullable(saveVO.getAppId()).ifPresent(mergedVO::setAppId);
        Optional.ofNullable(saveVO.getHost()).ifPresent(mergedVO::setHost);
        Optional.ofNullable(saveVO.getHookHost()).ifPresent(mergedVO::setHookHost);
        Optional.ofNullable(saveVO.getHttpPort()).ifPresent(mergedVO::setHttpPort);
        Optional.ofNullable(saveVO.getSecret()).ifPresent(mergedVO::setSecret);
        Optional.ofNullable(saveVO.getType()).ifPresent(mergedVO::setType);
        Optional.ofNullable(saveVO.getRemark()).ifPresent(mergedVO::setRemark);
        Optional.ofNullable(saveVO.getExtendParams()).ifPresent(mergedVO::setExtendParams);
        Optional.ofNullable(saveVO.getName()).ifPresent(mergedVO::setName);

        return mergedVO;
    }


    /**
     * 合并 updateVO 和 checkMediaServerConfig
     *
     * @param updateVO               原始修改参数
     * @param checkMediaServerConfig 校验返回的配置
     * @return 合并后的保存参数
     */
    private VideoMediaServerUpdateVO mergeUpdateVOAndConfig(VideoMediaServerUpdateVO updateVO, VideoMediaServerResultDTO checkMediaServerConfig) {
        VideoMediaServerUpdateVO mergedUpdateVO = BeanPlusUtil.toBeanIgnoreError(checkMediaServerConfig, VideoMediaServerUpdateVO.class);

        mergedUpdateVO.setId(updateVO.getId());
        Optional.ofNullable(updateVO.getAppId()).ifPresent(mergedUpdateVO::setAppId);
        Optional.ofNullable(updateVO.getHost()).ifPresent(mergedUpdateVO::setHost);
        Optional.ofNullable(updateVO.getHookHost()).ifPresent(mergedUpdateVO::setHookHost);
        Optional.ofNullable(updateVO.getHttpPort()).ifPresent(mergedUpdateVO::setHttpPort);
        Optional.ofNullable(updateVO.getSecret()).ifPresent(mergedUpdateVO::setSecret);
        Optional.ofNullable(updateVO.getType()).ifPresent(mergedUpdateVO::setType);
        Optional.ofNullable(updateVO.getRemark()).ifPresent(mergedUpdateVO::setRemark);
        Optional.ofNullable(updateVO.getExtendParams()).ifPresent(mergedUpdateVO::setExtendParams);
        Optional.ofNullable(updateVO.getName()).ifPresent(mergedUpdateVO::setName);

        return mergedUpdateVO;
    }

    /**
     * 校验保存参数
     *
     * @param saveVO 保存参数
     */
    private void checkSaveVO(VideoMediaServerSaveVO saveVO) {
        ArgumentAssert.notBlank(saveVO.getAppId(), "应用ID不能为空");
        ArgumentAssert.notBlank(saveVO.getName(), "名称不能为空");
        ArgumentAssert.notBlank(saveVO.getType(), "类型不能为空");
        ArgumentAssert.notBlank(saveVO.getHost(), "服务器地址不能为空");
        ArgumentAssert.notNull(saveVO.getHttpPort(), "HTTP端口不能为空");
        ArgumentAssert.notNull(saveVO.getSecret(), "鉴权参数不能为空");


        // 校验相同链接是否存在
        validateUniqueConnection(saveVO.getHost(), saveVO.getHttpPort(), null);


        Optional<VideoMediaServerTypeEnum> videoMediaServerTypeEnum = VideoMediaServerTypeEnum.fromValue(saveVO.getType());
        if (videoMediaServerTypeEnum.isEmpty()) {
            throw BizException.wrap("type is not exist");
        }


    }

    /**
     * 校验更新参数
     *
     * @param updateVO 更新参数
     */
    private void checkUpdateVO(VideoMediaServerUpdateVO updateVO) {
        ArgumentAssert.notNull(updateVO.getId(), "id不能为空");
        ArgumentAssert.notBlank(updateVO.getAppId(), "应用ID不能为空");
        ArgumentAssert.notBlank(updateVO.getName(), "名称不能为空");
        ArgumentAssert.notBlank(updateVO.getType(), "类型不能为空");
        ArgumentAssert.notBlank(updateVO.getHost(), "服务器地址不能为空");
        ArgumentAssert.notNull(updateVO.getHttpPort(), "HTTP端口不能为空");
        ArgumentAssert.notNull(updateVO.getSecret(), "鉴权参数不能为空");


        VideoMediaServer videoMediaServer = superManager.getById(updateVO.getId());

        if (null == videoMediaServer) {
            throw BizException.wrap("videoMediaServer is not exist");
        }
        // 校验相同链接是否存在，排除当前记录
        validateUniqueConnection(updateVO.getHost(), updateVO.getHttpPort(), updateVO.getId());
    }

    /**
     * 校验媒体唯一标识合法性
     *
     * @param mediaIdentifier 媒体唯一标识
     */
    private void validateMediaIdentifier(String mediaIdentifier) {
        // 提取租户ID
        String tenantId = TenantUtil.extractTenantId(mediaIdentifier);
        if (StrUtil.isBlank(tenantId)) {
            throw BizException.wrap("媒体唯一标识格式错误，必须以@{}结尾", ContextUtil.getTenantIdStr());
        }

        // 租户一致性校验（必须是当前租户 ContextUtil）
        if (!TenantUtil.validateTenantConsistency(mediaIdentifier)) {
            throw BizException.wrap("您无权操作其他租户资源");
        }
    }


    private void validateUniqueConnection(String ip, Integer httpPort, Long excludeId) {
        LbQueryWrap<VideoMediaServer> wrap = Wraps.lbQ();
        wrap.eq(VideoMediaServer::getHost, ip)
                .eq(VideoMediaServer::getHttpPort, httpPort);
        if (excludeId != null) {
            wrap.ne(VideoMediaServer::getId, excludeId);
        }
        List<VideoMediaServer> list = list(wrap);

        if (CollUtil.isNotEmpty(list)) {
            throw BizException.wrap("This connection already exists, do not add it again");
        }
    }

    /**
     * 构建保存参数
     *
     * @param saveVO 保存参数
     * @return {@link VideoMediaServer} 实体
     */
    private VideoMediaServer buildSaveVO(VideoMediaServerSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, VideoMediaServer.class);
    }
}


