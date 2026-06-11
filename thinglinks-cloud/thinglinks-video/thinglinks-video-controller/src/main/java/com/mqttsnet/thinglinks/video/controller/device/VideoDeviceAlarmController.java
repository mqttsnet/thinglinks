package com.mqttsnet.thinglinks.video.controller.device;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.device.VideoDeviceAlarm;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceAlarmService;
import com.mqttsnet.thinglinks.video.vo.query.device.VideoDeviceAlarmPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.device.AlarmStatisticsResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceAlarmResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Description:
 * 设备告警控制器。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceAlarm")
@Tag(name = "设备告警")
public class VideoDeviceAlarmController {

    private final VideoDeviceAlarmService videoDeviceAlarmService;
    private final EchoService echoService;

    /**
     * 分页查询告警列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询告警", description = "支持设备/通道/级别/状态/时间范围等条件筛选")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<IPage<VideoDeviceAlarmResultVO>> page(@RequestBody PageParams<VideoDeviceAlarmPageQuery> params) {
        VideoDeviceAlarm model = BeanPlusUtil.toBean(params.getModel(), VideoDeviceAlarm.class);
        QueryWrap<VideoDeviceAlarm> queryWrap = new QueryWrap<>(model);

        VideoDeviceAlarmPageQuery query = params.getModel();
        if (ObjectUtil.isNotNull(query.getStartTime())) {
            queryWrap.lambda().ge(VideoDeviceAlarm::getAlarmTime, query.getStartTime());
        }
        if (ObjectUtil.isNotNull(query.getEndTime())) {
            queryWrap.lambda().le(VideoDeviceAlarm::getAlarmTime, query.getEndTime());
        }
        queryWrap.lambda().orderByDesc(VideoDeviceAlarm::getAlarmTime);

        DataScopeHelper.startDataScope("video_device_alarm");

        IPage<VideoDeviceAlarm> page = videoDeviceAlarmService.page(params.buildPage(), queryWrap);
        IPage<VideoDeviceAlarmResultVO> resultPage = BeanPlusUtil.toBeanPage(page, VideoDeviceAlarmResultVO.class);
        echoService.action(resultPage);
        return R.success(resultPage);
    }

    /**
     * 查询告警详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "告警详情", description = "按 ID 查询告警详情，含 Echo 字典翻译")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<VideoDeviceAlarmResultVO> getById(
            @Parameter(description = "告警ID", required = true) @PathVariable Long id) {
        return Optional.ofNullable(videoDeviceAlarmService.getById(id))
                .map(alarm -> BeanPlusUtil.toBean(alarm, VideoDeviceAlarmResultVO.class))
                .map(vo -> {
                    echoService.action(vo);
                    return R.success(vo);
                })
                .orElseGet(() -> R.success(null));
    }

    /**
     * 查询设备告警列表（无分页，兼容旧接口）
     */
    @GetMapping("/list")
    @Operation(summary = "告警列表", description = "根据设备ID查询告警列表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<List<VideoDeviceAlarmResultVO>> list(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        List<VideoDeviceAlarmResultVO> list = videoDeviceAlarmService.listByDeviceId(deviceIdentification);
        echoService.action(list);
        return R.success(list);
    }

    /**
     * 告警统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "告警统计", description = "按级别/设备/天维度统计告警数量")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<AlarmStatisticsResultVO> statistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return R.success(videoDeviceAlarmService.statistics(startTime, endTime));
    }

    /**
     * 未处理告警计数
     */
    @GetMapping("/unhandledCount")
    @Operation(summary = "未处理告警计数", description = "查询未处理告警总数")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public R<Long> unhandledCount() {
        return R.success(videoDeviceAlarmService.unhandledCount());
    }

    /**
     * 确认告警（待处理→处理中）
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认告警", description = "将告警从待处理标记为处理中，需填写处理说明")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public R<Boolean> confirm(
            @Parameter(description = "告警ID", required = true) @RequestParam Long alarmId,
            @Parameter(description = "处理说明") @RequestParam(required = false) String handleResult) {
        videoDeviceAlarmService.confirmAlarm(alarmId, ContextUtil.getUserId(), handleResult);
        return R.success(true);
    }

    /**
     * 解决告警（处理中→已处理）
     */
    @PostMapping("/resolve")
    @Operation(summary = "解决告警", description = "将告警标记为已处理，需填写处理结果")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public R<Boolean> resolve(
            @Parameter(description = "告警ID", required = true) @RequestParam Long alarmId,
            @Parameter(description = "处理结果") @RequestParam(required = false) String handleResult) {
        videoDeviceAlarmService.resolveAlarm(alarmId, handleResult);
        return R.success(true);
    }

    /**
     * 忽略告警（待处理/处理中→已忽略）
     */
    @PostMapping("/ignore")
    @Operation(summary = "忽略告警", description = "将告警标记为已忽略")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public R<Boolean> ignore(
            @Parameter(description = "告警ID", required = true) @RequestParam Long alarmId,
            @Parameter(description = "忽略原因") @RequestParam(required = false) String handleResult) {
        videoDeviceAlarmService.ignoreAlarm(alarmId, handleResult);
        return R.success(true);
    }

    /**
     * 批量忽略告警
     */
    @PostMapping("/batchIgnore")
    @Operation(summary = "批量忽略", description = "批量忽略多条告警")
    @ApiResponse(responseCode = "200", description = "操作成功")
    public R<Boolean> batchIgnore(@RequestBody List<Long> alarmIds) {
        videoDeviceAlarmService.batchIgnore(alarmIds);
        return R.success(true);
    }

    /**
     * 更新告警处理状态（兼容旧接口）
     */
    @PostMapping("/handle")
    @Operation(summary = "处理告警", description = "更新告警处理状态")
    @ApiResponse(responseCode = "200", description = "处理成功")
    public R<Boolean> handle(
            @Parameter(description = "告警ID", required = true) @RequestParam Long alarmId,
            @Parameter(description = "处理状态(1-已处理/2-已忽略)", required = true) @RequestParam Integer handleStatus) {
        videoDeviceAlarmService.updateHandleStatus(alarmId, handleStatus);
        return R.success(true);
    }

    /**
     * 清除设备所有告警
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清除告警", description = "清除设备所有告警")
    @ApiResponse(responseCode = "200", description = "清除成功")
    public R<Boolean> clear(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification) {
        videoDeviceAlarmService.clearAlarmByDeviceId(deviceIdentification);
        return R.success(true);
    }
}
