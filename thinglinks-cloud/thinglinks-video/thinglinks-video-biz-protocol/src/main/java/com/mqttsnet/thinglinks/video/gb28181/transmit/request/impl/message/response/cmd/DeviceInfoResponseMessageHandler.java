package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceExtendParams;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import cn.hutool.core.date.DateUtil;

import java.text.ParseException;

/**
 * 设备信息查询应答消息处理器。
 * <p>
 * 处理设备对 DeviceInfo 查询命令的应答，解析设备名称、厂商、型号、固件版本、通道数等信息，
 * 并回写到数据库更新设备记录。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class DeviceInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.DEVICE_INFO.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfoResultDTO, Element element) {
        log.info("接收到DeviceInfo应答消息");
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 设备信息查询应答回复200OK: {}", e.getMessage());
        }

        String deviceIdentification = deviceInfoResultDTO.getDeviceIdentification();
        String deviceName = XmlUtil.getText(element, "DeviceName");
        String manufacturer = XmlUtil.getText(element, "Manufacturer");
        String model = XmlUtil.getText(element, "Model");
        String firmware = XmlUtil.getText(element, "Firmware");
        String channel = XmlUtil.getText(element, "Channel");
        // GB/T 28181-2016 §9.1.2：SummaryNum 表示该设备及子设备总数（含自身）
        String summaryNum = XmlUtil.getText(element, "SummaryNum");

        log.info("[设备信息] 设备: {}, 名称: {}, 厂商: {}, 型号: {}, 固件: {}, 通道数: {}, 总数: {}",
                deviceIdentification, deviceName, manufacturer, model, firmware, channel, summaryNum);

        // 1) 回写独立列字段
        try {
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setDeviceIdentification(deviceIdentification);
            if (StrUtil.isNotBlank(deviceName)) {
                updateVO.setDeviceName(deviceName);
            }
            if (StrUtil.isNotBlank(manufacturer)) {
                updateVO.setManufacturer(manufacturer);
            }
            if (StrUtil.isNotBlank(model)) {
                updateVO.setModel(model);
            }
            if (StrUtil.isNotBlank(firmware)) {
                updateVO.setFirmware(firmware);
            }
            if (StrUtil.isNotBlank(channel)) {
                Integer channelCount = NumberUtil.parseInt(channel, null);
                if (channelCount != null) {
                    updateVO.setChannelCount(channelCount);
                }
            }
            videoDeviceService.updateDeviceInfo(updateVO);
            log.info("[设备信息回写] 设备: {}, 名称已更新为: {}", deviceIdentification, deviceName);
        } catch (Exception e) {
            log.warn("[设备信息回写] 设备: {}, 更新失败: {}", deviceIdentification, e.getMessage());
        }

        // 2) GB 扩展字段写入 extend_params（按需合并，不覆盖其他 handler 写入的字段）
        try {
            Integer summaryNumInt = NumberUtil.parseInt(summaryNum, null);
            if (summaryNumInt != null) {
                videoDeviceService.patchExtendParams(deviceIdentification,
                        VideoDeviceExtendParams.builder().summaryNum(summaryNumInt).build());
            }
        } catch (Exception e) {
            log.warn("[设备信息扩展字段回写] 设备: {}, 更新失败: {}", deviceIdentification, e.getMessage());
        }

        // 3) 记录本次 DeviceInfo 同步时间到 protocol_config（按需合并）
        try {
            videoDeviceService.patchProtocolConfig(deviceIdentification,
                    VideoDeviceProtocolConfig.builder().deviceInfoSyncTime(DateUtil.now()).build());
        } catch (Exception e) {
            log.warn("[设备信息同步时间回写] 设备: {}, 更新失败: {}", deviceIdentification, e.getMessage());
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {

    }
}
