package com.mqttsnet.thinglinks.video.manager.record;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 录像文件 Manager 接口。
 *
 * <p>封装录像文件的数据访问逻辑，为
 * {@link com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService}
 * 提供底层数据操作支持。继承 {@link SuperManager} 获得通用持久化能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoRecordFile
 * @see SuperManager
 * @see com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService
 */
public interface VideoRecordFileManager extends SuperManager<VideoRecordFile> {

    /**
     * 根据设备国标编号和通道国标编号查询录像文件列表。
     *
     * @param deviceIdentification  设备国标编号（GB/T 28181 编码），不能为 {@code null}
     * @param channelIdentification 通道国标编号（GB/T 28181 编码），不能为 {@code null}
     * @return 匹配的 {@link VideoRecordFile} 列表，若无匹配记录则返回空列表
     */
    List<VideoRecordFile> listByDeviceAndChannel(String deviceIdentification, String channelIdentification);

    /**
     * 根据录像计划 ID 查询关联的录像文件列表。
     *
     * @param planId 录像计划主键 ID，不能为 {@code null}
     * @return 该计划下的 {@link VideoRecordFile} 列表，若无匹配记录则返回空列表
     * @see com.mqttsnet.thinglinks.video.manager.record.VideoRecordPlanManager
     */
    List<VideoRecordFile> listByPlanId(Long planId);

    /**
     * 根据设备、通道和日期查询录像文件列表。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param date                  查询日期
     * @return 该日期内的录像文件列表，按开始时间升序排列
     */
    List<VideoRecordFile> listByDate(String deviceIdentification, String channelIdentification, LocalDate date);

    /**
     * 查询指定月份内有录像数据的日期列表。
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param month                 查询月份
     * @return 有录像的日期列表
     */
    List<LocalDate> getRecordDates(String deviceIdentification, String channelIdentification, YearMonth month);
}
