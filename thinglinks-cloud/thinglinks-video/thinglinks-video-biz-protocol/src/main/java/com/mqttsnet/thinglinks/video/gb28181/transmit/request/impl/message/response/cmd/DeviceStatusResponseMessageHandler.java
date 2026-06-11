package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
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
import java.text.ParseException;

@Slf4j
@Component
public class DeviceStatusResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.DEVICE_STATUS.getValue();

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
        log.info("接收到DeviceStatus应答消息");
        // 检查设备是否存在， 不存在则不回复
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 国标级联 设备状态应答回复200OK: {}", e.getMessage());
        }
        // 解析在线状态
        String onlineText = XmlUtil.getText(element, "Online");
        String statusText = XmlUtil.getText(element, "Status");
        String reason = XmlUtil.getText(element, "Reason");

        log.info("[设备状态] 设备: {}, Online={}, Status={}, Reason={}",
                deviceInfoResultDTO.getDeviceIdentification(), onlineText, statusText, reason);

        // 回写设备在线状态到 DB
        try {
            boolean isOnline = "ONLINE".equalsIgnoreCase(onlineText);
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setDeviceIdentification(deviceInfoResultDTO.getDeviceIdentification());
            updateVO.setOnlineStatus(isOnline);
            videoDeviceService.updateDeviceInfo(updateVO);
        } catch (Exception e) {
            log.warn("[设备状态] 回写失败: {}", e.getMessage());
        }

        responseMessageHandler.handMessageEvent(element, onlineText);
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {


    }
}
