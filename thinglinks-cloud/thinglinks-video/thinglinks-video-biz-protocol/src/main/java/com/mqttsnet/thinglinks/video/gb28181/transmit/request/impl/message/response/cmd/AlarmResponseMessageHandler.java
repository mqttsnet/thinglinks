package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.callback.DeferredResultHolder;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
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
public class AlarmResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.ALARM.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private DeferredResultHolder deferredResultHolder;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element rootElement) {
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 目录查询回复: {}", e.getMessage());
        }
        JSONObject json = new JSONObject();
        XmlUtil.node2Json(rootElement, json);
        if (log.isDebugEnabled()) {
            log.debug(json.toJSONString());
        }
        responseMessageHandler.handMessageEvent(rootElement, null);
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo platform, Element element) {

    }
}
