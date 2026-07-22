package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;
import com.mqttsnet.thinglinks.video.gb28181.cmd.SubscribeCommander;
import com.mqttsnet.thinglinks.video.manager.device.VideoDeviceMobilePositionManager;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceMobilePositionService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceMobilePositionResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备移动位置业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceMobilePositionServiceImpl extends SuperServiceImpl<VideoDeviceMobilePositionManager, Long, VideoDeviceMobilePosition> implements VideoDeviceMobilePositionService {

    private final VideoDeviceService videoDeviceService;
    private final SubscribeCommander subscribeCommander;

    @Override
    public VideoDeviceMobilePosition savePosition(VideoDeviceMobilePosition position) {
        superManager.save(position);
        log.debug("保存设备位置: deviceIdentification={}, lng={}, lat={}",
                position.getDeviceIdentification(), position.getLongitude(), position.getLatitude());
        return position;
    }

    @Override
    public VideoDeviceMobilePositionResultVO getLatestPosition(String deviceIdentification) {
        VideoDeviceMobilePosition position = superManager.getLatestPosition(deviceIdentification);
        if (position == null) {
            return null;
        }
        return BeanPlusUtil.toBeanIgnoreError(position, VideoDeviceMobilePositionResultVO.class);
    }

    @Override
    public void subscribe(String deviceIdentification, int interval) {
        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线: " + deviceIdentification);
        }
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
        subscribeCommander.mobilePositionSubscribe(deviceIdentification, device.getHost(), device.getPort(),
                transport, GbProtocolVersionEnum.GB2016, interval);
        log.info("发起移动位置订阅: deviceIdentification={}, interval={}s", deviceIdentification, interval);
    }

    @Override
    public void unsubscribe(String deviceIdentification) {
        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
        subscribeCommander.mobilePositionUnsubscribe(deviceIdentification, device.getHost(), device.getPort(),
                transport, GbProtocolVersionEnum.GB2016);
        log.info("取消移动位置订阅: deviceIdentification={}", deviceIdentification);
    }

    @Override
    public IPage<VideoDeviceMobilePositionResultVO> pageHistory(Page<VideoDeviceMobilePosition> page,
                                                                 String deviceIdentification,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime) {
        IPage<VideoDeviceMobilePosition> positionPage = superManager.pageHistory(page, deviceIdentification, startTime, endTime);
        return BeanPlusUtil.toBeanPage(positionPage, VideoDeviceMobilePositionResultVO.class);
    }
}
