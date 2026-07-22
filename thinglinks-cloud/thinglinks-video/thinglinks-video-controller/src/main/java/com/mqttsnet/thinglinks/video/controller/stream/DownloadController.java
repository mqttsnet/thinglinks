package com.mqttsnet.thinglinks.video.controller.stream;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.dto.media.stream.StreamInfo;
import com.mqttsnet.thinglinks.video.service.stream.DownloadService;
import com.mqttsnet.thinglinks.video.vo.result.stream.PlaybackResultVO;
import com.mqttsnet.thinglinks.video.vo.save.stream.DownloadSaveVO;
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
 * 录像下载控制器。
 * 提供录像下载发起和停止的 REST API。
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
@RequestMapping("/download")
@Tag(name = "录像下载")
public class DownloadController {

    private final DownloadService downloadService;

    /**
     * 发起录像下载
     *
     * @param saveVO 下载请求参数
     * @return 下载结果（多协议流地址）
     */
    @PostMapping("/start")
    @Operation(summary = "发起录像下载", description = "向指定设备通道发起录像下载，支持倍速下载")
    @ApiResponse(responseCode = "200", description = "下载发起成功")
    public R<PlaybackResultVO> download(@Valid @RequestBody DownloadSaveVO saveVO) {
        log.info("发起录像下载: deviceIdentification={}, channelIdentification={}, time={} ~ {}, speed={}",
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(),
                saveVO.getStartTime(), saveVO.getEndTime(), saveVO.getDownloadSpeed());
        StreamInfo streamInfo = downloadService.download(
                saveVO.getDeviceIdentification(), saveVO.getChannelIdentification(),
                saveVO.getStartTime(), saveVO.getEndTime(), saveVO.getDownloadSpeed());
        return R.success(convertToResultVO(streamInfo));
    }

    /**
     * 停止下载
     *
     * @param deviceIdentification  设备国标编号
     * @param channelIdentification 通道国标编号
     * @return 操作结果
     */
    @DeleteMapping("/stop")
    @Operation(summary = "停止下载", description = "停止指定设备通道的录像下载")
    @ApiResponse(responseCode = "200", description = "停止成功")
    public R<Boolean> stop(
            @Parameter(description = "设备国标编号", required = true) @RequestParam String deviceIdentification,
            @Parameter(description = "通道国标编号", required = true) @RequestParam String channelIdentification) {
        log.info("停止下载: deviceIdentification={}, channelIdentification={}", deviceIdentification, channelIdentification);
        downloadService.stopDownload(deviceIdentification, channelIdentification);
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
