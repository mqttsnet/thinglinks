package com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.cmd;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.config.VideoDeviceProtocolConfig;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.CmdTypeEnum;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.SIPRequestProcessorParent;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.IMessageHandler;
import com.mqttsnet.thinglinks.video.gb28181.transmit.request.impl.message.notify.NotifyMessageHandler;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
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
import java.text.ParseException;

import static com.mqttsnet.thinglinks.video.utils.gb28181.XmlUtil.getText;

/**
 * 设备保活通知处理器。
 * 处理设备发送的 Keepalive 心跳消息，更新设备在线状态和最近心跳时间。
 * 同时同步更新 Redis 缓存，保证缓存与 DB 一致。
 *
 * GB/T 28181-2016 Section 9.4: 设备通过周期性发送 Keepalive 消息维持注册状态。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class KeepaliveNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final String cmdType = CmdTypeEnum.KEEPALIVE.getValue();

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Autowired
    private VideoCacheDataHelper videoCacheDataHelper;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, VideoDeviceResultVO deviceInfo, Element element) {
        if (deviceInfo == null) {
            log.warn("[Keepalive] 设备信息为空，忽略心跳");
            return;
        }

        // 回复 200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error("[Keepalive回复失败] 设备: {}, 错误: {}", deviceInfo.getDeviceIdentification(), e.getMessage());
        }

        // 解析状态 —— GB/T 28181-2016 §9.4 定义 OK/ERROR，非标设备偶尔见 OFF
        String status = getText(element, "Status");
        String deviceIdentification = deviceInfo.getDeviceIdentification();
        // OFF 直接视为离线；其他值（包括 ERROR）视为在线但有内部故障
        boolean isOnline = !"OFF".equalsIgnoreCase(status);
        boolean isOk = "OK".equalsIgnoreCase(status);
        String now = DateUtil.now();

        if (isOk) {
            log.info("[Keepalive] 设备: {}, Status=OK", deviceIdentification);
        } else {
            // Status != OK 会在日志里显著标注，方便运维排查（常见 ERROR 带 DeviceErrorStatusList）
            log.warn("[Keepalive] 设备: {}, Status={} (非OK心跳)", deviceIdentification, status);
        }

        // 1. 更新 DB：只写 SIP keepalive 实际带过来的字段（lastKeepaliveTime / onlineStatus），
        //    其余字段保留 DB 原值，避免覆盖用户在 UI 维护的配置项；按 id 更新。
        VideoDeviceUpdateVO updateVO = new VideoDeviceUpdateVO();
        updateVO.setId(deviceInfo.getId());
        updateVO.setLastKeepaliveTime(now);
        updateVO.setOnlineStatus(isOnline);
        videoDeviceService.updateDeviceInfo(updateVO);

        // 2. 同步更新 Redis 缓存
        try {
            VideoDeviceCacheVO cacheVO = videoCacheDataHelper.getDeviceInfo(deviceIdentification);
            if (cacheVO != null) {
                cacheVO.setLastKeepaliveTime(now);
                cacheVO.setOnlineStatus(isOnline);
                videoCacheDataHelper.setDeviceInfo(cacheVO);
            }
        } catch (Exception e) {
            log.warn("[Keepalive] 设备: {}, Redis缓存更新失败: {}", deviceIdentification, e.getMessage());
        }

        // 3. 把心跳 Status / 错误详情落到 protocol_config（按需合并，不影响其他字段）
        try {
            patchKeepaliveToProtocolConfig(deviceIdentification, status, isOk, element, now);
        } catch (Exception e) {
            log.warn("[Keepalive] 设备: {}, 心跳详情回写 protocol_config 失败: {}", deviceIdentification, e.getMessage());
        }
    }

    /**
     * 写心跳元数据到 {@code protocol_config}：
     * <ul>
     *   <li>{@code lastKeepaliveStatus} - 每次都更新，正常/异常都记</li>
     *   <li>{@code lastKeepaliveErrorDetail} / {@code lastKeepaliveErrorTime} - 仅 Status != OK 时更新</li>
     * </ul>
     * 正常心跳不记 Info 细节，避免高频写 DB + 数据冗余。
     */
    private void patchKeepaliveToProtocolConfig(String deviceIdentification, String status,
                                                boolean isOk, Element root, String now) {
        VideoDeviceProtocolConfig.VideoDeviceProtocolConfigBuilder patch = VideoDeviceProtocolConfig.builder()
                .lastKeepaliveStatus(status);
        if (!isOk) {
            String infoJson = serializeInfoElement(root.element("Info"));
            patch.lastKeepaliveErrorDetail(infoJson).lastKeepaliveErrorTime(now);
        }
        videoDeviceService.patchProtocolConfig(deviceIdentification, patch.build());
    }

    /**
     * 把 {@code <Info>} 子节点序列化成紧凑 JSON 字符串。
     * <p>直接用 Hutool 的 {@link JSONUtil#xmlToJson}，自动处理嵌套、同名子元素合并为数组，
     * 不手写递归。典型内容：{@code <DeviceErrorStatusList><DeviceErrorStatus>...:123</DeviceErrorStatus>}。
     * 解析失败或节点空时返回 null。
     */
    private String serializeInfoElement(Element info) {
        if (info == null) {
            return null;
        }
        try {
            var json = JSONUtil.xmlToJson(info.asXML());
            return json == null || json.isEmpty() ? null : json.toString();
        } catch (Exception e) {
            log.warn("[Keepalive] Info 节点序列化失败，忽略: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, VideoPlatformInfo parentPlatform, Element element) {
        log.info("[Keepalive-Platform] 收到平台级联保活消息");
    }
}
