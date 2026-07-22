package com.mqttsnet.thinglinks.video.gb28181.protocol;

import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * GB/T 28181-2016 协议版本适配器。
 * 按照 GB/T 28181-2016 标准构建 SIP 消息中的 XML 内容，
 * 大多数现有设备仍使用此版本。使用 GB2312 字符编码。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see GbProtocolAdapter
 * @see GbProtocolVersionEnum#GB2016
 * @since 2026-03-30
 */
@Slf4j
@Component
public final class Gb2016ProtocolAdapter implements GbProtocolAdapter {

    private static final String ENCODING = "GB2312";

    @Override
    public GbProtocolVersionEnum getVersion() {
        return GbProtocolVersionEnum.GB2016;
    }

    @Override
    public String getEncoding() {
        return ENCODING;
    }

    @Override
    public String buildCatalogQueryXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>Catalog</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildDeviceInfoQueryXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>DeviceInfo</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildDeviceStatusQueryXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>DeviceStatus</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildRecordInfoQueryXml(String sn, String deviceIdentification, String channelIdentification,
                                          String startTime, String endTime) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>RecordInfo</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <StartTime>%s</StartTime>
                    <EndTime>%s</EndTime>
                    <Secrecy>0</Secrecy>
                    <Type>all</Type>
                </Query>
                """.formatted(sn, channelIdentification, startTime, endTime);
    }

    @Override
    public String buildConfigDownloadXml(String sn, String deviceIdentification, String configType) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>ConfigDownload</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <ConfigType>%s</ConfigType>
                </Query>
                """.formatted(sn, deviceIdentification, configType);
    }

    @Override
    public String buildGuardControlXml(String sn, String deviceIdentification, String guardCmd) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Control>
                    <CmdType>DeviceControl</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <GuardCmd>%s</GuardCmd>
                </Control>
                """.formatted(sn, deviceIdentification, guardCmd);
    }

    @Override
    public String buildTeleBootXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Control>
                    <CmdType>DeviceControl</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <TeleBoot>Boot</TeleBoot>
                </Control>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildAlarmSubscribeXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>Alarm</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <StartAlarmPriority>0</StartAlarmPriority>
                    <EndAlarmPriority>0</EndAlarmPriority>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildCatalogSubscribeXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>Catalog</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    @Override
    public String buildMobilePositionSubscribeXml(String sn, String deviceIdentification, int interval) {
        return """
                <?xml version="1.0" encoding="GB2312"?>
                <Query>
                    <CmdType>MobilePosition</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <Interval>%d</Interval>
                </Query>
                """.formatted(sn, deviceIdentification, interval);
    }
}
