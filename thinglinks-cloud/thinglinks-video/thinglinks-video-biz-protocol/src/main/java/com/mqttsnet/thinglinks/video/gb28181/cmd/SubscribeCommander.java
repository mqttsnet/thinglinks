package com.mqttsnet.thinglinks.video.gb28181.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipCommandEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import com.mqttsnet.thinglinks.video.gb28181.protocol.GbProtocolAdapterFactory;
import com.mqttsnet.thinglinks.video.gb28181.transmit.SIPSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * 订阅命令发送器。
 * 负责构建和发送 GB/T 28181 订阅类 SIP SUBSCRIBE 命令，包括：
 * - 目录订阅（Catalog）
 * - 告警订阅（Alarm）
 * - 移动位置订阅（MobilePosition）
 * <p>
 * 支持订阅和取消订阅（expires=0）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeCommander {

    private final SipMessageBuilder sipMessageBuilder;
    private final SIPSender sipSender;
    private final TenantSipConfigProvider tenantSipConfigProvider;
    private final GbProtocolAdapterFactory gbProtocolAdapterFactory;
    private final SipCommandEventPublisher sipCommandEventPublisher;

    /**
     * SN 序列号生成器
     */
    private final AtomicInteger snGenerator = new AtomicInteger(1);

    /**
     * 默认订阅有效期（秒）
     */
    private static final int DEFAULT_SUBSCRIBE_EXPIRES = 3600;

    /**
     * 发送目录订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     * @param expires    订阅有效期（秒，0 表示取消订阅）
     */
    public void catalogSubscribe(String deviceIdentification,
                                 String deviceIp, int devicePort,
                                 String transport,
                                 GbProtocolVersionEnum gbVersion,
                                 int expires) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildCatalogSubscribeXml(sn, deviceIdentification);
        sendSubscribe(deviceIdentification, deviceIp, devicePort, transport, xml,
                expires, "Catalog", "CATALOG_SUBSCRIBE");
    }

    /**
     * 发送目录订阅（使用默认有效期）
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void catalogSubscribe(String deviceIdentification,
                                 String deviceIp, int devicePort,
                                 String transport,
                                 GbProtocolVersionEnum gbVersion) {
        catalogSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, DEFAULT_SUBSCRIBE_EXPIRES);
    }

    /**
     * 取消目录订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void catalogUnsubscribe(String deviceIdentification,
                                   String deviceIp, int devicePort,
                                   String transport,
                                   GbProtocolVersionEnum gbVersion) {
        catalogSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, 0);
    }

    /**
     * 发送告警订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     * @param expires    订阅有效期（秒，0 表示取消订阅）
     */
    public void alarmSubscribe(String deviceIdentification,
                               String deviceIp, int devicePort,
                               String transport,
                               GbProtocolVersionEnum gbVersion,
                               int expires) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildAlarmSubscribeXml(sn, deviceIdentification);
        sendSubscribe(deviceIdentification, deviceIp, devicePort, transport, xml,
                expires, "Alarm", "ALARM_SUBSCRIBE");
    }

    /**
     * 发送告警订阅（使用默认有效期）
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void alarmSubscribe(String deviceIdentification,
                               String deviceIp, int devicePort,
                               String transport,
                               GbProtocolVersionEnum gbVersion) {
        alarmSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, DEFAULT_SUBSCRIBE_EXPIRES);
    }

    /**
     * 取消告警订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void alarmUnsubscribe(String deviceIdentification,
                                 String deviceIp, int devicePort,
                                 String transport,
                                 GbProtocolVersionEnum gbVersion) {
        alarmSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, 0);
    }

    /**
     * 发送移动位置订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     * @param interval   位置上报间隔（秒）
     * @param expires    订阅有效期（秒，0 表示取消订阅）
     */
    public void mobilePositionSubscribe(String deviceIdentification,
                                        String deviceIp, int devicePort,
                                        String transport,
                                        GbProtocolVersionEnum gbVersion,
                                        int interval, int expires) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildMobilePositionSubscribeXml(sn, deviceIdentification, interval);
        sendSubscribe(deviceIdentification, deviceIp, devicePort, transport, xml,
                expires, "presence", "MOBILE_POSITION_SUBSCRIBE");
    }

    /**
     * 发送移动位置订阅（使用默认有效期）
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     * @param interval   位置上报间隔（秒）
     */
    public void mobilePositionSubscribe(String deviceIdentification,
                                        String deviceIp, int devicePort,
                                        String transport,
                                        GbProtocolVersionEnum gbVersion,
                                        int interval) {
        mobilePositionSubscribe(deviceIdentification, deviceIp, devicePort, transport,
                gbVersion, interval, DEFAULT_SUBSCRIBE_EXPIRES);
    }

    /**
     * 取消移动位置订阅
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     */
    public void mobilePositionUnsubscribe(String deviceIdentification,
                                          String deviceIp, int devicePort,
                                          String transport,
                                          GbProtocolVersionEnum gbVersion) {
        mobilePositionSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, 0, 0);
    }

    /**
     * 发送录像状态订阅（GB2022 新增）。
     * 订阅设备的录像状态变更事件，当设备开始/停止录像时收到 NOTIFY 通知。
     * 仅 GB2022 设备支持此订阅类型，GB2016 设备调用时会发送空 XML（无效果）。
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     * @param expires    订阅有效期（秒，0 表示取消订阅）
     */
    public void recordStatusSubscribe(String deviceIdentification,
                                      String deviceIp, int devicePort,
                                      String transport,
                                      GbProtocolVersionEnum gbVersion,
                                      int expires) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildRecordStatusSubscribeXml(sn, deviceIdentification);
        if (xml == null || xml.isBlank()) {
            log.warn("当前协议版本({})不支持录像状态订阅，跳过: deviceIdentification={}", gbVersion, deviceIdentification);
            return;
        }
        sendSubscribe(deviceIdentification, deviceIp, devicePort, transport, xml,
                expires, "RecordStatus", "RECORD_STATUS_SUBSCRIBE");
    }

    /**
     * 发送录像状态订阅（使用默认有效期，GB2022 新增）
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     */
    public void recordStatusSubscribe(String deviceIdentification,
                                      String deviceIp, int devicePort,
                                      String transport,
                                      GbProtocolVersionEnum gbVersion) {
        recordStatusSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, DEFAULT_SUBSCRIBE_EXPIRES);
    }

    /**
     * 取消录像状态订阅（GB2022 新增）
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     */
    public void recordStatusUnsubscribe(String deviceIdentification,
                                        String deviceIp, int devicePort,
                                        String transport,
                                        GbProtocolVersionEnum gbVersion) {
        recordStatusSubscribe(deviceIdentification, deviceIp, devicePort, transport, gbVersion, 0);
    }

    /**
     * 通用订阅命令发送
     */
    private void sendSubscribe(String deviceIdentification,
                               String deviceIp, int devicePort,
                               String transport, String xmlContent,
                               int expires, String eventType,
                               String subscribeType) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            Request request = sipMessageBuilder.buildSubscribeRequest(
                    deviceIdentification, deviceIp, devicePort, transport,
                    xmlContent, callId, expires, eventType, tenantConfig);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            String action = expires > 0 ? "订阅" : "取消订阅";
            log.info("发送{}({}): deviceIdentification={}, expires={}, callId={}",
                    subscribeType, action, deviceIdentification, expires, callId.getCallId());

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType(subscribeType)
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(null)
                            .callId(callId.getCallId())
                            .sipMethod("SUBSCRIBE")
                            .build());
        } catch (Exception e) {
            log.error("发送{}失败: deviceIdentification={}", subscribeType, deviceIdentification, e);
            throw BizException.wrap("发送" + subscribeType + "失败: " + e.getMessage());
        }
    }
}
