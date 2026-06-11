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
public class ConfigDownloadResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.CONFIG_DOWNLOAD.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private DeferredResultHolder deferredResultHolder;

//    @Autowired
//    private IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }


    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element element) {
        try {
            // 回复200 OK
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 设备配置查询: {}", e.getMessage());
        }
        // 此处是对本平台发出DeviceControl指令的应答
        JSONObject json = new JSONObject();
        XmlUtil.node2Json(element, json);
        if (log.isDebugEnabled()) {
            log.debug(json.toJSONString());
        }
        JSONObject jsonObject = new JSONObject();
        if (json.get("BasicParam") != null) {
            jsonObject.put("BasicParam", json.getJSONObject("BasicParam"));
        }
        if (json.get("VideoParamOpt") != null) {
            jsonObject.put("VideoParamOpt", json.getJSONObject("VideoParamOpt"));
        }
        if (json.get("SVACEncodeConfig") != null) {
            jsonObject.put("SVACEncodeConfig", json.getJSONObject("SVACEncodeConfig"));
        }
        if (json.get("SVACDecodeConfig") != null) {
            jsonObject.put("SVACDecodeConfig", json.getJSONObject("SVACDecodeConfig"));
        }

        responseMessageHandler.handMessageEvent(element, jsonObject);

        JSONObject basicParam = json.getJSONObject("BasicParam");
        if (basicParam != null) {
            Integer heartBeatInterval = basicParam.getInteger("HeartBeatInterval");
            Integer heartBeatCount = basicParam.getInteger("HeartBeatCount");
            Integer positionCapability = basicParam.getInteger("PositionCapability");
            // TODO
            /*device.setHeartBeatInterval(heartBeatInterval);
            device.setHeartBeatCount(heartBeatCount);
            device.setPositionCapability(positionCapability);

            deviceService.updateDeviceHeartInfo(device);*/
        }

    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {
        // 不会收到上级平台的心跳信息

    }
}
