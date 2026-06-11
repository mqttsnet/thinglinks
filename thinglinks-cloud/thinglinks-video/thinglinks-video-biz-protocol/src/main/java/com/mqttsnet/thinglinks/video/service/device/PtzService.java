package com.mqttsnet.thinglinks.video.service.device;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.enumeration.device.PtzCommandEnum;
import com.mqttsnet.thinglinks.video.enumeration.device.PtzDirectionEnum;
import com.mqttsnet.thinglinks.video.vo.save.device.PtzControlSaveVO;
import com.mqttsnet.thinglinks.video.gb28181.cmd.PTZCommander;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.DeviceControlEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.PtzCommandExecutedEventSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

/**
 * Description:
 * PTZ 云台控制业务服务。
 * 封装 PTZ 方向控制、变倍控制、预置位操作、巡航控制、扫描控制、辅助开关等功能。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class PtzService {

    private final VideoDeviceService videoDeviceService;
    private final PTZCommander ptzCommander;
    private final DeviceControlEventPublisher deviceControlEventPublisher;

    private static final int DEFAULT_SPEED = 128;

    /**
     * 统一 PTZ 命令分发
     *
     * @param saveVO PTZ 控制请求参数
     */
    public void executeCommand(PtzControlSaveVO saveVO) {
        PtzCommandEnum command = PtzCommandEnum.fromValue(saveVO.getCommand())
                .orElseThrow(() -> BizException.wrap("不支持的PTZ命令: " + saveVO.getCommand()));
        String deviceIdentification = saveVO.getDeviceIdentification();
        String channelIdentification = saveVO.getChannelIdentification();

        switch (command) {
            case DIRECTION -> directionControl(deviceIdentification, channelIdentification,
                    saveVO.getDirection(), saveVO.getMoveSpeed());
            case ZOOM -> zoomControl(deviceIdentification, channelIdentification,
                    saveVO.getZoomDirection() != null ? saveVO.getZoomDirection() : 0,
                    saveVO.getZoomSpeed());
            case STOP -> stopPtz(deviceIdentification, channelIdentification);
            case PRESET_SET -> presetSet(deviceIdentification, channelIdentification, saveVO.getPresetId());
            case PRESET_CALL -> presetCall(deviceIdentification, channelIdentification, saveVO.getPresetId());
            case PRESET_DELETE -> presetDelete(deviceIdentification, channelIdentification, saveVO.getPresetId());
            case CRUISE_START -> cruiseStart(deviceIdentification, channelIdentification, saveVO.getCruiseId());
            case CRUISE_STOP -> cruiseStop(deviceIdentification, channelIdentification, saveVO.getCruiseId());
            case CRUISE_ADD_POINT -> cruiseAddPoint(deviceIdentification, channelIdentification,
                    saveVO.getCruiseId(), saveVO.getPresetId(),
                    saveVO.getCruiseSpeed() != null ? saveVO.getCruiseSpeed() : 50,
                    saveVO.getCruiseStayTime() != null ? saveVO.getCruiseStayTime() : 10);
            case CRUISE_DELETE_POINT -> cruiseDeletePoint(deviceIdentification, channelIdentification,
                    saveVO.getCruiseId(), saveVO.getPresetId());
            case SCAN_START -> scanStart(deviceIdentification, channelIdentification,
                    saveVO.getScanId(), saveVO.getScanSpeed() != null ? saveVO.getScanSpeed() : 50);
            case SCAN_STOP -> scanStop(deviceIdentification, channelIdentification, saveVO.getScanId());
            case SCAN_SET_LEFT -> scanStart(deviceIdentification, channelIdentification, saveVO.getScanId(), 0);
            case AUX_SWITCH -> auxiliarySwitch(deviceIdentification, channelIdentification,
                    saveVO.getSwitchId(), Boolean.TRUE.equals(saveVO.getSwitchOn()));
            case COMBINED, SCAN_SET_RIGHT -> throw BizException.wrap("PTZ命令暂未实现: " + command.getDesc());
        }
    }

    /**
     * 方向控制
     */
    public void directionControl(String deviceIdentification, String channelIdentification, String direction, Integer moveSpeed) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        PtzDirectionEnum dirEnum = PtzDirectionEnum.fromValue(direction)
                .orElseThrow(() -> BizException.wrap("无效的方向: " + direction));

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        int speed = (moveSpeed != null && moveSpeed > 0) ? moveSpeed : DEFAULT_SPEED;
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.directionControl(deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                transport, dirEnum.getLeftRight(), dirEnum.getUpDown(), speed);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.DIRECTION.getValue(), direction, speed, null);
    }

    /**
     * 变倍控制
     */
    public void zoomControl(String deviceIdentification, String channelIdentification, int inOut, Integer zoomSpeed) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        int speed = (zoomSpeed != null && zoomSpeed > 0) ? zoomSpeed : DEFAULT_SPEED;
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.zoomControl(deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                transport, inOut, speed);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.ZOOM.getValue(), null, speed, null);
    }

    /**
     * 停止 PTZ 控制
     */
    public void stopPtz(String deviceIdentification, String channelIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.stopPtz(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.STOP.getValue(), null, null, null);
    }

    /**
     * 预置位 - 设置
     */
    public void presetSet(String deviceIdentification, String channelIdentification, int presetId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.isTrue(presetId >= 1 && presetId <= 255, "预置位编号范围: 1-255");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.presetSet(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, presetId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.PRESET_SET.getValue(), null, null, presetId);
    }

    /**
     * 预置位 - 调用
     */
    public void presetCall(String deviceIdentification, String channelIdentification, int presetId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.isTrue(presetId >= 1 && presetId <= 255, "预置位编号范围: 1-255");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.presetCall(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, presetId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.PRESET_CALL.getValue(), null, null, presetId);
    }

    /**
     * 预置位 - 删除
     */
    public void presetDelete(String deviceIdentification, String channelIdentification, int presetId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.isTrue(presetId >= 1 && presetId <= 255, "预置位编号范围: 1-255");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.presetDelete(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, presetId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.PRESET_DELETE.getValue(), null, null, presetId);
    }

    /**
     * 巡航 - 开始
     */
    public void cruiseStart(String deviceIdentification, String channelIdentification, int cruiseId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.cruiseStart(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, cruiseId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.CRUISE_START.getValue(), null, null, null);
    }

    /**
     * 巡航 - 停止
     */
    public void cruiseStop(String deviceIdentification, String channelIdentification, int cruiseId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.cruiseStop(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, cruiseId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.CRUISE_STOP.getValue(), null, null, null);
    }

    /**
     * 巡航 - 添加巡航点
     */
    public void cruiseAddPoint(String deviceIdentification, String channelIdentification, int cruiseId,
                                int presetId, int speed, int stayTime) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.cruiseAddPoint(deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                transport, cruiseId, presetId, speed, stayTime);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.CRUISE_ADD_POINT.getValue(), null, speed, presetId);
    }

    /**
     * 巡航 - 删除巡航点
     */
    public void cruiseDeletePoint(String deviceIdentification, String channelIdentification, int cruiseId, int presetId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.cruiseDeletePoint(deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                transport, cruiseId, presetId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.CRUISE_DELETE_POINT.getValue(), null, null, presetId);
    }

    /**
     * 扫描 - 开始
     */
    public void scanStart(String deviceIdentification, String channelIdentification, int scanId, int speed) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.scanStart(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, scanId, speed);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.SCAN_START.getValue(), null, speed, null);
    }

    /**
     * 扫描 - 停止
     */
    public void scanStop(String deviceIdentification, String channelIdentification, int scanId) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.scanStop(deviceIdentification, channelIdentification, device.getHost(), device.getPort(), transport, scanId);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.SCAN_STOP.getValue(), null, null, null);
    }

    /**
     * 辅助开关控制
     */
    public void auxiliarySwitch(String deviceIdentification, String channelIdentification, int switchId, boolean on) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.isTrue(switchId >= 1 && switchId <= 255, "开关编号范围: 1-255");

        VideoDeviceResultVO device = getOnlineDevice(deviceIdentification);
        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";

        ptzCommander.auxiliarySwitch(deviceIdentification, channelIdentification, device.getHost(), device.getPort(),
                transport, switchId, on);

        publishPtzEvent(deviceIdentification, channelIdentification, PtzCommandEnum.AUX_SWITCH.getValue(), null, null, null);
    }

    private VideoDeviceResultVO getOnlineDevice(String deviceIdentification) {
        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线: " + deviceIdentification);
        }
        return device;
    }

    private void publishPtzEvent(String deviceIdentification, String channelIdentification, String commandType,
                                 String direction, Integer speed, Integer presetId) {
        deviceControlEventPublisher.publishPtzCommandExecutedEvent(
                PtzCommandExecutedEventSource.builder()
                        .deviceIdentification(deviceIdentification)
                        .channelIdentification(channelIdentification)
                        .commandType(commandType)
                        .direction(direction)
                        .speed(speed)
                        .presetId(presetId)
                        .build());
    }
}
