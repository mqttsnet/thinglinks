package com.mqttsnet.thinglinks.video.service.device;

import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;

/**
 * 视频设备只读查询 Service。
 *
 * <p>独立于 {@link VideoDeviceService},仅持有 {@code VideoDeviceManager},零下游 Service 依赖,
 * 类图天然为 DAG。专供 {@code VideoCacheDataHelper} 等反向访问设备基础信息使用,
 * 从根本规避 helper ↔ device service 构造期循环依赖。</p>
 *
 * @author mqttsnet
 * @since 2026-05-18
 */
public interface VideoDeviceQueryService {

    /**
     * 根据设备标识查询视频设备基础信息。
     *
     * @param deviceIdentification 设备标识
     * @return {@link VideoDeviceResultVO} 视频设备信息;未命中返回 {@code null}
     */
    VideoDeviceResultVO getByDeviceIdentification(String deviceIdentification);
}
