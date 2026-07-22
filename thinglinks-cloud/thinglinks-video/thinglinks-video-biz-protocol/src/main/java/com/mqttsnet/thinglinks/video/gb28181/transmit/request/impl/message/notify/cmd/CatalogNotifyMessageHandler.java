package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.cmd;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.CatalogChannelParser;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.NotifyMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoChannelService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 目录变更通知处理器。
 * 处理设备发送的 Catalog 变更通知，支持通道的上线(ON)、下线(OFF)、
 * 新增(ADD)、删除(DEL)、更新(UPDATE) 等事件。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class CatalogNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.CATALOG.getValue();

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private VideoChannelService videoChannelService;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfo, Element element) {
        if (deviceInfo == null) {
            log.warn("[CatalogNotify] 设备信息为空，忽略目录变更通知");
            return;
        }

        // 回复 200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[CatalogNotify回复失败] 设备: {}, 错误: {}", deviceInfo.getDeviceIdentification(), e.getMessage());
        }

        String deviceIdentification = deviceInfo.getDeviceIdentification();

        // 解析 DeviceList
        Element deviceListElement = element.element("DeviceList");
        if (deviceListElement == null) {
            log.warn("[CatalogNotify] 设备: {}, DeviceList 为空", deviceIdentification);
            return;
        }

        Iterator<Element> itemIterator = deviceListElement.elementIterator("Item");
        while (itemIterator.hasNext()) {
            Element itemElement = itemIterator.next();
            try {
                processCatalogItem(deviceIdentification, itemElement);
            } catch (Exception e) {
                log.error("[CatalogNotify] 设备: {}, 处理目录项异常: {}", deviceIdentification, e.getMessage());
            }
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {
        log.info("[CatalogNotify-Platform] 收到平台级联目录变更通知");
    }

    /**
     * 处理单个目录项变更
     */
    private void processCatalogItem(String deviceIdentification, Element itemElement) {
        String channelIdentification = getText(itemElement, "DeviceID");
        if (StrUtil.isBlank(channelIdentification)) {
            return;
        }
        String event = getText(itemElement, "Event");
        String name = getText(itemElement, "Name");

        if (StrUtil.isBlank(event)) {
            log.debug("[CatalogNotify] 设备: {}, 通道: {}, 无 Event 字段，视为全量更新", deviceIdentification, channelIdentification);
            handleAddOrUpdate(deviceIdentification, itemElement);
            updateDeviceChannelCount(deviceIdentification);
            return;
        }

        switch (event.toUpperCase()) {
            case "ON" -> {
                log.info("[CatalogNotify] 设备: {}, 通道: {} ({}) 上线", deviceIdentification, channelIdentification, name);
                videoChannelService.updateOnlineStatus(deviceIdentification, channelIdentification, true);
            }
            case "OFF" -> {
                log.info("[CatalogNotify] 设备: {}, 通道: {} ({}) 下线", deviceIdentification, channelIdentification, name);
                videoChannelService.updateOnlineStatus(deviceIdentification, channelIdentification, false);
            }
            case "ADD" -> {
                log.info("[CatalogNotify] 设备: {}, 通道: {} ({}) 新增", deviceIdentification, channelIdentification, name);
                handleAddOrUpdate(deviceIdentification, itemElement);
                updateDeviceChannelCount(deviceIdentification);
            }
            case "DEL" -> {
                log.info("[CatalogNotify] 设备: {}, 通道: {} ({}) 删除", deviceIdentification, channelIdentification, name);
                videoChannelService.removeByChannelIdentification(deviceIdentification, channelIdentification);
                updateDeviceChannelCount(deviceIdentification);
            }
            case "UPDATE" -> {
                log.info("[CatalogNotify] 设备: {}, 通道: {} ({}) 更新", deviceIdentification, channelIdentification, name);
                handleAddOrUpdate(deviceIdentification, itemElement);
            }
            default -> log.warn("[CatalogNotify] 设备: {}, 通道: {}, 未知事件类型: {}",
                    deviceIdentification, channelIdentification, event);
        }
    }

    /**
     * 处理通道新增或更新（upsert）
     */
    private void handleAddOrUpdate(String deviceIdentification, Element itemElement) {
        VideoChannel channel = CatalogChannelParser.parseItem(deviceIdentification, itemElement);
        videoChannelService.syncChannelsFromCatalog(deviceIdentification, Collections.singletonList(channel));
    }

    /**
     * 更新设备通道计数
     */
    private void updateDeviceChannelCount(String deviceIdentification) {
        try {
            int count = videoChannelService.countByDeviceIdentification(deviceIdentification);
            VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
            updateVO.setDeviceIdentification(deviceIdentification);
            updateVO.setChannelCount(count);
            videoDeviceService.updateDeviceInfo(updateVO);
        } catch (Exception e) {
            log.warn("[CatalogNotify] 更新设备通道计数失败: {}", e.getMessage());
        }
    }
}
