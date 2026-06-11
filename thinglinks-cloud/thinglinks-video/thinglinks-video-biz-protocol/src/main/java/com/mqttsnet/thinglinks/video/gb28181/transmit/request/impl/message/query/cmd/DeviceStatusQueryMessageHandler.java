package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.query.cmd;


import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.query.QueryMessageHandler;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.FromHeader;
import javax.sip.message.Response;
import java.text.ParseException;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

;

@Slf4j
@Component
public class DeviceStatusQueryMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {


    private final String cmdType = CmdTypeEnum.DEVICE_STATUS.getValue();

    @Autowired
    private QueryMessageHandler queryMessageHandler;

   /* @Autowired
    private IGbChannelService channelService;

    @Autowired
    private ISIPCommanderForPlatform cmderFroPlatform;*/

    @Override
    public void afterPropertiesSet() throws Exception {
        queryMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element element) {

    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo platform, Element rootElement) {

        log.info("接收到DeviceStatus查询消息");
        FromHeader fromHeader = (FromHeader) evt.getRequest().getHeader(FromHeader.NAME);
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 国标级联 DeviceStatus查询回复200OK: {}", e.getMessage());
        }
        String sn = rootElement.element("SN").getText();
        String channelId = getText(rootElement, "DeviceID");
       /* CommonGBChannel channel = channelService.queryOneWithPlatform(parentPlatform.getId(), channelId);
        if (channel == null) {
            log.error("[平台没有该通道的使用权限]:platformId" + parentPlatform.getServerGBId() + "  deviceID:" + channelId);
            return;
        }
        try {
            cmderFroPlatform.deviceStatusResponse(parentPlatform, channelId, sn, fromHeader.getTag(), "ON".equalsIgnoreCase(channel.getGbStatus()));
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 国标级联 DeviceStatus查询回复: {}", e.getMessage());
        }*/
    }
}
