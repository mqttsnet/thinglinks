package com.mqttsnet.thinglinks.video.cache.device;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceQueryService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频设备缓存 DB 回源辅助 service ── 供 VideoCacheDataHelperImpl 的 read-through loader 调用,
 * 按设备国标编号反序列化为 {@link VideoDeviceCacheVO}。本类不写缓存,写缓存由 CachePlusUtil.getOrLoad 统一负责避免双写竞态。
 * 仅依赖 leaf {@link VideoDeviceQueryService}(零下游 Service 依赖,类图天然 DAG)规避构造期循环;严禁反向依赖 helper / 业务 Service。
 *
 * @author mqttsnet
 */
@DS(DsConstant.BASE_TENANT)
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoDeviceCacheService {

    private final VideoDeviceQueryService videoDeviceQueryService;

    /**
     * 仅从 DB 加载视频设备 VO,不写缓存 ── 供 VideoCacheDataHelperImpl#getDeviceInfo 的 read-through 回源使用。
     * 参数空 / DB 不存在 / 转换异常返 null(由调用方决定是否缓存 null 防穿透)。
     *
     * @param deviceIdentification 设备国标编号
     * @return {@link VideoDeviceCacheVO};失败返 null
     */
    public VideoDeviceCacheVO loadDeviceFromDb(String deviceIdentification) {
        if (deviceIdentification == null || deviceIdentification.isBlank()) {
            return null;
        }
        try {
            VideoDeviceResultVO vo = videoDeviceQueryService.getByDeviceIdentification(deviceIdentification);
            if (vo == null) {
                log.warn("[video-device-fallback] DB miss deviceIdentification={}", deviceIdentification);
                return null;
            }
            return BeanPlusUtil.toBeanIgnoreError(vo, VideoDeviceCacheVO.class);
        } catch (Exception e) {
            log.error("[video-device-fallback] load failed deviceIdentification={}", deviceIdentification, e);
            return null;
        }
    }
}
