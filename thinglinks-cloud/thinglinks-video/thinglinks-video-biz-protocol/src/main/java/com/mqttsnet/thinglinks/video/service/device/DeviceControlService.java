package com.mqttsnet.thinglinks.video.service.device;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.enumeration.device.DeviceControlTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.DeviceControlEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.DeviceControlExecutedEventSource;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import com.mqttsnet.thinglinks.video.gb28181.cmd.SipMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import org.springframework.stereotype.Service;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * 设备远程控制业务服务。
 * 提供设备远程启动、录像控制、布防撤防、告警控制、
 * 强制关键帧、看守位等设备控制功能。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class DeviceControlService {

    private final VideoDeviceService videoDeviceService;
    private final SipMessageBuilder sipMessageBuilder;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final DeviceControlEventPublisher deviceControlEventPublisher;

    private final AtomicInteger snGenerator = new AtomicInteger(1);

    /**
     * 执行设备控制命令
     *
     * @param deviceIdentification     设备国标编号
     * @param channelIdentification    通道国标编号
     * @param controlType  控制类型
     * @param controlValue 控制值
     */
    public void deviceControl(String deviceIdentification, String channelIdentification, String controlType, String controlValue) {
        ArgumentAssert.notBlank(deviceIdentification, "设备编号不能为空");
        ArgumentAssert.notBlank(channelIdentification, "通道编号不能为空");
        ArgumentAssert.notBlank(controlType, "控制类型不能为空");

        DeviceControlTypeEnum typeEnum = DeviceControlTypeEnum.fromValue(controlType)
                .orElseThrow(() -> BizException.wrap("无效的控制类型: " + controlType));

        VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("设备不存在: " + deviceIdentification);
        }
        if (!Boolean.TRUE.equals(device.getOnlineStatus())) {
            throw BizException.wrap("设备离线: " + deviceIdentification);
        }

        String transport = StrUtil.isNotBlank(device.getTransport()) ? device.getTransport() : "UDP";
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = buildDeviceControlXml(sn, channelIdentification, typeEnum, controlValue);

        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();
            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            Request request = sipMessageBuilder.buildMessageRequest(
                    deviceIdentification, device.getHost(), device.getPort(), transport, xml, callId, tenantConfig);
            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("设备控制发送成功: deviceIdentification={}, channelIdentification={}, type={}, value={}",
                    deviceIdentification, channelIdentification, controlType, controlValue);

            deviceControlEventPublisher.publishDeviceControlExecutedEvent(
                    DeviceControlExecutedEventSource.builder()
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(channelIdentification)
                            .controlType(controlType)
                            .controlValue(controlValue)
                            .build());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("设备控制发送失败: deviceIdentification={}, type={}", deviceIdentification, controlType, e);
            throw BizException.wrap("设备控制失败: " + e.getMessage());
        }
    }

    /**
     * 远程启动
     */
    public void teleBoot(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.TELE_BOOT.getValue(), "Boot");
    }

    /**
     * 录像控制 - 开始录像
     */
    public void startRecord(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.RECORD.getValue(), "Record");
    }

    /**
     * 录像控制 - 停止录像
     */
    public void stopRecord(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.RECORD.getValue(), "StopRecord");
    }

    /**
     * 布防
     */
    public void setGuard(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.GUARD.getValue(), "SetGuard");
    }

    /**
     * 撤防
     */
    public void resetGuard(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.GUARD.getValue(), "ResetGuard");
    }

    /**
     * 强制关键帧
     */
    public void forceKeyFrame(String deviceIdentification, String channelIdentification) {
        deviceControl(deviceIdentification, channelIdentification, DeviceControlTypeEnum.I_FRAME.getValue(), null);
    }

    /**
     * 构建设备控制 XML
     */
    private String buildDeviceControlXml(String sn, String channelIdentification,
                                         DeviceControlTypeEnum typeEnum, String controlValue) {
        var sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"GB2312\"?>\n");
        sb.append("<Control>\n");
        sb.append("<CmdType>DeviceControl</CmdType>\n");
        sb.append("<SN>").append(sn).append("</SN>\n");
        sb.append("<DeviceID>").append(channelIdentification).append("</DeviceID>\n");

        if (StrUtil.isNotBlank(controlValue)) {
            sb.append("<").append(typeEnum.getValue()).append(">")
                    .append(controlValue)
                    .append("</").append(typeEnum.getValue()).append(">\n");
        } else {
            sb.append("<").append(typeEnum.getValue()).append("/>\n");
        }

        sb.append("</Control>");
        return sb.toString();
    }
}
