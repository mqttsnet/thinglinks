package com.mqttsnet.thinglinks.video.gb28181.cmd;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfig;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import com.mqttsnet.thinglinks.video.gb28181.TenantSipConfigProvider;
import com.mqttsnet.thinglinks.video.gb28181.event.publisher.SipCommandEventPublisher;
import com.mqttsnet.thinglinks.video.gb28181.event.source.SipCommandSentEventSource;
import com.mqttsnet.thinglinks.video.gb28181.protocol.GbProtocolAdapter;
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
 * 设备查询命令发送器。
 * 负责构建和发送 GB/T 28181 查询类 SIP MESSAGE 命令，包括：
 * - 设备目录查询（Catalog）
 * - 设备信息查询（DeviceInfo）
 * - 设备状态查询（DeviceStatus）
 * - 设备录像查询（RecordInfo）
 * - 预置位查询（PresetQuery）
 * <p>
 * 所有查询通过 GbProtocolAdapter 适配不同协议版本的 XML 差异。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCommander {

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
     * 发送设备目录查询
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void catalogQuery(String deviceIdentification,
                             String deviceIp, int devicePort,
                             String transport,
                             GbProtocolVersionEnum gbVersion) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildCatalogQueryXml(sn, deviceIdentification);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "CATALOG");
    }

    /**
     * 发送设备信息查询
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void deviceInfoQuery(String deviceIdentification,
                                String deviceIp, int devicePort,
                                String transport,
                                GbProtocolVersionEnum gbVersion) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildDeviceInfoQueryXml(sn, deviceIdentification);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "DEVICE_INFO");
    }

    /**
     * 发送设备状态查询
     *
     * @param deviceIdentification   设备国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     */
    public void deviceStatusQuery(String deviceIdentification,
                                  String deviceIp, int devicePort,
                                  String transport,
                                  GbProtocolVersionEnum gbVersion) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildDeviceStatusQueryXml(sn, deviceIdentification);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "DEVICE_STATUS");
    }

    /**
     * 发送设备录像查询
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本
     * @param startTime  查询开始时间（ISO 格式，如 2026-03-30T00:00:00）
     * @param endTime    查询结束时间
     */
    public void recordInfoQuery(String deviceIdentification, String channelIdentification,
                                String deviceIp, int devicePort,
                                String transport,
                                GbProtocolVersionEnum gbVersion,
                                String startTime, String endTime) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildRecordInfoQueryXml(sn, deviceIdentification, channelIdentification, startTime, endTime);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "RECORD_INFO");
    }

    /**
     * 发送预置位查询
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     */
    public void presetQuery(String deviceIdentification, String channelIdentification,
                            String deviceIp, int devicePort,
                            String transport) {
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                <CmdType>PresetQuery</CmdType>
                <SN>%s</SN>
                <DeviceID>%s</DeviceID>
                </Query>""".formatted(sn, channelIdentification);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "PRESET_QUERY");
    }

    /**
     * 发送带录像类型过滤的录像查询（GB2022 增强）。
     * 允许按录像类型（定时/报警/手动）过滤查询结果。
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     * @param startTime  查询开始时间（ISO 8601 格式）
     * @param endTime    查询结束时间
     * @param recordType 录像类型（time/alarm/manual/all）
     */
    public void recordInfoQueryByType(String deviceIdentification, String channelIdentification,
                                      String deviceIp, int devicePort,
                                      String transport,
                                      GbProtocolVersionEnum gbVersion,
                                      String startTime, String endTime,
                                      String recordType) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildRecordInfoQueryXml(sn, deviceIdentification, channelIdentification, startTime, endTime, recordType);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "RECORD_INFO_BY_TYPE");
    }

    /**
     * 发送 BusinessGroup 业务分组目录查询（GB2022 新增）。
     * 仅查询属于指定业务分组下的通道列表。
     *
     * @param deviceIdentification        设备国标编号
     * @param deviceIp        设备 IP
     * @param devicePort      设备 SIP 端口
     * @param transport       传输协议
     * @param gbVersion       协议版本 {@link GbProtocolVersionEnum}
     * @param businessGroupId 业务分组编号
     */
    public void businessGroupCatalogQuery(String deviceIdentification,
                                          String deviceIp, int devicePort,
                                          String transport,
                                          GbProtocolVersionEnum gbVersion,
                                          String businessGroupId) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildBusinessGroupCatalogQueryXml(sn, deviceIdentification, businessGroupId);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "BUSINESS_GROUP_CATALOG");
    }

    /**
     * 发送配置下载查询。
     * 通过 {@link GbProtocolAdapter} 构建版本适配的配置下载 XML。
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param gbVersion  协议版本 {@link GbProtocolVersionEnum}
     * @param configType 配置类型（BasicParam/VideoParamOpt/SVACEncodeConfig/SVACDecodeConfig）
     */
    public void configDownloadQuery(String deviceIdentification, String channelIdentification,
                                    String deviceIp, int devicePort,
                                    String transport,
                                    GbProtocolVersionEnum gbVersion,
                                    String configType) {
        var adapter = gbProtocolAdapterFactory.getAdapterOrDefault(gbVersion);
        String sn = String.valueOf(snGenerator.getAndIncrement());
        String xml = adapter.buildConfigDownloadXml(sn, channelIdentification, configType);
        sendQuery(deviceIdentification, deviceIp, devicePort, transport, xml, "CONFIG_DOWNLOAD");
    }

    /**
     * 发送配置下载查询（向后兼容，不指定协议版本，使用 GB2016 默认）
     *
     * @param deviceIdentification   设备国标编号
     * @param channelIdentification  通道国标编号
     * @param deviceIp   设备 IP
     * @param devicePort 设备 SIP 端口
     * @param transport  传输协议
     * @param configType 配置类型（BasicParam/VideoParamOpt 等）
     */
    public void configDownloadQuery(String deviceIdentification, String channelIdentification,
                                    String deviceIp, int devicePort,
                                    String transport, String configType) {
        configDownloadQuery(deviceIdentification, channelIdentification, deviceIp, devicePort, transport,
                GbProtocolVersionEnum.GB2016, configType);
    }

    /**
     * 通用查询命令发送
     */
    private void sendQuery(String deviceIdentification,
                           String deviceIp, int devicePort,
                           String transport, String xmlContent,
                           String queryType) {
        try {
            TenantSipConfig tenantConfig = tenantSipConfigProvider.resolve();

            CallIdHeader callId = sipSender.getNewCallIdHeader(tenantConfig.getEffectiveHost(), transport);
            Request request = sipMessageBuilder.buildMessageRequest(
                    deviceIdentification, deviceIp, devicePort, transport, xmlContent, callId, tenantConfig);

            sipSender.transmitRequest(tenantConfig.getEffectiveHost(), request);

            log.info("发送{}查询: deviceIdentification={}, callId={}", queryType, deviceIdentification, callId.getCallId());

            sipCommandEventPublisher.publishSipCommandSentEvent(
                    SipCommandSentEventSource.builder()
                            .commandType(queryType)
                            .deviceIdentification(deviceIdentification)
                            .channelIdentification(null)
                            .callId(callId.getCallId())
                            .sipMethod("MESSAGE")
                            .build());
        } catch (Exception e) {
            log.error("发送{}查询失败: deviceIdentification={}", queryType, deviceIdentification, e);
            throw BizException.wrap("发送" + queryType + "查询失败: " + e.getMessage());
        }
    }
}
