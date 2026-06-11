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

/**
 *
 * @author mqttsnet
 */
@Slf4j
@Component
public class CatalogQueryMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.CATALOG.getValue();

    @Autowired
    private QueryMessageHandler queryMessageHandler;

   /*
    @Autowired
    private IGbChannelService channelService;

    @Autowired
    private IPlatformChannelService platformChannelService;

    @Autowired
    private SIPCommanderForPlatform cmderFroPlatform;*/


    @Override
    public void afterPropertiesSet() throws Exception {
        queryMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element element) {
        try {
            // 回复200 OK
            responseAck((SIPRequest) evt.getRequest(), Response.FORBIDDEN);
        } catch (SipException | InvalidArgumentException | ParseException ignored) {
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo platform, Element rootElement) {

        FromHeader fromHeader = (FromHeader) evt.getRequest().getHeader(FromHeader.NAME);
        try {
            // 回复200 OK
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 国标级联 目录查询回复200OK: {}", e.getMessage());
        }
        Element snElement = rootElement.element("SN");
        String sn = snElement.getText();
        // TODO 待处理
       /* List<CommonGBChannel> channelList = platformChannelService.queryByPlatform(platform);

        try {
            if (!channelList.isEmpty()) {
                cmderFroPlatform.catalogQuery(channelList, platform, sn, fromHeader.getTag());
            } else {
                // 回复无通道
                cmderFroPlatform.catalogQuery(null, platform, sn, fromHeader.getTag(), 0);
            }
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 国标级联 目录查询回复: {}", e.getMessage());
        }*/
    }
}
