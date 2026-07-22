package com.mqttsnet.thinglinks.video.service.device.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.device.VideoDevice;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceManager;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceQueryService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频设备只读查询 Service 实现。
 *
 * <p>仅持有 {@link VideoDeviceManager},零下游 Service 依赖,物理上不可能进入循环图。</p>
 *
 * @author mqttsnet
 * @since 2026-05-18
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceQueryServiceImpl implements VideoDeviceQueryService {

    private final VideoDeviceManager videoDeviceManager;

    @Override
    public VideoDeviceResultVO getByDeviceIdentification(String deviceIdentification) {
        VideoDevice device = videoDeviceManager.getOneByDeviceIdentification(deviceIdentification);
        return BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceResultVO.class);
    }
}
