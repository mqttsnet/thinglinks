package com.mqttsnet.thinglinks.video.service.record;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 录像文件业务接口。
 *
 * <p>提供录像文件的查询与持久化操作，支持按设备/通道维度
 * 或按 {@link VideoRecordPlanService 录像计划} 维度检索录像文件。
 * 继承 {@link SuperService} 获得通用 CRUD 能力。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VideoRecordFile
 * @see VideoRecordPlanService
 * @see SuperService
 */
public interface VideoRecordFileService extends SuperService<Long, VideoRecordFile> {

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
     * @see VideoRecordPlanService
     */
    List<VideoRecordFile> listByPlanId(Long planId);

    /**
     * 保存录像文件信息。
     *
     * <p>将录像文件元数据持久化到数据库，包括文件路径、时长、
     * 关联设备/通道等信息。</p>
     *
     * @param recordFile 待保存的 {@link VideoRecordFile} 实体，不能为 {@code null}
     */
    void saveRecordFile(VideoRecordFile recordFile);

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

    /**
     * 根据录像文件 ID 获取 OSS 播放/下载 URL。
     * <p>通过 {@code FileFacade} 查询 base 服务获取文件 URL。</p>
     *
     * @param recordFileId 录像文件主键 ID
     * @return 文件的 OSS URL；若文件不存在或无 fileId 则返回 {@code null}
     */
    String getFileUrl(Long recordFileId);

    /**
     * 清理过期录像文件。
     * <p>
     * 根据关联录像计划的 retentionDays，将超过保留天数的录像文件
     * 标记为已过期（fileStatus=2），并删除 OSS 文件。
     * 无关联计划的文件不处理。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 清理的文件数量
     */
    int cleanExpiredFiles(Long tenantId);
}
