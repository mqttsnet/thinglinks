package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.cmd;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

import java.text.ParseException;
import java.time.LocalDateTime;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.AlarmMethodEnum;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.AlarmPriorityEnum;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.NotifyMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceAlarmService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 报警通知处理器。
 * 解析设备发送的报警 XML 通知，创建 {@link VideoDeviceAlarm} 实体并持久化。
 * <p>
 * GB/T 28181-2016 Section 9.4: 报警通知包含告警级别、告警方式、告警时间等信息。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class AlarmNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.ALARM.getValue();

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private VideoDeviceAlarmService videoDeviceAlarmService;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfo, Element element) {
        if (deviceInfo == null) {
            log.warn("[Alarm] 设备信息为空，忽略报警通知");
            return;
        }
        if (ContextUtil.getTenantId() == null) {
            log.error("[Alarm] 租户上下文丢失, 设备: {}, 忽略报警通知", deviceInfo.getDeviceIdentification());
            return;
        }

        // 回复 200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[Alarm回复失败] 设备: {}, 错误: {}", deviceInfo.getDeviceIdentification(), e.getMessage());
        }

        String deviceIdentification = getText(element, "DeviceID");
        log.info("[Alarm] 设备: {}, 收到报警通知", deviceIdentification);

        // 解析报警信息并构建实体
        VideoDeviceAlarm alarm = parseAlarmFromXml(element, deviceInfo.getDeviceIdentification());

        // 继承设备的创建者和组织，用于数据权限
        alarm.setCreatedBy(deviceInfo.getCreatedBy());
        alarm.setCreatedOrgId(deviceInfo.getCreatedOrgId());

        // 持久化告警
        videoDeviceAlarmService.saveAlarm(alarm);
        log.info("[Alarm] 设备: {}, 告警已保存, 级别={}, 方式={}",
                deviceIdentification,
                AlarmPriorityEnum.descOf(alarm.getAlarmPriority()),
                AlarmMethodEnum.descOf(alarm.getAlarmMethod()));
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {
        // 级联场景下的报警处理（预留）
        log.info("[Alarm-Platform] 收到平台级联报警通知");
    }

    /**
     * 从 XML 元素解析报警信息
     *
     * @param element              XML 根元素
     * @param deviceIdentification 设备国标编号
     * @return 报警实体
     */
    private VideoDeviceAlarm parseAlarmFromXml(Element element, String deviceIdentification) {
        VideoDeviceAlarm.VideoDeviceAlarmBuilder builder = VideoDeviceAlarm.builder()
            .deviceIdentification(deviceIdentification)
            .handleStatus(0);

        // 告警级别
        String alarmPriority = getText(element, "AlarmPriority");
        Integer alarmPriorityVal = NumberUtil.parseInt(alarmPriority, null);
        if (alarmPriorityVal != null) {
            builder.alarmPriority(alarmPriorityVal);
        } else if (StrUtil.isNotBlank(alarmPriority)) {
            log.warn("[Alarm] 告警级别解析失败: {}", alarmPriority);
        }

        // 告警方式
        String alarmMethod = getText(element, "AlarmMethod");
        Integer alarmMethodVal = NumberUtil.parseInt(alarmMethod, null);
        if (alarmMethodVal != null) {
            builder.alarmMethod(alarmMethodVal);
        } else if (StrUtil.isNotBlank(alarmMethod)) {
            log.warn("[Alarm] 告警方式解析失败: {}", alarmMethod);
        }

        // 告警时间（GB28181 使用 ISO 形式 2024-04-19T10:00:00，DateUtil.parse 能自动识别）
        String alarmTime = getText(element, "AlarmTime");
        LocalDateTime alarmLdt = null;
        if (StrUtil.isNotBlank(alarmTime)) {
            try {
                alarmLdt = DateUtil.parseLocalDateTime(alarmTime, "yyyy-MM-dd'T'HH:mm:ss");
            } catch (Exception e) {
                log.warn("[Alarm] 告警时间解析失败: {}", alarmTime);
            }
        }
        builder.alarmTime(alarmLdt != null ? alarmLdt : LocalDateTime.now());

        // 告警描述
        String alarmDescription = getText(element, "AlarmDescription");
        if (StrUtil.isNotBlank(alarmDescription)) {
            builder.alarmDescription(alarmDescription);
        }

        // 告警类型
        // GB/T 28181-2016: AlarmType 是条件必选字段，仅在 AlarmMethod=2(设备报警) 或
        // AlarmMethod=6(设备故障报警) 时必填。其他 AlarmMethod 下 AlarmType 为空合法。
        String alarmType = getText(element, "AlarmType");
        Integer alarmTypeVal = NumberUtil.parseInt(alarmType, null);
        if (alarmTypeVal != null) {
            builder.alarmType(alarmTypeVal);
        } else if (StrUtil.isNotBlank(alarmType)) {
            log.warn("[Alarm] 告警类型解析失败: {}", alarmType);
        } else if ("2".equals(alarmMethod) || "6".equals(alarmMethod)) {
            // 条件必填场景下缺失 AlarmType —— 设备不规范或报文异常，记 warn 供排查
            log.warn("[Alarm] 设备: {} 发送 AlarmMethod={} 但缺失 AlarmType（GB28181 规范要求必填），请检查设备配置或上报报文",
                deviceIdentification, alarmMethod);
        }

        // 经纬度
        String longitude = getText(element, "Longitude");
        String latitude = getText(element, "Latitude");
        Double lng = NumberUtil.parseDouble(longitude, null);
        if (lng != null) {
            builder.longitude(lng);
        }
        Double lat = NumberUtil.parseDouble(latitude, null);
        if (lat != null) {
            builder.latitude(lat);
        }

        return builder.build();
    }
}
