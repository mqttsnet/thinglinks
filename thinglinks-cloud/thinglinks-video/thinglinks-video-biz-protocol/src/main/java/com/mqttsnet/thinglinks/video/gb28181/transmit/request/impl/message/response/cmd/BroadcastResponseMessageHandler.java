package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;


import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.empowerment.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.RequestEvent;

@Slf4j
@Component
public class BroadcastResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {


    private final String cmdType = CmdTypeEnum.BROADCAST.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

   /* @Autowired
    private IDeviceChannelService deviceChannelService;

    @Autowired
    private AudioBroadcastManager audioBroadcastManager;

    @Autowired
    private IPlayService playService;*/

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceInfoResultDTO deviceInfoResultDTO, Element rootElement) {

        SIPRequest request = (SIPRequest) evt.getRequest();
        /*try {
            String channelId = getText(rootElement, "DeviceID");
            DeviceChannel channel = null;
            if (!channelId.equals(device.getDeviceId())) {
                channel = deviceChannelService.getOneBySourceId(device.getId(), channelId);
            }else {
                channel = deviceChannelService.getBroadcastChannel(device.getId());
            }
            if (channel == null) {
                log.info("[语音广播]回复： 未找到通道{}/{}", device.getDeviceId(), channelId );
                // 回复410
                responseAck((SIPRequest) evt.getRequest(), Response.NOT_FOUND);
                return;
            }
            if (!audioBroadcastManager.exit(channel.getId())) {
                // 回复410
                responseAck((SIPRequest) evt.getRequest(), Response.BUSY_HERE);
                return;
            }
            String result = getText(rootElement, "Result");
            Element infoElement = rootElement.element("Info");
            String reason = null;
            if (infoElement != null) {
                reason = getText(infoElement, "Reason");
            }
            log.info("[语音广播]回复：{}, {}/{}", reason == null? result : result + ": " + reason, device.getDeviceId(), channelId );

            // 回复200 OK
            responseAck(request, Response.OK);
            if (result.equalsIgnoreCase("OK")) {
                AudioBroadcastCatch audioBroadcastCatch = audioBroadcastManager.get(channel.getId());
                audioBroadcastCatch.setStatus(AudioBroadcastCatchStatus.WaiteInvite);
                audioBroadcastManager.update(audioBroadcastCatch);
            }else {
                playService.stopAudioBroadcast(device, channel);
            }
        } catch (ParseException | SipException | InvalidArgumentException e) {
            log.error("[命令发送失败] 国标级联 语音喊话: {}", e.getMessage());
        }*/
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {

    }


}
