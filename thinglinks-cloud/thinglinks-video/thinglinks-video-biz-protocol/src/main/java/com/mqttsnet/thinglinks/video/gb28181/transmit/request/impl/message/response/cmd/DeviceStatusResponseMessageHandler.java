package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceInfoService;
import com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil;
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
    private VideoDeviceInfoService deviceInfoService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element element) {
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
        Element onlineElement = element.element("Online");
        JSONObject json = new JSONObject();
        XmlUtil.node2Json(element, json);
        if (log.isDebugEnabled()) {
            log.debug(json.toJSONString());
        }
        String text = onlineElement.getText();
        responseMessageHandler.handMessageEvent(element, text);

    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {


    }
}
