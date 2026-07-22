package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
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

/**
 * 移动设备位置查询应答消息处理器。
 * <p>
 * 处理设备对 MobilePosition 查询命令的应答（不同于NOTIFY主动上报），
 * 解析经度、纬度、速度、方向、海拔、时间等位置信息。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class MobilePositionResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.MOBILE_POSITION.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfoResultDTO, Element element) {
        log.info("接收到MobilePosition应答消息");
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 移动设备位置查询应答回复200OK: {}", e.getMessage());
        }

        String deviceIdentification = XmlUtil.getText(element, "DeviceID");
        String longitude = XmlUtil.getText(element, "Longitude");
        String latitude = XmlUtil.getText(element, "Latitude");
        String speed = XmlUtil.getText(element, "Speed");
        String direction = XmlUtil.getText(element, "Direction");
        String altitude = XmlUtil.getText(element, "Altitude");
        String time = XmlUtil.getText(element, "Time");

        log.info("[移动位置] 设备: {}, 通道: {}, 经度: {}, 纬度: {}, 速度: {}, 方向: {}, 海拔: {}, 时间: {}",
                deviceInfoResultDTO.getDeviceIdentification(), deviceIdentification, longitude, latitude, speed, direction, altitude, time);

        JSONObject positionInfo = new JSONObject();
        positionInfo.put("DeviceID", deviceIdentification);
        positionInfo.put("Longitude", longitude);
        positionInfo.put("Latitude", latitude);
        positionInfo.put("Speed", speed);
        positionInfo.put("Direction", direction);
        positionInfo.put("Altitude", altitude);
        positionInfo.put("Time", time);

        if (log.isDebugEnabled()) {
            log.debug("[移动位置] 详情: {}", positionInfo.toJSONString());
        }

        responseMessageHandler.handMessageEvent(element, positionInfo);
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {

    }
}
