package com.mqttsnet.thinglinks.video.gb28181.protocol;

import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;

/**
 * Description:
 * GB/T 28181 协议版本适配器接口。
 * 处理不同协议版本（2016/2022）之间的 SIP 消息构建和解析差异，
 * 业务层通过此接口统一操作，无需关心具体版本。
 * <p>
 * 使用 sealed 接口限定实现类，确保类型安全。
 * <p>
 * GB2022 相对 GB2016 的主要扩展：
 * <ul>
 *   <li>目录查询支持 BusinessGroup 业务分组过滤</li>
 *   <li>录像查询支持按类型过滤（alarm/manual/all）</li>
 *   <li>订阅机制增强，支持录像状态订阅</li>
 *   <li>通道编码语义扩展</li>
 *   <li>NOTIFY 推送支持目录变更增量通知</li>
 *   <li>告警查询支持更细粒度的类型过滤</li>
 *   <li>配置下载支持更多配置类型</li>
 * </ul>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @see Gb2016ProtocolAdapter
 * @see Gb2022ProtocolAdapter
 * @see GbProtocolVersionEnum
 * @since 2026-03-30
 */
public sealed interface GbProtocolAdapter
        permits Gb2016ProtocolAdapter, Gb2022ProtocolAdapter {

    /**
     * 获取当前适配器对应的协议版本
     *
     * @return 协议版本枚举 {@link GbProtocolVersionEnum}
     */
    GbProtocolVersionEnum getVersion();

    /**
     * 获取当前适配器使用的 XML 编码字符集
     *
     * @return 字符集名称（GB2312 或 UTF-8）
     */
    String getEncoding();

    // ======================== 查询类 XML 构建 ========================

    /**
     * 构建设备目录查询 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 目录查询 XML 字符串
     */
    String buildCatalogQueryXml(String sn, String deviceIdentification);

    /**
     * 构建业务分组目录查询 XML（GB2022 新增）。
     * GB2016 不支持此功能，默认回退到普通目录查询。
     *
     * @param sn              命令序列号
     * @param deviceIdentification        目标设备国标编号
     * @param businessGroupId 业务分组编号（GB2022 新增字段）
     * @return 带 BusinessGroup 过滤的目录查询 XML 字符串
     */
    default String buildBusinessGroupCatalogQueryXml(String sn, String deviceIdentification, String businessGroupId) {
        return buildCatalogQueryXml(sn, deviceIdentification);
    }

    /**
     * 构建设备信息查询 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 设备信息查询 XML 字符串
     */
    String buildDeviceInfoQueryXml(String sn, String deviceIdentification);

    /**
     * 构建设备状态查询 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 设备状态查询 XML 字符串
     */
    String buildDeviceStatusQueryXml(String sn, String deviceIdentification);

    /**
     * 构建录像查询 XML
     *
     * @param sn        命令序列号
     * @param deviceIdentification  目标设备国标编号
     * @param channelIdentification 通道国标编号
     * @param startTime 开始时间（ISO 8601 格式，如 2026-03-30T00:00:00）
     * @param endTime   结束时间（ISO 8601 格式，如 2026-03-30T23:59:59）
     * @return 录像查询 XML 字符串
     */
    String buildRecordInfoQueryXml(String sn, String deviceIdentification, String channelIdentification,
                                   String startTime, String endTime);

    /**
     * 构建按类型过滤的录像查询 XML（GB2022 增强）。
     * GB2016 忽略 recordType 参数，回退到标准录像查询。
     *
     * @param sn         命令序列号
     * @param deviceIdentification   目标设备国标编号
     * @param channelIdentification  通道国标编号
     * @param startTime  开始时间（ISO 8601 格式）
     * @param endTime    结束时间（ISO 8601 格式）
     * @param recordType 录像类型过滤（time=定时/alarm=报警/manual=手动/all=全部）
     * @return 带类型过滤的录像查询 XML 字符串
     */
    default String buildRecordInfoQueryXml(String sn, String deviceIdentification, String channelIdentification,
                                           String startTime, String endTime, String recordType) {
        return buildRecordInfoQueryXml(sn, deviceIdentification, channelIdentification, startTime, endTime);
    }

    /**
     * 构建配置下载查询 XML
     *
     * @param sn         命令序列号
     * @param deviceIdentification   目标设备国标编号
     * @param configType 配置类型（BasicParam/VideoParamOpt/SVACEncodeConfig/SVACDecodeConfig）
     * @return 配置下载查询 XML 字符串
     */
    String buildConfigDownloadXml(String sn, String deviceIdentification, String configType);

    // ======================== 控制类 XML 构建 ========================

    /**
     * 构建布防/撤防控制 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @param guardCmd 布防命令（SetGuard 或 ResetGuard）
     * @return 布防控制 XML 字符串
     */
    String buildGuardControlXml(String sn, String deviceIdentification, String guardCmd);

    /**
     * 构建设备远程重启控制 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 重启控制 XML 字符串
     */
    String buildTeleBootXml(String sn, String deviceIdentification);

    /**
     * 构建录像控制 XML（GB2022 新增）。
     * 用于控制设备开始/停止录像。
     *
     * @param sn        命令序列号
     * @param deviceIdentification  目标设备国标编号
     * @param recordCmd 录像命令（Record 或 StopRecord）
     * @return 录像控制 XML 字符串
     */
    default String buildRecordControlXml(String sn, String deviceIdentification, String recordCmd) {
        return """
                <?xml version="1.0" encoding="%s"?>
                <Control>
                    <CmdType>DeviceControl</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <RecordCmd>%s</RecordCmd>
                </Control>
                """.formatted(getEncoding(), sn, deviceIdentification, recordCmd);
    }

    // ======================== 订阅类 XML 构建 ========================

    /**
     * 构建告警订阅 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 告警订阅 XML 字符串
     */
    String buildAlarmSubscribeXml(String sn, String deviceIdentification);

    /**
     * 构建目录订阅 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 目录订阅 XML 字符串
     */
    String buildCatalogSubscribeXml(String sn, String deviceIdentification);

    /**
     * 构建移动位置订阅 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @param interval 位置上报间隔（秒）
     * @return 移动位置订阅 XML 字符串
     */
    String buildMobilePositionSubscribeXml(String sn, String deviceIdentification, int interval);

    /**
     * 构建录像状态订阅 XML（GB2022 新增）。
     * GB2016 不支持此功能，默认返回空。
     *
     * @param sn       命令序列号
     * @param deviceIdentification 目标设备国标编号
     * @return 录像状态订阅 XML 字符串，GB2016 返回空字符串
     */
    default String buildRecordStatusSubscribeXml(String sn, String deviceIdentification) {
        return "";
    }

    // ======================== 通知类 XML 构建 ========================

    /**
     * 构建目录变更通知 XML（用于级联推送）
     *
     * @param sn       命令序列号
     * @param deviceIdentification 设备国标编号
     * @param event    变更事件类型（ON/OFF/VLOST/DEFECT/ADD/DEL/UPDATE）
     * @return 目录通知 XML 字符串
     */
    default String buildCatalogNotifyXml(String sn, String deviceIdentification, String event) {
        return """
                <?xml version="1.0" encoding="%s"?>
                <Notify>
                    <CmdType>Catalog</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <SumNum>1</SumNum>
                    <DeviceList Num="1">
                        <Item>
                            <DeviceID>%s</DeviceID>
                            <Event>%s</Event>
                        </Item>
                    </DeviceList>
                </Notify>
                """.formatted(getEncoding(), sn, deviceIdentification, deviceIdentification, event);
    }

    /**
     * 构建心跳通知 XML
     *
     * @param sn       命令序列号
     * @param deviceIdentification 设备国标编号
     * @return 心跳通知 XML 字符串
     */
    default String buildKeepaliveNotifyXml(String sn, String deviceIdentification) {
        return """
                <?xml version="1.0" encoding="%s"?>
                <Notify>
                    <CmdType>Keepalive</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <Status>OK</Status>
                </Notify>
                """.formatted(getEncoding(), sn, deviceIdentification);
    }

    // ======================== 告警类 XML 构建 ========================

    /**
     * 构建告警查询 XML（带告警级别过滤）
     *
     * @param sn                 命令序列号
     * @param deviceIdentification           目标设备国标编号
     * @param startAlarmPriority 起始告警级别（0=全部）
     * @param endAlarmPriority   结束告警级别（0=全部）
     * @return 告警查询 XML 字符串
     */
    default String buildAlarmQueryXml(String sn, String deviceIdentification,
                                      Integer startAlarmPriority, Integer endAlarmPriority) {
        return """
                <?xml version="1.0" encoding="%s"?>
                <Query>
                    <CmdType>Alarm</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <StartAlarmPriority>%d</StartAlarmPriority>
                    <EndAlarmPriority>%d</EndAlarmPriority>
                </Query>
                """.formatted(getEncoding(), sn, deviceIdentification,
                startAlarmPriority != null ? startAlarmPriority : 0,
                endAlarmPriority != null ? endAlarmPriority : 0);
    }

    /**
     * 构建告警确认 XML（GB2022 新增）。
     * 向设备发送告警确认消息，通知设备告警已被处理。
     *
     * @param sn         命令序列号
     * @param deviceIdentification   目标设备国标编号
     * @param alarmMethod 告警方式
     * @param alarmType   告警类型
     * @return 告警确认 XML 字符串
     */
    default String buildAlarmAckXml(String sn, String deviceIdentification,
                                    String alarmMethod, String alarmType) {
        return """
                <?xml version="1.0" encoding="%s"?>
                <Control>
                    <CmdType>DeviceControl</CmdType>
                    <SN>%s</SN>
                    <DeviceID>%s</DeviceID>
                    <AlarmCmd>ResetAlarm</AlarmCmd>
                    <Info>
                        <AlarmMethod>%s</AlarmMethod>
                        <AlarmType>%s</AlarmType>
                    </Info>
                </Control>
                """.formatted(getEncoding(), sn, deviceIdentification, alarmMethod, alarmType);
    }
}
