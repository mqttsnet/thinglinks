package com.mqttsnet.thinglinks.video.cache.device;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.VideoChannelCacheVO;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频通道缓存 DB 回源辅助 service ── 供 VideoCacheDataHelperImpl 的 read-through loader 调用,
 * 按通道国标编号反序列化为 {@link VideoChannelCacheVO}。本类不写缓存,写缓存由 CachePlusUtil.getOrLoad 统一负责避免双写竞态。
 * 仅依赖 {@link VideoChannelService}(不反向依赖 helper / 本 service,类图天然 DAG),无构造期循环风险。
 *
 * @author mqttsnet
 */
@DS(DsConstant.BASE_TENANT)
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoChannelCacheService {

    private final VideoChannelService videoChannelService;

    /**
     * 仅从 DB 加载视频通道 VO,不写缓存 ── 供 VideoCacheDataHelperImpl#getChannelInfo 的 read-through 回源使用。
     *
     * @param channelIdentification 通道国标编号
     * @return {@link VideoChannelCacheVO};参数空 / DB 不存在 / 异常返 null
     */
    public VideoChannelCacheVO loadChannelFromDb(String channelIdentification) {
        if (channelIdentification == null || channelIdentification.isBlank()) {
            return null;
        }
        try {
            VideoChannelResultVO vo = videoChannelService.getByChannelIdentification(channelIdentification);
            if (vo == null) {
                log.warn("[video-channel-fallback] DB miss channelIdentification={}", channelIdentification);
                return null;
            }
            return BeanPlusUtil.toBeanIgnoreError(vo, VideoChannelCacheVO.class);
        } catch (Exception e) {
            log.error("[video-channel-fallback] load failed channelIdentification={}", channelIdentification, e);
            return null;
        }
    }
}
