package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.cmd;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceMobilePosition;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.NotifyMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceMobilePositionService;
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
import java.time.LocalDateTime;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

/**
 * 移动位置通知处理器。
 * 解析设备发送的 MobilePosition 通知，保存位置信息。
 *
 * GB/T 28181-2016 Section 9.4: 移动设备位置通知包含经纬度、海拔、速度、方向等信息。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class MobilePositionNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.MOBILE_POSITION.getValue();

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private VideoDeviceMobilePositionService mobilePositionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfo, Element element) {
        if (deviceInfo == null) {
            log.warn("[MobilePosition] 设备信息为空，忽略位置通知");
            return;
        }

        // 回复 200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[MobilePosition回复失败] 设备: {}, 错误: {}", deviceInfo.getDeviceIdentification(), e.getMessage());
        }

        String deviceIdentification = getText(element, "DeviceID");
        log.info("[MobilePosition] 设备: {}, 收到位置通知", deviceIdentification);

        // 解析位置信息
        VideoDeviceMobilePosition position = parsePositionFromXml(element, deviceInfo.getDeviceIdentification());

        // 持久化位置信息
        mobilePositionService.savePosition(position);

        log.debug("[MobilePosition] 设备: {}, 经度={}, 纬度={}, 海拔={}",
                deviceIdentification, position.getLongitude(), position.getLatitude(), position.getAltitude());
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {
        // 级联场景下的移动位置处理（预留）
        log.info("[MobilePosition-Platform] 收到平台级联位置通知");
    }

    /**
     * 从 XML 元素解析位置信息
     *
     * @param element  XML 根元素
     * @param deviceIdentification 设备国标编号
     * @return 位置实体
     */
    private VideoDeviceMobilePosition parsePositionFromXml(Element element, String deviceIdentification) {
        VideoDeviceMobilePosition.VideoDeviceMobilePositionBuilder builder = VideoDeviceMobilePosition.builder()
                .deviceIdentification(deviceIdentification);

        // 经度 / 纬度 / 海拔 / 速度 / 方向 —— 一律用 NumberUtil.parseDouble，解析失败返回 null 跳过
        Double lng = NumberUtil.parseDouble(getText(element, "Longitude"), null);
        if (lng != null) builder.longitude(lng);

        Double lat = NumberUtil.parseDouble(getText(element, "Latitude"), null);
        if (lat != null) builder.latitude(lat);

        Double altitude = NumberUtil.parseDouble(getText(element, "Altitude"), null);
        if (altitude != null) builder.altitude(altitude);

        Double speed = NumberUtil.parseDouble(getText(element, "Speed"), null);
        if (speed != null) builder.speed(speed);

        Double direction = NumberUtil.parseDouble(getText(element, "Direction"), null);
        if (direction != null) builder.direction(direction);

        // 上报时间（GB28181 ISO 格式 2024-04-19T10:00:00）。设备上报格式不一致时记录警告，
        // 用当前时间兜底（位置上报强调实时性，丢弃会失真）。
        String time = getText(element, "Time");
        LocalDateTime reportTime = LocalDateTime.now();
        if (StrUtil.isNotBlank(time)) {
            try {
                reportTime = DateUtil.parseLocalDateTime(time, "yyyy-MM-dd'T'HH:mm:ss");
            } catch (Exception e) {
                log.warn("[MobilePosition] 设备 Time 字段格式非 ISO-8601，用当前时间兜底: raw={}, err={}", time, e.getMessage());
            }
        }
        builder.reportTime(reportTime);

        return builder.build();
    }
}
