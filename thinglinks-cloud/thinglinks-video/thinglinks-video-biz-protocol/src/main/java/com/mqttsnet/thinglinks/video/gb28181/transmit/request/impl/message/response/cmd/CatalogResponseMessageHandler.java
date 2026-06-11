package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.cmd;

import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.CatalogChannelParser;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.response.ResponseMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
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
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 目录查询应答消息处理器。
 * <p>
 * 处理设备对 Catalog 查询命令的应答，解析 DeviceList 中的通道信息并自动入库。
 * 支持多包应答（SumNum 分批上报），每批到达后即 upsert 通道到 video_channel 表。
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class CatalogResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.CATALOG.getValue();

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    @Autowired
    private VideoChannelService videoChannelService;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfoResultDTO, Element element) {
        log.info("接收到Catalog应答消息");
        if (deviceInfoResultDTO == null) {
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[命令发送失败] 目录查询应答回复200OK: {}", e.getMessage());
        }

        String deviceIdentification = deviceInfoResultDTO.getDeviceIdentification();

        try {
            String sn = XmlUtil.getText(element, "SN");
            int sumNum = NumberUtil.parseInt(XmlUtil.getText(element, "SumNum"), 0);

            if (sumNum == 0) {
                log.info("[收到通道] 设备: {} 的通道数为0", deviceIdentification);
                return;
            }

            Element deviceListElement = element.element("DeviceList");
            if (deviceListElement == null) {
                log.warn("[收到通道] DeviceList为空, 设备: {}", deviceIdentification);
                return;
            }

            List<VideoChannel> channels = new ArrayList<>();
            Iterator<Element> deviceListIterator = deviceListElement.elementIterator();
            while (deviceListIterator.hasNext()) {
                Element itemDevice = deviceListIterator.next();
                if (XmlUtil.getText(itemDevice, "DeviceID") == null) {
                    continue;
                }
                channels.add(CatalogChannelParser.parseItem(deviceIdentification, itemDevice));
            }

            log.info("[收到通道] 设备: {} -> 本批{}个, SN={}, SumNum={}",
                    deviceIdentification, channels.size(), sn, sumNum);

            if (!channels.isEmpty()) {
                int synced = videoChannelService.syncChannelsFromCatalog(deviceIdentification, channels);
                log.info("[通道入库] 设备: {}, 成功同步 {} 个通道", deviceIdentification, synced);
                updateDeviceChannelCount(deviceIdentification);
                // 记录本次 Catalog 同步时间到设备 protocol_config（按需合并，不抹其他字段）
                try {
                    videoDeviceService.patchProtocolConfig(deviceIdentification,
                            VideoDeviceProtocolConfig.builder().catalogSyncTime(DateUtil.now()).build());
                } catch (Exception e) {
                    log.warn("[Catalog同步时间回写] 设备: {}, 更新失败: {}", deviceIdentification, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[收到通道] 解析异常, 设备: {}", deviceIdentification, e);
        }
    }

    /**
     * 更新设备的通道计数
     */
    private void updateDeviceChannelCount(String deviceIdentification) {
        try {
            int count = videoChannelService.countByDeviceIdentification(deviceIdentification);
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setDeviceIdentification(deviceIdentification);
            updateVO.setChannelCount(count);
            videoDeviceService.updateDeviceInfo(updateVO);
            log.info("[通道计数] 设备: {}, 通道数: {}", deviceIdentification, count);
        } catch (Exception e) {
            log.warn("[通道计数] 设备: {}, 更新失败: {}", deviceIdentification, e.getMessage());
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element rootElement) {

    }
}
