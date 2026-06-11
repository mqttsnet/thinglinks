package com.mqttsnet.thinglinks.video.controller.stream;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.service.stream.PlaybackService;
import com.mqttsnet.thinglinks.video.service.stream.RecordInfoService;
import com.mqttsnet.thinglinks.video.vo.result.stream.PlaybackResultVO;
import com.mqttsnet.thinglinks.video.vo.save.stream.PlaybackSaveVO;
import com.mqttsnet.thinglinks.video.vo.save.stream.RecordQuerySaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * 录像回放控制器。
 * 提供录像回放发起、停止、暂停、恢复、倍速、拖拽及设备端录像查询等 REST API。
 * <p>
 *
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/playback")
@Tag(name = "录像回放")
public class PlaybackController {

    private final PlaybackService playbackService;
    private final RecordInfoService recordInfoService;

    /**
     * 发起录像回放
     *
     * @param saveVO 回放请求参数
     * @return 回放结果（多协议流地址）
     */
    @PostMapping("/start")
    @Operation(summary = "发起录像回放", description = "向指定设备通道发起录像回放，返回多协议流地址")
    @ApiResponse(responseCode = "200", description = "回放发起成功")
    public R<PlaybackResultVO> playback(@Valid @RequestBody PlaybackSaveVO saveVO) {
        log.info("发起录像回放: deviceIdentification={}, channelIdentification={}, time={} ~ {}",
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(), saveVO.getStartTime(), saveVO.getEndTime());
        StreamInfo streamInfo = playbackService.playback(
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(),
                saveVO.getStartTime(), saveVO.getEndTime());
        return R.success(convertToResultVO(streamInfo));
    }

    /**
     * 停止回放
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 操作结果
     */
    @DeleteMapping("/stop")
    @Operation(summary = "停止回放", description = "停止指定设备通道的录像回放")
    @ApiResponse(responseCode = "200", description = "停止成功")
    public R<Boolean> stop(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        log.info("停止回放: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        playbackService.stopPlayback(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 回放暂停
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 操作结果
     */
    @PostMapping("/pause")
    @Operation(summary = "回放暂停", description = "暂停指定设备通道的录像回放")
    @ApiResponse(responseCode = "200", description = "暂停成功")
    public R<Boolean> pause(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        log.info("回放暂停: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        playbackService.pause(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 回放恢复
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 操作结果
     */
    @PostMapping("/resume")
    @Operation(summary = "回放恢复", description = "恢复指定设备通道的录像回放")
    @ApiResponse(responseCode = "200", description = "恢复成功")
    public R<Boolean> resume(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        log.info("回放恢复: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        playbackService.resume(deviceIdentification, channelIdentification);
        return R.success(true);
    }

    /**
     * 回放倍速控制
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param speed     倍速（0.25/0.5/1/2/4）
     * @return 操作结果
     */
    @PostMapping("/speed")
    @Operation(summary = "回放倍速", description = "调整指定设备通道的回放速度")
    @ApiResponse(responseCode = "200", description = "倍速设置成功")
    public R<Boolean> speed(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
            @Parameter(description = "倍速(0.25/0.5/1/2/4)", required = true) @RequestParam double speed) {
        log.info("回放倍速: deviceIdentification={}, channelIdentification={}, speed={}x", deviceIdentification, channelIdentification, speed);
        playbackService.speed(deviceIdentification, channelIdentification, speed);
        return R.success(true);
    }

    /**
     * 回放拖拽（跳转到指定时间）
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @param seekTime  跳转时间（秒级时间戳）
     * @return 操作结果
     */
    @PostMapping("/seek")
    @Operation(summary = "回放拖拽", description = "跳转到指定时间点进行回放")
    @ApiResponse(responseCode = "200", description = "拖拽成功")
    public R<Boolean> seek(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification,
            @Parameter(description = "跳转时间(秒级时间戳)", required = true) @RequestParam long seekTime) {
        log.info("回放拖拽: deviceIdentification={}, channelIdentification={}, seekTime={}", deviceIdentification, channelIdentification, seekTime);
        playbackService.seek(deviceIdentification, channelIdentification, seekTime);
        return R.success(true);
    }

    /**
     * 查询设备端录像
     *
     * @param saveVO 录像查询请求参数
     * @return 操作结果（录像信息通过设备异步回复返回）
     */
    @PostMapping("/recordQuery")
    @Operation(summary = "查询设备端录像", description = "向设备发起录像查询，结果通过设备异步回复")
    @ApiResponse(responseCode = "200", description = "查询请求发送成功")
    public R<Boolean> queryRecordInfo(@Valid @RequestBody RecordQuerySaveVO saveVO) {
        log.info("查询设备端录像: deviceIdentification={}, channelIdentification={}, time={} ~ {}",
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(), saveVO.getStartTime(), saveVO.getEndTime());
        recordInfoService.queryRecordInfo(
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(),
                saveVO.getStartTime(), saveVO.getEndTime(), saveVO.getType());
        return R.success(true);
    }

    /**
     * StreamInfo → PlaybackResultVO 转换
     */
    private PlaybackResultVO convertToResultVO(StreamInfo streamInfo) {
        return PlaybackResultVO.builder()
                .deviceIdentification(streamInfo.getDeviceIdentification())
                .channelIdentification(streamInfo.getStream())
                .app(streamInfo.getApp())
                .stream(streamInfo.getStream())
                .mediaServerIp(streamInfo.getHost())
                .startTime(streamInfo.getStartTime())
                .endTime(streamInfo.getEndTime())
                .flv(streamInfo.getFlv())
                .httpsFlv(streamInfo.getHttps_flv())
                .wsFlv(streamInfo.getWs_flv())
                .hls(streamInfo.getHls())
                .httpsHls(streamInfo.getHttps_hls())
                .rtmp(streamInfo.getRtmp())
                .rtsp(streamInfo.getRtsp())
                .fmp4(streamInfo.getFmp4())
                .ts(streamInfo.getTs())
                .rtc(streamInfo.getRtc())
                .rtcs(streamInfo.getRtcs())
                .callId(streamInfo.getCallId())
                .build();
    }
}
