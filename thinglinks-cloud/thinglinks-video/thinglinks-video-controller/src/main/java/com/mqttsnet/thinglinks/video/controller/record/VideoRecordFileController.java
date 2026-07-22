package com.mqttsnet.thinglinks.video.controller.record;

import cn.hutool.core.bean.BeanUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService;
import com.mqttsnet.thinglinks.video.vo.result.record.VideoRecordFileResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前端控制器 - 录像文件管理
 * <p>
 * 录像文件为系统自动生成，不提供手动新增/修改。
 * 提供查询（按设备/计划筛选）和删除操作。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/recordFile")
@Tag(name = "录像文件管理")
public class VideoRecordFileController {

    private final VideoRecordFileService videoRecordFileService;
    private final EchoService echoService;

    /**
     * 根据设备和通道查询录像文件列表
     */
    @GetMapping("/listByDevice")
    @Operation(summary = "根据设备通道查询录像", description = "根据设备国标编号和通道国标编号查询录像文件列表")
    public R<List<VideoRecordFileResultVO>> listByDeviceAndChannel(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        DataScopeHelper.startDataScope("video_record_file");
        List<VideoRecordFileResultVO> list = BeanUtil.copyToList(
                videoRecordFileService.listByDeviceAndChannel(deviceIdentification, channelIdentification),
                VideoRecordFileResultVO.class);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 根据录像计划查询录像文件列表
     */
    @GetMapping("/listByPlan")
    @Operation(summary = "根据录像计划查询录像", description = "根据录像计划ID查询录像文件列表")
    public R<List<VideoRecordFileResultVO>> listByPlanId(
            @Parameter(description = "录像计划ID", required = true) @RequestParam Long planId) {
        DataScopeHelper.startDataScope("video_record_file");
        List<VideoRecordFileResultVO> list = BeanUtil.copyToList(
                videoRecordFileService.listByPlanId(planId),
                VideoRecordFileResultVO.class);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 根据ID查询录像文件详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询录像文件详情", description = "根据ID获取录像文件详细信息")
    public R<VideoRecordFileResultVO> getById(
            @Parameter(description = "录像文件ID", required = true) @PathVariable Long id) {
        VideoRecordFileResultVO result = BeanUtil.toBean(videoRecordFileService.getById(id), VideoRecordFileResultVO.class);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 根据设备、通道和日期查询录像文件列表
     */
    @GetMapping("/listByDate")
    @Operation(summary = "根据日期查询录像", description = "根据设备通道和日期查询录像文件列表")
    public R<List<VideoRecordFileResultVO>> listByDate(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
            @Parameter(description = "查询日期(yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DataScopeHelper.startDataScope("video_record_file");
        List<VideoRecordFileResultVO> list = BeanUtil.copyToList(
                videoRecordFileService.listByDate(deviceIdentification, channelIdentification, date),
                VideoRecordFileResultVO.class);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 查询指定月份内有录像数据的日期列表
     */
    @GetMapping("/getRecordDates")
    @Operation(summary = "查询有录像的日期", description = "查询指定月份内有录像数据的日期列表，用于日历标记")
    public R<List<String>> getRecordDates(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
            @Parameter(description = "查询月份(yyyy-MM)", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        List<String> dates = videoRecordFileService.getRecordDates(deviceIdentification, channelIdentification, month)
                .stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        return R.success(dates);
    }

    /**
     * 获取录像文件播放地址
     * <p>
     * 通过 FileFacade 跨服务调用 base 模块，根据录像文件关联的 fileId 获取 OSS URL。
     * 录像文件需事先通过 ZLMediaKit Hook 上传到 base 通用 OSS 存储。
     */
    @GetMapping("/playUrl/{id}")
    @Operation(summary = "获取播放地址", description = "根据录像文件ID获取OSS播放地址")
    public R<String> getPlayUrl(
            @Parameter(description = "录像文件ID", required = true) @PathVariable Long id) {
        String url = videoRecordFileService.getFileUrl(id);
        if (url == null) {
            return R.fail("录像文件不存在或未关联存储文件");
        }
        return R.success(url);
    }

    /**
     * 获取录像文件下载地址
     * <p>
     * 下载与播放使用同一个 OSS URL，浏览器端通过 Content-Disposition 或直接访问即可下载。
     * 若需强制下载（非预览），前端可在 URL 后追加 response-content-disposition 参数。
     */
    @GetMapping("/downloadUrl/{id}")
    @Operation(summary = "获取下载地址", description = "根据录像文件ID获取OSS下载地址")
    public R<String> getDownloadUrl(
            @Parameter(description = "录像文件ID", required = true) @PathVariable Long id) {
        String url = videoRecordFileService.getFileUrl(id);
        if (url == null) {
            return R.fail("录像文件不存在或未关联存储文件");
        }
        return R.success(url);
    }

    /**
     * 删除录像文件记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除录像文件", description = "删除录像文件记录")
    public R<Boolean> delete(
            @Parameter(description = "录像文件ID", required = true) @PathVariable Long id) {
        log.info("删除录像文件: id={}", id);
        videoRecordFileService.removeByIds(Collections.singletonList(id));
        return R.success(true);
    }
}
