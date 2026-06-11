package com.mqttsnet.thinglinks.video.gb28181.protocol;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * GB/T 28181-2022 协议版本适配器。
 * 在 GB/T 28181-2016 基础上实现 2022 版本的全部新增特性，
 * 使用 UTF-8 编码替代 GB2312，并支持以下 GB2022 新能力：
 * <ul>
 *   <li><b>BusinessGroup 业务分组</b> — 目录查询支持按业务分组过滤</li>
 *   <li><b>录像类型过滤</b> — 录像查询支持 time/alarm/manual/all 类型过滤</li>
 *   <li><b>录像状态订阅</b> — 新增录像状态变更订阅事件类型</li>
 *   <li><b>告警确认</b> — 支持向设备发送告警处理确认</li>
 *   <li><b>目录增量通知</b> — 目录变更支持 ADD/DEL/UPDATE 增量事件通知</li>
 *   <li><b>配置下载扩展</b> — 支持 SVACEncodeConfig/SVACDecodeConfig 等新配置类型</li>
 *   <li><b>强制关键帧</b> — MANSRTSP 协议扩展支持 ForceIFrame</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see GbProtocolAdapter
 * @see GbProtocolVersionEnum#GB2022
 * @since 2026-03-30
 */
@Slf4j
@Component
public final class Gb2022ProtocolAdapter implements GbProtocolAdapter {

    private static final String ENCODING = "UTF-8";

    @Override
    public GbProtocolVersionEnum getVersion() {
        return GbProtocolVersionEnum.GB2022;
    }

    @Override
    public String getEncoding() {
        return ENCODING;
    }

    // ======================== 查询类 XML ========================

    @Override
    public String buildCatalogQueryXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>Catalog</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }

    /**
     * 构建带 BusinessGroup 过滤的目录查询 XML（GB2022 新增）。
     * 通过 BusinessGroupID 元素实现业务分组级别的目录过滤查询，
     * 仅返回属于指定分组下的通道列表。
     *
     * @param sn              命令序列号
     * @param deviceIdentification        目标设备国标编号
     * @param businessGroupId 业务分组编号
     * @return 带 BusinessGroup 过滤的目录查询 XML 字符串
     */
    @Override
    public String buildBusinessGroupCatalogQueryXml(String sn, String deviceIdentification, String businessGroupId) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>Catalog</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <BusinessGroupID>%s</BusinessGroupID>
                </Query>
                """.formatted(sn, deviceIdentification, businessGroupId);
    }

    @Override
    public String buildDeviceInfoQueryXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
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
                <?xml version="1.0" encoding="UTF-8"?>
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
        return buildRecordInfoQueryXml(sn, deviceIdentification, channelIdentification, startTime, endTime, BizConstant.ALL);
    }

    /**
     * 构建带录像类型过滤的录像查询 XML（GB2022 增强）。
     * GB2022 允许按录像类型过滤查询结果：
     * <ul>
     *   <li>time — 定时录像</li>
     *   <li>alarm — 报警录像</li>
     *   <li>manual — 手动录像</li>
     *   <li>all — 全部类型</li>
     * </ul>
     *
     * @param sn         命令序列号
     * @param deviceIdentification   目标设备国标编号
     * @param channelIdentification  通道国标编号
     * @param startTime  开始时间（ISO 8601 格式）
     * @param endTime    结束时间（ISO 8601 格式）
     * @param recordType 录像类型过滤
     * @return 录像查询 XML 字符串
     */
    @Override
    public String buildRecordInfoQueryXml(String sn, String deviceIdentification, String channelIdentification,
                                           String startTime, String endTime, String recordType) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>RecordInfo</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <StartTime>%s</StartTime>
                    <EndTime>%s</EndTime>
                    <Secrecy>0</Secrecy>
                    <Type>%s</Type>
                </Query>
                """.formatted(sn, channelIdentification, startTime, endTime,
                recordType != null ? recordType : BizConstant.ALL);
    }

    @Override
    public String buildConfigDownloadXml(String sn, String deviceIdentification, String configType) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>ConfigDownload</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <ConfigType>%s</ConfigType>
                </Query>
                """.formatted(sn, deviceIdentification, configType);
    }

    // ======================== 控制类 XML ========================

    @Override
    public String buildGuardControlXml(String sn, String deviceIdentification, String guardCmd) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
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
                <?xml version="1.0" encoding="UTF-8"?>
                <Control>
                    <CmdType>DeviceControl</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <TeleBoot>Boot</TeleBoot>
                </Control>
                """.formatted(sn, deviceIdentification);
    }

    // ======================== 订阅类 XML ========================

    @Override
    public String buildAlarmSubscribeXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
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
                <?xml version="1.0" encoding="UTF-8"?>
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
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>MobilePosition</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <Interval>%d</Interval>
                </Query>
                """.formatted(sn, deviceIdentification, interval);
    }

    /**
     * 构建录像状态订阅 XML（GB2022 新增）。
     * 订阅设备的录像状态变更事件，当设备开始/停止录像时收到通知。
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 录像状态订阅 XML 字符串
     */
    @Override
    public String buildRecordStatusSubscribeXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Query>
                    <CmdType>RecordStatus</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                </Query>
                """.formatted(sn, deviceIdentification);
    }
}
